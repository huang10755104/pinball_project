package com.pinball.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Bumper extends GameObject {
    private double centerX;
    private double centerY;
    private double radius;
    private int scoreValue = 1000;
    private int score;

    public Bumper(double centerX, double centerY, double radius) {
        this(centerX, centerY, radius, 0.9);
    }

    public Bumper(double centerX, double centerY, double radius, double elasticity) {
        super(elasticity);
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }

    @Override
    public void draw(GraphicsContext gc) {
        double diameter = radius * 2.0;
        gc.setFill(Color.ORANGERED);
        gc.fillOval(centerX - radius, centerY - radius, diameter, diameter);
        gc.setStroke(Color.WHITE);
        gc.strokeOval(centerX - radius, centerY - radius, diameter, diameter);
    }

    public void registerHit() {
        score += scoreValue;
    }

    public double getCenterX() {
        return centerX;
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(int scoreValue) {
        this.scoreValue = scoreValue;
    }

    public int getScore() {
        return score;
    }
}