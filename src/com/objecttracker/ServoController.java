package com.objecttracker;

public class ServoController {
    private static final int FRAME_CENTER_X = 320;
    private static final int FRAME_CENTER_Y = 240;
    private static final double KP = 0.05;

    private double panAngle  = 90;
    private double tiltAngle = 90;

    public void adjustToTarget(double targetX, double targetY) {
        double errorX = targetX - FRAME_CENTER_X;
        double errorY = targetY - FRAME_CENTER_Y;

        panAngle  -= errorX * KP;
        tiltAngle += errorY * KP;

        panAngle  = clamp(panAngle,  0, 180);
        tiltAngle = clamp(tiltAngle, 0, 180);

        System.out.printf("Pan: %.1f°  Tilt: %.1f°%n", panAngle, tiltAngle);
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}