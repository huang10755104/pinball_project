package com.pinball.core;

import com.pinball.ui.PinballCanvas;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;

public class GameLoop extends AnimationTimer {
    private final PhysicsEngine physicsEngine;
    private final PinballCanvas canvas;
    private long lastTime;

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
        physicsEngine.update(deltaTime);
        physicsEngine.checkCollision();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.draw(gc);

        lastTime = now;
    }
}
