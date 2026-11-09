package com.objecttracker;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ColorTracker {

    public static class Detection {
        public final String name;
        public final Rect   box;
        public final Scalar displayColor;
        public final double area;

        public Detection(String name, Rect box, Scalar displayColor, double area) {
            this.name = name;
            this.box = box;
            this.displayColor = displayColor;
            this.area = area;
        }
    }

    public static class ColorProfile {
        public final String name;
        public final Scalar displayBGR;
        public final List<Scalar> lowers = new ArrayList<>();
        public final List<Scalar> uppers = new ArrayList<>();

        public ColorProfile(String name, Scalar displayBGR) {
            this.name = name;
            this.displayBGR = displayBGR;
        }

        public ColorProfile range(double lh, double ls, double lv,
                                  double uh, double us, double uv) {
            lowers.add(new Scalar(lh, ls, lv));
            uppers.add(new Scalar(uh, us, uv));
            return this;
        }
    }

    public static final List<ColorProfile> PROFILES = new ArrayList<>();
    static {
        // Red wraps around the hue circle so it needs two HSV ranges
        PROFILES.add(new ColorProfile("RED", new Scalar(60, 60, 255))
                .range(0,   120, 70,  10,  255, 255)
                .range(170, 120, 70,  180, 255, 255));
        PROFILES.add(new ColorProfile("ORANGE", new Scalar(0, 140, 255))
                .range(11, 130, 100, 22, 255, 255));
        PROFILES.add(new ColorProfile("YELLOW", new Scalar(0, 220, 255))
                .range(23, 100, 100, 34, 255, 255));
        PROFILES.add(new ColorProfile("GREEN", new Scalar(80, 220, 80))
                .range(40, 70, 70, 85, 255, 255));
        PROFILES.add(new ColorProfile("BLUE", new Scalar(255, 150, 50))
                .range(95, 120, 70, 130, 255, 255));
        PROFILES.add(new ColorProfile("PURPLE", new Scalar(200, 80, 200))
                .range(131, 70, 50, 160, 255, 255));
    }

    public static List<double[]> parseRanges(String configured) {
        if (configured == null || configured.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<double[]> ranges = new ArrayList<>();
        String[] rangeSpecs = configured.split(";");
        for (String rangeSpec : rangeSpecs) {
            String trimmed = rangeSpec.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String[] values = trimmed.split(",");
            if (values.length != 6) {
                throw new IllegalArgumentException(
                        "Invalid HSV range format. Expected six comma-separated values in the form lh,ls,lv,uh,us,uv.");
            }

            double[] numeric = new double[6];
            for (int i = 0; i < values.length; i++) {
                numeric[i] = Double.parseDouble(values[i].trim());
            }
            ranges.add(numeric);
        }

        if (ranges.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid HSV range format. Expected six comma-separated values in the form lh,ls,lv,uh,us,uv.");
        }
        return ranges;
    }

    public static void applySystemOverrides() {
        for (ColorProfile profile : PROFILES) {
            String propertyName = "objecttracker.color." + profile.name.toUpperCase(Locale.ROOT);
            String configured = System.getProperty(propertyName);
            if (configured == null || configured.trim().isEmpty()) {
                continue;
            }

            profile.lowers.clear();
            profile.uppers.clear();

            List<double[]> rawRanges = parseRanges(configured);
            for (double[] values : rawRanges) {
                profile.lowers.add(new Scalar(values[0], values[1], values[2]));
                profile.uppers.add(new Scalar(values[3], values[4], values[5]));
            }
        }
    }

    private static final double MIN_CONTOUR_AREA = 800.0;
    private static final int MIN_BBOX_DIMENSION = 10;

    // Reused per-frame buffers so we don't allocate Mats in the hot loop
    private final Mat hsv      = new Mat();
    private final Mat mask     = new Mat();
    private final Mat tempMask = new Mat();
    private final Mat kernel   =
            Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));

    public List<Detection> detect(Mat frame) {
        List<Detection> detections = new ArrayList<>();
        if (frame == null || frame.empty()) {
            return detections;
        }

        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);

        for (ColorProfile profile : PROFILES) {
            boolean first = true;
            for (int i = 0; i < profile.lowers.size(); i++) {
                if (first) {
                    Core.inRange(hsv, profile.lowers.get(i), profile.uppers.get(i), mask);
                    first = false;
                } else {
                    Core.inRange(hsv, profile.lowers.get(i), profile.uppers.get(i), tempMask);
                    Core.bitwise_or(mask, tempMask, mask);
                }
            }

            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN,  kernel);
            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernel);

            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(mask, contours, hierarchy,
                    Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            hierarchy.release();

            MatOfPoint largest = null;
            double largestArea = MIN_CONTOUR_AREA;
            for (MatOfPoint c : contours) {
                double area = Imgproc.contourArea(c);
                if (area <= MIN_CONTOUR_AREA) {
                    continue;
                }

                Rect box = Imgproc.boundingRect(c);
                if (box.width < MIN_BBOX_DIMENSION || box.height < MIN_BBOX_DIMENSION) {
                    continue;
                }

                if (area > largestArea) {
                    largestArea = area;
                    largest = c;
                }
            }

            if (largest != null) {
                Rect box = Imgproc.boundingRect(largest);
                if (box.width >= MIN_BBOX_DIMENSION && box.height >= MIN_BBOX_DIMENSION) {
                    detections.add(new Detection(profile.name, box, profile.displayBGR, largestArea));
                }
            }

            for (MatOfPoint c : contours) c.release();
        }
        return detections;
    }

    public void release() {
        hsv.release();
        mask.release();
        tempMask.release();
        kernel.release();
    }
}
