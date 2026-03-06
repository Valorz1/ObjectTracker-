package com.objecttracker;

import org.opencv.core.*;

public class ObjectTracker {
    private Rect lastKnownBox = null;
    private boolean initialized = false;

    public void init(Mat frame, Rect boundingBox) {
        lastKnownBox = boundingBox;
        initialized = true;
    }

    public Rect update(Mat frame) {
        if (!initialized) return null;
        // Returns last known position
        // ColorTracker will update this each frame
        return lastKnownBox;
    }

    public void updateBox(Rect newBox) {
        if (newBox != null) {
            lastKnownBox = newBox;
        }
    }

    public boolean isInitialized() { return initialized; }
    
    public void reset() { 
        initialized = false; 
        lastKnownBox = null;
    }
}