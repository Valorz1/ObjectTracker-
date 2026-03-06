package com.objecttracker;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

public class ColorTracker {

    // Define all colors to track
    public static final double[][] COLORS = {
        // {lowerH, lowerS, lowerV, upperH, upperS, upperV, R, G, B}
        {0,   120, 70,  10,  255, 255, 0,   255, 0  }, // Red
        {40,  50,  50,  80,  255, 255, 0,   255, 0  }, // Green
        {100, 50,  50,  130, 255, 255, 255, 0,   0  }, // Blue
    };

    public static final String[] COLOR_NAMES = {
        "Red", "Green", "Blue", "Yellow", "Purple", "Orange"
    };

    public List<Rect> findAllObjects(Mat frame) {
        List<Rect> results = new ArrayList<>();

        for (double[] color : COLORS) {
            Scalar lower = new Scalar(color[0], color[1], color[2]);
            Scalar upper = new Scalar(color[3], color[4], color[5]);

            Mat hsv  = new Mat();
            Mat mask = new Mat();

            Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);
            Core.inRange(hsv, lower, upper, mask);

            Imgproc.erode(mask, mask, new Mat(), new Point(-1,-1), 2);
            Imgproc.dilate(mask, mask, new Mat(), new Point(-1,-1), 2);

            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(mask, contours, new Mat(),
                    Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            if (!contours.isEmpty()) {
                MatOfPoint largest = contours.stream()
                        .max((a, b) -> Double.compare(
                                Imgproc.contourArea(a),
                                Imgproc.contourArea(b)))
                        .orElse(null);

                if (largest != null && Imgproc.contourArea(largest) > 500) {
                    results.add(Imgproc.boundingRect(largest));
                } else {
                    results.add(null);
                }
            } else {
                results.add(null);
            }
        }
        return results;
    }
}