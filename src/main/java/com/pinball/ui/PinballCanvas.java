package com.pinball.ui;

import com.pinball.core.PinballPhysicsEngine;
import com.pinball.core.Renderable;
import com.pinball.model.Ball;
import com.pinball.model.GameObject;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class PinballCanvas extends Canvas implements Renderable {
    private PinballPhysicsEngine physicsEngine;
    private Image backgroundImage;

    public PinballCanvas() {
        this(800, 600);
    }

    public PinballCanvas(double width, double height) {
        super(width, height);
    }

    public void setPhysicsEngine(PinballPhysicsEngine physicsEngine) {
        this.physicsEngine = physicsEngine;
    }

    public void setBackgroundImage(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (physicsEngine == null) {
            return;
        }

        // 繪製背景圖層
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0.0, 0.0, getWidth(), getHeight());
        } else {
            // 預設背景色
            gc.setFill(Color.web("#102030"));
            gc.fillRect(0.0, 0.0, getWidth(), getHeight());
        }

        // 繪製遊戲物件
        for (GameObject gameObject : physicsEngine.getCollisionObjects()) {
            gameObject.draw(gc);
        }

        // 繪製彈珠
        for (Ball ball : physicsEngine.getBalls()) {
            ball.draw(gc);
        }
    }
}
