package com.pinball.model;

import com.pinball.core.Renderable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Ball implements Renderable {
    private static final double DEFAULT_RADIUS = 15.0;
    private static final double DEFAULT_GRAVITY = 900.0;

    private double previousPositionX;
    private double previousPositionY;
    private double positionX;
    private double positionY;
    private double velocityX;
    private double velocityY;
    private double accelerationX;
    private double accelerationY;
    private double radius;
    private Image ballImage;

    public Ball(double positionX, double positionY) {
        this(positionX, positionY, DEFAULT_RADIUS);
    }

    public Ball(double positionX, double positionY, double radius) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.previousPositionX = positionX;
        this.previousPositionY = positionY;
        this.radius = radius;
        this.accelerationY = DEFAULT_GRAVITY;
    }

    public void update(double deltaTime) {
        if (deltaTime <= 0.0) {
            return;
        }

        previousPositionX = positionX;
        previousPositionY = positionY;
        positionX += velocityX * deltaTime + 0.5 * accelerationX * deltaTime * deltaTime;
        positionY += velocityY * deltaTime + 0.5 * accelerationY * deltaTime * deltaTime;
        velocityX += accelerationX * deltaTime;
        velocityY += accelerationY * deltaTime;
    }

    @Override
    public void draw(javafx.scene.canvas.GraphicsContext gc) {
        double diameter = radius * 2;
        if (ballImage != null) {
            gc.drawImage(ballImage, positionX - radius, positionY - radius, diameter, diameter);
            return;
        }

        // 建立一個跟著球心移動的放射漸層
        javafx.scene.paint.RadialGradient ballGrad = new javafx.scene.paint.RadialGradient(
                -0.2, -0.2, positionX, positionY, radius, false, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.WHITE),       // 球心反光點
                new javafx.scene.paint.Stop(0.6, javafx.scene.paint.Color.GOLD),      // 主體金色
                new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.DARKGOLDENROD) // 邊緣陰影深金色
        );

        gc.setFill(ballGrad);
        gc.fillOval(positionX - radius, positionY - radius, diameter, diameter);
    }

    public void setBallImage(Image ballImage) {
        this.ballImage = ballImage;
    }

    public Image getBallImage() {
        return ballImage;
    }

    public double getPositionX() {
        return positionX;
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
        this.previousPositionX = positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
        this.previousPositionY = positionY;
    }

    public double getPreviousPositionX() {
        return previousPositionX;
    }

    public double getPreviousPositionY() {
        return previousPositionY;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public double getAccelerationX() {
        return accelerationX;
    }

    public void setAccelerationX(double accelerationX) {
        this.accelerationX = accelerationX;
    }

    public double getAccelerationY() {
        return accelerationY;
    }

    public void setAccelerationY(double accelerationY) {
        this.accelerationY = accelerationY;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}