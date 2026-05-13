package com.objecttracker;

public class ServoController {
    private static final double KP_PAN    = 0.04;
    private static final double KP_TILT   = 0.04;
    private static final double DEADZONE  = 12.0;
    private static final double SMOOTHING = 0.35;

    private int frameCenterX = 320;
    private int frameCenterY = 240;

    private double panAngle  = 90;
    private double tiltAngle = 90;
    private double targetPan  = 90;
    private double targetTilt = 90;

    private long lastLogTime = 0;

    public void setFrameSize(int width, int height) {
        this.frameCenterX = width  / 2;
        this.frameCenterY = height / 2;
    }

    public void adjustToTarget(double targetX, double targetY) {
        double errorX = targetX - frameCenterX;
        double errorY = targetY - frameCenterY;

        if (Math.abs(errorX) > DEADZONE) targetPan  -= errorX * KP_PAN;
        if (Math.abs(errorY) > DEADZONE) targetTilt += errorY * KP_TILT;

        targetPan  = clamp(targetPan,  0, 180);
        targetTilt = clamp(targetTilt, 0, 180);

        // Exponential smoothing keeps the servos from snapping/jittering
        panAngle  += (targetPan  - panAngle)  * SMOOTHING;
        tiltAngle += (targetTilt - tiltAngle) * SMOOTHING;

        long now = System.currentTimeMillis();
        if (now - lastLogTime > 200) {
            System.out.printf("Pan: %5.1f°  Tilt: %5.1f°%n", panAngle, tiltAngle);
            lastLogTime = now;
        }
    }

    public double getPan()  { return panAngle;  }
    public double getTilt() { return tiltAngle; }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
