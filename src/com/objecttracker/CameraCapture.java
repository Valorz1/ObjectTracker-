package com.objecttracker;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;

public class CameraCapture {
    private final VideoCapture camera;

    public CameraCapture(int deviceIndex) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        camera = new VideoCapture(deviceIndex);
        if (!camera.isOpened()) {
            System.out.println("No camera found - check connection!");
            return;
        }
    }

    public Mat getFrame() {
        Mat frame = new Mat();
        camera.read(frame);
        return frame;
    }

    public void release() { camera.release(); }
}