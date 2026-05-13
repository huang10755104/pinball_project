package com.pinball.ui;

import com.pinball.core.GameLoop;
import com.pinball.core.PinballPhysicsEngine;
import com.pinball.model.Ball;
import com.pinball.model.Bumper;
import com.pinball.model.Flipper;
import com.pinball.model.Wall;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

public class PrimaryController {
    @FXML
    private Pane canvasContainer;
    private PinballCanvas pinballCanvas;
    private PinballPhysicsEngine physicsEngine;
    private GameLoop gameLoop;
    private Flipper leftFlipper;
    private Flipper rightFlipper;

    @FXML
    private void initialize() {
        physicsEngine = new PinballPhysicsEngine();

        Ball ball = new Ball(120.0, 120.0, 12.0);
        ball.setVelocityX(180.0);
        ball.setVelocityY(0.0);
        physicsEngine.addBall(ball);

        physicsEngine.addCollisionObject(new Wall(20.0, 20.0, 780.0, 20.0));
        physicsEngine.addCollisionObject(new Wall(780.0, 20.0, 780.0, 580.0));
        physicsEngine.addCollisionObject(new Wall(780.0, 580.0, 20.0, 580.0));
        physicsEngine.addCollisionObject(new Wall(20.0, 580.0, 20.0, 20.0));
        physicsEngine.addCollisionObject(new Bumper(260.0, 220.0, 28.0));
        physicsEngine.addCollisionObject(new Bumper(520.0, 300.0, 32.0, 0.92));

        leftFlipper = new Flipper(280.0, 510.0, 110.0, -0.18, -0.95, 8.5, 0.95);
        rightFlipper = new Flipper(520.0, 510.0, 110.0, Math.PI - 0.18, Math.PI + 0.95, 8.5, 0.95);
        physicsEngine.addCollisionObject(leftFlipper);
        physicsEngine.addCollisionObject(rightFlipper);

        pinballCanvas = new PinballCanvas(800.0, 600.0);
        pinballCanvas.setPhysicsEngine(physicsEngine);
        canvasContainer.getChildren().add(pinballCanvas);

        installKeyHandlers();
        Platform.runLater(canvasContainer::requestFocus);

        gameLoop = new GameLoop(physicsEngine, pinballCanvas);
        gameLoop.start();
    }

    private void installKeyHandlers() {
        canvasContainer.sceneProperty().addListener((observable, previousScene, scene) -> {
            if (scene != null) {
                scene.setOnKeyPressed(this::handleKeyPressed);
                scene.setOnKeyReleased(this::handleKeyReleased);
            }
        });
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.SHIFT || event.getCode() == KeyCode.LEFT) {
            leftFlipper.extend();
        }
        if (event.getCode() == KeyCode.SHIFT || event.getCode() == KeyCode.RIGHT) {
            rightFlipper.extend();
        }
    }

    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.SHIFT || event.getCode() == KeyCode.LEFT) {
            leftFlipper.retract();
        }
        if (event.getCode() == KeyCode.SHIFT || event.getCode() == KeyCode.RIGHT) {
            rightFlipper.retract();
        }
    }

    public Pane getCanvasContainer() {
        return canvasContainer;
    }

    public PinballCanvas getPinballCanvas() {
        return pinballCanvas;
    }

    public PinballPhysicsEngine getPhysicsEngine() {
        return physicsEngine;
    }
}
