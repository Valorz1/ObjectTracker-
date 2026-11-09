package com.objecttracker;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class CameraCapture {
    private final VideoCapture camera;
    private final boolean opened;

    public CameraCapture(int deviceIndex) {
        OpenCVLoader.load();
        camera = new VideoCapture(deviceIndex);
        if (camera.isOpened()) {
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH,  640);
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
            opened = true;
        } else {
            System.out.println("No camera found - check connection!");
            opened = false;
        }
    }

    public boolean isOpened() { return opened; }

    public Mat getFrame() {
        Mat frame = new Mat();
        if (opened) camera.read(frame);
        return frame;
    }

    public void release() {
        if (camera != null) camera.release();
    }
}
