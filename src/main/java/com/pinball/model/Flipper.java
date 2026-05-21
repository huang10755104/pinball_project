package com.pinball.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Flipper extends GameObject {
    private final double pivotX;
    private final double pivotY;
    private final double length;
    private final double restAngle;
    private final double activeAngle;
    private final double angularSpeed;

    private double currentAngle;
    private double previousAngle;
    private double targetAngle;
    private double angularVelocity;

    public Flipper(double pivotX, double pivotY, double length, double restAngle, double activeAngle) {
        this(pivotX, pivotY, length, restAngle, activeAngle, 8.0, 0.92);
    }

    public Flipper(
            double pivotX,
            double pivotY,
            double length,
            double restAngle,
            double activeAngle,
            double angularSpeed,
            double elasticity) {
        super(elasticity);
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.length = length;
        this.restAngle = restAngle;
        this.activeAngle = activeAngle;
        this.angularSpeed = angularSpeed;
        this.currentAngle = restAngle;
        this.previousAngle = restAngle;
        this.targetAngle = restAngle;
    }

    public void update(double deltaTime) {
        if (deltaTime <= 0.0) {
            angularVelocity = 0.0;
            return;
        }

        previousAngle = currentAngle;
        double angleDelta = targetAngle - currentAngle;
        double step = angularSpeed * deltaTime;
        if (Math.abs(angleDelta) <= step) {
            currentAngle = targetAngle;
            angularVelocity = (currentAngle - previousAngle) / (deltaTime * 1.1);
            return;
        }

        currentAngle += Math.copySign(step, angleDelta);
        angularVelocity = (currentAngle - previousAngle) / (deltaTime * 1.1);
    }

    public void extend() {
        targetAngle = activeAngle;
    }

    public void retract() {
        targetAngle = restAngle;
    }

    public boolean isExtending() {
        return Double.compare(targetAngle, activeAngle) == 0;
    }

    public double getAngularVelocity() {
        return angularVelocity;
    }

    public double getPivotX() {
        return pivotX;
    }

    public double getPivotY() {
        return pivotY;
    }

    public double getCurrentAngle() {
        return currentAngle;
    }

    public double getTargetAngle() {
        return targetAngle;
    }

    public double getEndX() {
        return pivotX + Math.cos(currentAngle) * length;
    }

    public double getEndY() {
        return pivotY + Math.sin(currentAngle) * length;
    }

    public double getLength() {
        return length;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(Color.WHITESMOKE);
        gc.setLineWidth(10.0);
        gc.strokeLine(pivotX, pivotY, getEndX(), getEndY());
        gc.setFill(Color.DIMGRAY);
        gc.fillOval(pivotX - 8.0, pivotY - 8.0, 16.0, 16.0);
    }
}