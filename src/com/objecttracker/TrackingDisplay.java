package com.objecttracker;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class TrackingDisplay {

    public void draw(Mat frame, Rect target) {
        if (target == null) return;

        Imgproc.rectangle(frame, target, new Scalar(0, 255, 0), 2);

        Point center = new Point(
                target.x + target.width / 2.0,
                target.y + target.height / 2.0
        );
        Imgproc.circle(frame, center, 5, new Scalar(0, 0, 255), -1);

        Imgproc.putText(frame, "TRACKING",
                new Point(target.x, target.y - 10),
                Imgproc.FONT_HERSHEY_SIMPLEX, 0.6,
                new Scalar(0, 255, 0), 2);
    }

    public Point getCenter(Rect box) {
        return new Point(box.x + box.width / 2.0, box.y + box.height / 2.0);
    }
}