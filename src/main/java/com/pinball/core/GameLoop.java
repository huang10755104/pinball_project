package com.pinball.core;

import com.pinball.ui.PinballCanvas;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameLoop extends AnimationTimer {
    private final PhysicsEngine physicsEngine;
    private final PinballCanvas canvas;
    private long lastTime;
    
    private final double TIME_STEP = 1.0 / 240.0; 
    private double accumulator = 0.0;

    public GameLoop(PhysicsEngine physicsEngine, PinballCanvas canvas) {
        this.physicsEngine = physicsEngine;
        this.canvas = canvas;
    }

    @Override
    public void handle(long now) {
        if (lastTime == 0L) {
            lastTime = now;
            return;
        }

        double deltaTime = (now - lastTime) / 1_000_000_000.0;
        lastTime = now;

        if (deltaTime > 0.1) {
            deltaTime = 0.016;
        }

        accumulator += deltaTime;

        while (accumulator >= TIME_STEP) {
            physicsEngine.update(TIME_STEP);
            physicsEngine.checkCollision();
            accumulator -= TIME_STEP;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#102030"));
        gc.clearRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());
        canvas.draw(gc);
    }
}