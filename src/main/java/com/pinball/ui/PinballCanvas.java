package com.pinball.ui;

import com.pinball.core.PinballPhysicsEngine;
import com.pinball.core.Renderable;
import com.pinball.model.Ball;
import com.pinball.model.GameObject;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PinballCanvas extends Canvas implements Renderable {
    private PinballPhysicsEngine physicsEngine;

    public PinballCanvas() {
        this(800, 600);
    }

    public PinballCanvas(double width, double height) {
        super(width, height);
    }

    public void setPhysicsEngine(PinballPhysicsEngine physicsEngine) {
        this.physicsEngine = physicsEngine;
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (physicsEngine == null) {
            return;
        }

        gc.setFill(Color.web("#102030"));
        gc.fillRect(0.0, 0.0, getWidth(), getHeight());

        for (GameObject gameObject : physicsEngine.getCollisionObjects()) {
            gameObject.draw(gc);
        }

        for (Ball ball : physicsEngine.getBalls()) {
            ball.draw(gc);
        }
    }
}
