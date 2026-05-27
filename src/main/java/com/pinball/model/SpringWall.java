package com.pinball.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SpringWall extends Wall {
    private double kickForce = 800.0; // 自訂固定彈射力道

    public SpringWall(double startX, double startY, double endX, double endY) {
        super(startX, startY, endX, endY, 1.0);
    }

    public SpringWall(double startX, double startY, double endX, double endY, double elasticity) {
        super(startX, startY, endX, endY, elasticity);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(Color.web("#f38ba8")); 
        gc.setLineWidth(4.0);
        gc.strokeLine(getStartX(), getStartY(), getEndX(), getEndY());
    }

    public double getKickForce() {
        return kickForce;
    }

    public void setKickForce(double kickForce) {
        this.kickForce = kickForce;
    }
}