package com.pinball.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

public class Bumper extends GameObject {
    private double centerX;
    private double centerY;
    private double radius;
    private int score;
    private Image bumperImage;
    private Color bumperColor = Color.web("#782323"); // 預設青銅色
    private int scoreValue = 100;
    private String typeName = "Bronze";
    public Bumper(double centerX, double centerY, double radius) {
        this(centerX, centerY, radius, 0.9);
    }

    public Bumper(double centerX, double centerY, double radius, double elasticity) {
        super(elasticity);
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }
    public void setBumperType(String name, Color color, int score) {
        this.typeName = name;
        this.bumperColor = color;
        this.scoreValue = score;
    }



    @Override
    public void draw(GraphicsContext gc) {

        gc.setStroke(bumperColor);
        gc.setLineWidth(3.0);
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        gc.setFill(bumperColor.deriveColor(0, 1, 1, 0.25));
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        gc.setFill(bumperColor);
        gc.fillOval(centerX - 3, centerY - 3, 6, 6);
    }

    public void setBumperImage(Image bumperImage) {
        this.bumperImage = bumperImage;
    }

    public Image getBumperImage() {
        return bumperImage;
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