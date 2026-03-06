package com.objecttracker;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        CameraCapture   camera  = new CameraCapture(0);
        ColorTracker    detector = new ColorTracker();
        ServoController servos  = new ServoController();

        System.out.println("Starting tracker... Press Q to quit.");

        while (true) {
            Mat frame = camera.getFrame();
            if (frame.empty()) continue;

            List<Rect> targets = detector.findAllObjects(frame);
            boolean anyFound = false;

            for (int i = 0; i < targets.size(); i++) {
                Rect target = targets.get(i);
                if (target == null) continue;

                anyFound = true;
                double[] color = ColorTracker.COLORS[i];
                String   name  = ColorTracker.COLOR_NAMES[i];

                // Draw box in matching color
                Scalar boxColor = new Scalar(color[8], color[7], color[6]);
                Imgproc.rectangle(frame, target, boxColor, 2);

                // Draw center dot
                Point center = new Point(
                    target.x + target.width  / 2.0,
                    target.y + target.height / 2.0
                );
                Imgproc.circle(frame, center, 6, boxColor, -1);

                // Draw color name label
                Imgproc.putText(frame, name,
                    new Point(target.x, target.y - 10),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.7,
                    boxColor, 2);

                servos.adjustToTarget(center.x, center.y);
            }

            // Show searching if nothing found
            if (!anyFound) {
                Imgproc.putText(frame, "SEARCHING...",
                    new Point(20, 40),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 1.0,
                    new Scalar(0, 255, 255), 2);
            }

            HighGui.imshow("Object Tracker", frame);
            if (HighGui.waitKey(30) == 'q') break;
        }

        camera.release();
    }
}