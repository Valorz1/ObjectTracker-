package com.objecttracker;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class TrackingDisplay {

    private static final Scalar LABEL_TEXT = new Scalar(20, 20, 20);

    public void drawDetection(Mat frame, ColorTracker.Detection d,
                              boolean isPrimary, int frameNum) {
        if (d == null) return;
        Rect r = d.box;
        Scalar color = d.displayColor;
        int thickness = isPrimary ? 2 : 1;

        // Sci-fi corner brackets at each corner of the bounding box
        int len = Math.max(10, Math.min(r.width, r.height) / 5);
        int x1 = r.x, y1 = r.y, x2 = r.x + r.width, y2 = r.y + r.height;

        Imgproc.line(frame, new Point(x1, y1), new Point(x1 + len, y1), color, thickness);
        Imgproc.line(frame, new Point(x1, y1), new Point(x1, y1 + len), color, thickness);
        Imgproc.line(frame, new Point(x2 - len, y1), new Point(x2, y1), color, thickness);
        Imgproc.line(frame, new Point(x2, y1), new Point(x2, y1 + len), color, thickness);
        Imgproc.line(frame, new Point(x1, y2 - len), new Point(x1, y2), color, thickness);
        Imgproc.line(frame, new Point(x1, y2), new Point(x1 + len, y2), color, thickness);
        Imgproc.line(frame, new Point(x2 - len, y2), new Point(x2, y2), color, thickness);
        Imgproc.line(frame, new Point(x2, y2 - len), new Point(x2, y2), color, thickness);

        Point center = new Point(r.x + r.width / 2.0, r.y + r.height / 2.0);
        Imgproc.circle(frame, center, isPrimary ? 5 : 3, color, -1);

        if (isPrimary) {
            int ring = 9 + (int) (Math.sin(frameNum * 0.2) * 3);
            Imgproc.circle(frame, center, ring, color, 1);
        }

        String label = d.name + (isPrimary ? "  [PRIMARY]" : "");
        int[] baseline = new int[1];
        Size ts = Imgproc.getTextSize(label, Imgproc.FONT_HERSHEY_DUPLEX, 0.5, 1, baseline);
        Point pos = new Point(r.x, Math.max(ts.height + 6, r.y - 6));
        Imgproc.rectangle(frame,
                new Point(pos.x - 2, pos.y - ts.height - 4),
                new Point(pos.x + ts.width + 4, pos.y + 4),
                color, -1);
        Imgproc.putText(frame, label, pos,
                Imgproc.FONT_HERSHEY_DUPLEX, 0.5, LABEL_TEXT, 1);

        Imgproc.putText(frame, String.format("A:%d", (int) d.area),
                new Point(r.x, Math.min(frame.rows() - 4, r.y + r.height + 14)),
                Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, color, 1);
    }

    public Point getCenter(Rect box) {
        return new Point(box.x + box.width / 2.0, box.y + box.height / 2.0);
    }
}
