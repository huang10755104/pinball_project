package com.pinball.ui;

import com.pinball.core.PinballPhysicsEngine;
import com.pinball.core.Renderable;
import com.pinball.model.Ball;
import com.pinball.model.GameObject;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

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
    private void drawCyberpanes(GraphicsContext gc) {
        gc.setLineWidth(2.0);

        LinearGradient slingNeonFill = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#00f0ff", 0.35)),
                new Stop(1, Color.web("#004466", 0.60))
        );

        double[] leftSlingX = { 85.0, 60.0, 95.0 };
        double[] leftSlingY = { 330.0, 400.0, 430.0 };

        gc.setFill(slingNeonFill);
        gc.fillPolygon(leftSlingX, leftSlingY, 3);

        gc.setStroke(Color.web("#00f0ff", 0.8));
        gc.strokePolygon(leftSlingX, leftSlingY, 3);


        double[] rightSlingX = { 265.0, 290.0, 255.0 };
        double[] rightSlingY = { 330.0, 400.0, 430.0 };

        gc.setFill(slingNeonFill);
        gc.fillPolygon(rightSlingX, rightSlingY, 3);

        gc.setStroke(Color.web("#00f0ff", 0.8)); // 邊框亮度同步加強
        gc.strokePolygon(rightSlingX, rightSlingY, 3);



        double[] leftBumperBlockX = { 80.0,  80.0,  140.0, 80.0,  50.0 };
        double[] leftBumperBlockY = { 140.0, 220.0, 240.0, 260.0, 220.0 };

        gc.setFill(slingNeonFill);
        gc.fillPolygon(leftBumperBlockX, leftBumperBlockY, 5);

        gc.setStroke(Color.web("#ff007f", 0.8));
        gc.strokePolygon(leftBumperBlockX, leftBumperBlockY, 5);
    }
    @Override
    public void draw(GraphicsContext gc) {
        if (physicsEngine == null) {
            return;
        }


        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0.0, 0.0, getWidth(), getHeight());
        } else {
            // background
            gc.setFill(Color.web("#0d1117"));
            gc.fillRect(0.0, 0.0, getWidth(), getHeight());
            gc.setStroke(Color.web("#161b22", 0.3));
            gc.setLineWidth(1.0);
            for (int x = 0; x < getWidth(); x += 30) {
                gc.strokeLine(x, 0, x, getHeight());
            }
            for (int y = 0; y < getHeight(); y += 30) {
                gc.strokeLine(0, y, getWidth(), y);
            }
        }
        drawCyberpanes(gc);
        // 3. 繪製遊戲環境物件（例如牆壁、Bumper 等）
        for (GameObject gameObject : physicsEngine.getCollisionObjects()) {
            if (gameObject.isActive()) {
                gameObject.draw(gc);
            }
        }

        // 4. 繪製彈珠
        for (Ball ball : physicsEngine.getBalls()) {
            ball.draw(gc);
        }

    }
}