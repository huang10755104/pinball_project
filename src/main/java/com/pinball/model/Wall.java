package com.pinball.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Wall extends GameObject {
    private double startX;
    private double startY;
    private double endX;
    private double endY;

    public Wall(double startX, double startY, double endX, double endY) {
        this(startX, startY, endX, endY, 0.85);
    }

    public Wall(double startX, double startY, double endX, double endY, double elasticity) {
        super(elasticity);
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(Color.web("#45475a"));
        gc.setLineWidth(3.0);
        gc.strokeLine(startX, startY, endX, endY);
    }

    public double getStartX() {
        return startX;
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public double getStartY() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    public double getEndX() {
        return endX;
    }

    public void setEndX(double endX) {
        this.endX = endX;
    }

    public double getEndY() {
        return endY;
    }

    public void setEndY(double endY) {
        this.endY = endY;
    }
    // 在 Wall.java 裡面加入反彈係數的屬性與 setter
    private double bounciness = 0.3; // 預設反彈係數
    
    public void setBounciness(double bounciness) {
        this.bounciness = bounciness; 
    }

    // 如果你的物理引擎還需要讀取它，順便補個 getter
    public double getBounciness() {
        return this.bounciness;
    }
}