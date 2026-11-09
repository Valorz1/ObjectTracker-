package com.objecttracker;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import java.util.List;

public class Main {

    private static final Scalar HUD_CYAN  = new Scalar(255, 220, 0);
    private static final Scalar HUD_GREEN = new Scalar(120, 255, 80);
    private static final Scalar HUD_AMBER = new Scalar(0, 200, 255);
    private static final Scalar HUD_WHITE = new Scalar(240, 240, 240);
    private static final Scalar HUD_BG    = new Scalar(20, 20, 20);
    private static final Scalar SCAN_LINE = new Scalar(90, 80, 0);

    public static void main(String[] args) {
        int cameraIndex = parseIntOption(args, "--camera", 0);
        boolean hd      = hasFlag(args, "--hd");
        int width       = parseIntOption(args, "--width",  hd ? 1280 : CameraCapture.DEFAULT_WIDTH);
        int height      = parseIntOption(args, "--height", hd ? 720  : CameraCapture.DEFAULT_HEIGHT);
        boolean showHud = !hasFlag(args, "--no-hud");
        boolean useServo = !hasFlag(args, "--no-servo");

        if (hasFlag(args, "--help") || hasFlag(args, "-h")) {
            printHelp();
            return;
        }

        if (hasFlag(args, "--list-cameras")) {
            CameraCapture.listCameras(6);
            return;
        }

        CameraCapture   camera   = new CameraCapture(cameraIndex, width, height);
        ColorTracker    detector = new ColorTracker();
        ColorTracker.applySystemOverrides();
        ServoController servos   = new ServoController();
        TrackingDisplay display  = new TrackingDisplay();

        if (!camera.isOpened()) {
            System.out.println("Exiting - no camera.");
            return;
        }

        System.out.println("Object Tracker v2.0  |  [Q/ESC] quit  [H] HUD  [P/Space] pause  [--camera N] select camera");

        long   lastFpsTime  = System.nanoTime();
        int    frameCounter = 0;
        double fps          = 0.0;
        int    frameNum     = 0;
        boolean paused  = false;
        Mat frame = new Mat();
        int emptyFrames = 0;

        while (true) {
            if (!paused) {
                frame = camera.getFrame();
                if (frame.empty()) {
                    // Camo returns empty frames until the phone is streaming.
                    emptyFrames++;
                    if (emptyFrames == 100) {
                        System.out.println("No picture from camera " + cameraIndex
                                + " yet. If this is Camo, open Camo Studio and check your phone is connected.");
                    }
                    sleepQuietly(30);
                    continue;
                }
                emptyFrames = 0;
                frameNum++;
            }

            int w = frame.cols();
            int h = frame.rows();
            servos.setFrameSize(w, h);

            List<ColorTracker.Detection> detections = detector.detect(frame);
            detections.sort((a, b) -> Double.compare(b.area, a.area));

            ColorTracker.Detection primary =
                    detections.isEmpty() ? null : detections.get(0);

            for (int i = 0; i < detections.size(); i++) {
                display.drawDetection(frame, detections.get(i), i == 0, frameNum);
            }

            if (primary != null && !paused && useServo) {
                servos.adjustToTarget(
                        primary.box.x + primary.box.width  / 2.0,
                        primary.box.y + primary.box.height / 2.0);
            }

            frameCounter++;
            long now = System.nanoTime();
            if (now - lastFpsTime >= 500_000_000L) {
                fps = frameCounter * 1e9 / (now - lastFpsTime);
                frameCounter = 0;
                lastFpsTime = now;
            }

            if (showHud) {
                drawHud(frame, fps, detections, primary, servos, paused, frameNum, useServo);
            }

            HighGui.imshow("Object Tracker v2.0", frame);
            int key = HighGui.waitKey(30);
            if (key == 'q' || key == 'Q' || key == 27) break;
            if (key == 'h' || key == 'H') showHud = !showHud;
            if (key == 'p' || key == 'P' || key == ' ') paused = !paused;
        }

        camera.release();
        detector.release();
        HighGui.destroyAllWindows();
    }

    private static int parseIntOption(String[] args, String name, int defaultValue) {
        for (int i = 0; i < args.length; i++) {
            if (name.equals(args[i]) && i + 1 < args.length) {
                try {
                    return Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid value for " + name + ": " + args[i + 1]);
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String arg : args) {
            if (flag.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static void printHelp() {
        System.out.println("Usage: java com.objecttracker.Main [options]");
        System.out.println("  --camera N         Select webcam index (default 0)");
        System.out.println("  --hd               Request 1280x720 video");
        System.out.println("  --width N          Request video width");
        System.out.println("  --height N         Request video height");
        System.out.println("  --list-cameras     List available cameras");
        System.out.println("  --no-hud           Hide HUD overlays");
        System.out.println("  --no-servo         Disable servo updates");
        System.out.println("  --help, -h         Show this help message");
    }

    private static void drawHud(Mat frame, double fps,
                                List<ColorTracker.Detection> dets,
                                ColorTracker.Detection primary,
                                ServoController servos,
                                boolean paused, int frameNum, boolean useServo) {
        int w = frame.cols();
        int h = frame.rows();

        Mat overlay = frame.clone();
        Imgproc.rectangle(overlay, new Point(0, 0),       new Point(265, 95),  HUD_BG, -1);
        Imgproc.rectangle(overlay, new Point(w - 230, 0), new Point(w, 60),    HUD_BG, -1);
        Imgproc.rectangle(overlay, new Point(0, h - 38),  new Point(w, h),     HUD_BG, -1);
        Core.addWeighted(overlay, 0.55, frame, 0.45, 0, frame);
        overlay.release();

        Imgproc.putText(frame, "OBJECT TRACKER v2.0", new Point(10, 22),
                Imgproc.FONT_HERSHEY_DUPLEX, 0.55, HUD_CYAN, 1);
        Imgproc.putText(frame, String.format("FPS: %.1f", fps), new Point(10, 45),
                Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, HUD_WHITE, 1);
        Imgproc.putText(frame, "TARGETS: " + dets.size(), new Point(10, 65),
                Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, HUD_WHITE, 1);
        Imgproc.putText(frame, "[H] HUD  [P] Pause  [Q] Quit", new Point(10, 85),
                Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, HUD_AMBER, 1);

        String status;
        Scalar statusColor;
        if (paused) {
            status = "// PAUSED";       statusColor = HUD_AMBER;
        } else if (primary == null) {
            status = "// SCANNING...";  statusColor = HUD_AMBER;
        } else {
            double cx = primary.box.x + primary.box.width  / 2.0;
            double cy = primary.box.y + primary.box.height / 2.0;
            double dist = Math.hypot(cx - w / 2.0, cy - h / 2.0);
            if (dist < 40) { status = "// LOCKED ON";  statusColor = HUD_GREEN; }
            else           { status = "// TRACKING";   statusColor = HUD_CYAN;  }
        }
        Imgproc.putText(frame, status, new Point(w - 220, 25),
                Imgproc.FONT_HERSHEY_DUPLEX, 0.6, statusColor, 1);
        if (primary != null) {
            Imgproc.putText(frame, "LOCK: " + primary.name, new Point(w - 220, 48),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, HUD_WHITE, 1);
        }

        if (useServo) {
            Imgproc.putText(frame,
                    String.format("PAN %5.1f°  TILT %5.1f°", servos.getPan(), servos.getTilt()),
                    new Point(10, h - 14), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, HUD_CYAN, 1);
        } else {
            Imgproc.putText(frame, "SERVO DISABLED",
                    new Point(10, h - 14), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, HUD_AMBER, 1);
        }
        Imgproc.putText(frame, "FRAME #" + frameNum,
                new Point(w - 130, h - 14), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, HUD_WHITE, 1);

        int cx = w / 2, cy = h / 2;
        Imgproc.line(frame, new Point(cx - 12, cy), new Point(cx - 4, cy), HUD_CYAN, 1);
        Imgproc.line(frame, new Point(cx + 4,  cy), new Point(cx + 12, cy), HUD_CYAN, 1);
        Imgproc.line(frame, new Point(cx, cy - 12), new Point(cx, cy - 4), HUD_CYAN, 1);
        Imgproc.line(frame, new Point(cx, cy + 4),  new Point(cx, cy + 12), HUD_CYAN, 1);
        Imgproc.circle(frame, new Point(cx, cy), 2, HUD_CYAN, -1);

        int scanY = (int) ((Math.sin(frameNum * 0.04) * 0.5 + 0.5) * (h - 140)) + 100;
        Imgproc.line(frame, new Point(0, scanY), new Point(w, scanY), SCAN_LINE, 1);
    }
}
