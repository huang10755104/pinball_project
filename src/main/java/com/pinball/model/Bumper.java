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
    private int scoreValue = 1000;
    private int score;
    private Image bumperImage;

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
        RadialGradient neonGrad = new RadialGradient(
                0, 0, centerX, centerY, radius, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE),
                new Stop(0.4, Color.DEEPPINK),
                new Stop(1, Color.TRANSPARENT)
        );

        if (bumperImage != null) {
            // 使用圖像繪製碰撞球
            gc.drawImage(bumperImage, centerX - radius, centerY - radius, diameter, diameter);
        } else {
            gc.setFill(neonGrad);
            gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }
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