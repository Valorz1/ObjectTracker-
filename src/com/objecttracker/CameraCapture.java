package com.objecttracker;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class CameraCapture {
    public static final int DEFAULT_WIDTH  = 640;
    public static final int DEFAULT_HEIGHT = 480;

    private final VideoCapture camera;
    private final boolean opened;
    private final int width;
    private final int height;

    public CameraCapture(int deviceIndex) {
        this(deviceIndex, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public CameraCapture(int deviceIndex, int requestedWidth, int requestedHeight) {
        OpenCVLoader.load();
        camera = open(deviceIndex);
        if (camera.isOpened()) {
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH,  requestedWidth);
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, requestedHeight);
            // The camera may not support the size we asked for, so read back what it chose.
            width  = (int) camera.get(Videoio.CAP_PROP_FRAME_WIDTH);
            height = (int) camera.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            opened = true;
            System.out.printf("Camera %d opened at %dx%d (requested %dx%d)%n",
                    deviceIndex, width, height, requestedWidth, requestedHeight);
        } else {
            System.out.println("No camera found at index " + deviceIndex + " - check connection!");
            System.out.println("Tip: run with --list-cameras to see which indexes are available.");
            width = 0;
            height = 0;
            opened = false;
        }
    }

    // DirectShow on Windows, AVFoundation on macOS, then OpenCV's automatic choice.
    private static VideoCapture open(int deviceIndex) {
        int preferred = preferredBackend();
        if (preferred != Videoio.CAP_ANY) {
            VideoCapture cap = new VideoCapture(deviceIndex, preferred);
            if (cap.isOpened()) {
                return cap;
            }
            cap.release();
        }
        return new VideoCapture(deviceIndex, Videoio.CAP_ANY);
    }

    private static int preferredBackend() {
        String override = System.getProperty("objecttracker.camera.backend", "").trim().toLowerCase();
        switch (override) {
            case "dshow"        -> { return Videoio.CAP_DSHOW; }
            case "msmf"         -> { return Videoio.CAP_MSMF; }
            case "avfoundation" -> { return Videoio.CAP_AVFOUNDATION; }
            case "v4l2"         -> { return Videoio.CAP_V4L2; }
            case "any"          -> { return Videoio.CAP_ANY; }
            default             -> { }
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) return Videoio.CAP_DSHOW;
        if (os.contains("mac")) return Videoio.CAP_AVFOUNDATION;
        return Videoio.CAP_ANY;
    }

    public static void listCameras(int maxIndex) {
        OpenCVLoader.load();
        System.out.println("Looking for cameras (this can take a few seconds)...");
        int found = 0;
        for (int i = 0; i < maxIndex; i++) {
            VideoCapture cap = open(i);
            if (cap.isOpened()) {
                Mat frame = new Mat();
                boolean gotFrame = cap.read(frame) && !frame.empty();
                System.out.printf("  --camera %d : %dx%d%s%n", i,
                        (int) cap.get(Videoio.CAP_PROP_FRAME_WIDTH),
                        (int) cap.get(Videoio.CAP_PROP_FRAME_HEIGHT),
                        gotFrame ? "" : "  (opened, but no picture yet - is Camo Studio streaming?)");
                frame.release();
                found++;
            }
            cap.release();
        }
        if (found == 0) {
            System.out.println("  No cameras found.");
        } else {
            System.out.println("Your built-in webcam is usually 0; Camo is usually the next number up.");
        }
    }

    public boolean isOpened() { return opened; }
    public int getWidth()     { return width; }
    public int getHeight()    { return height; }

    public Mat getFrame() {
        Mat frame = new Mat();
        if (opened) camera.read(frame);
        return frame;
    }

    public void release() {
        if (camera != null) camera.release();
    }
}