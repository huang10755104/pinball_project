package com.pinball.ui;

import com.pinball.core.GameLoop;
import com.pinball.core.PinballPhysicsEngine;
import com.pinball.core.TableBuilder;
import com.pinball.model.Ball;
import com.pinball.model.Bumper;
import com.pinball.model.GameObject;
import com.pinball.model.Wall;

import com.pinball.model.Flipper;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class PrimaryController {
    @FXML
    private StackPane canvasContainer;
    private PinballCanvas pinballCanvas;
    private PinballPhysicsEngine physicsEngine;
    private GameLoop gameLoop;
    private Flipper leftFlipper;
    private Flipper rightFlipper;

    // Plunger manual charge variables
    private double chargePower = 0;
    private boolean isCharging = false;
    private final double MAX_CHARGE = 800.0; // Scaled for velocity directly

    public Rectangle plungerBlock;

    @FXML
    private void initialize() {
        physicsEngine = new PinballPhysicsEngine();

        // 建立桌面幾何物件（Table Builder，根據原始資料的 1:1 縮放）
        TableBuilder tableBuilder = new TableBuilder(400.0, 550.0);
        
        for (Wall w : tableBuilder.buildBoundaryWalls()) {
            physicsEngine.addCollisionObject(w);
        }
        for (GameObject obj : tableBuilder.buildTableGeometries()) {
            physicsEngine.addCollisionObject(obj);
        }

        // 發射通道右移與加寬：使用 400x550 縮放，將球放在通道正中央
        Ball ball = new Ball(380.0, 500.0, 8.0); 
        ball.setVelocityX(0.0);
        ball.setVelocityY(0.0); 
        physicsEngine.addBall(ball);
        
        // 替換 Plunger：新增一個深灰色的 Rectangle 彈簧方塊
        plungerBlock = new Rectangle(30, 20, Color.web("#6c7086"));
        plungerBlock.setTranslateX(180); // Canvas is centered in StackPane. Canvas = 400. Center = 0. 380 = 180 right of center.
        plungerBlock.setTranslateY(250); // Height = 550. Center = 0. Bottom is +275. Place at +250.

        // 新增碰撞球
        physicsEngine.addCollisionObject(new Bumper(130.0, 180.0, 20.0));
        physicsEngine.addCollisionObject(new Bumper(270.0, 180.0, 20.0, 0.92));

        // 初始化翻轉器
        leftFlipper = new Flipper(100.0, 470.0, 60.0, -0.18, -0.95, 8.5, 0.95);
        rightFlipper = new Flipper(250.0, 470.0, 60.0, Math.PI - 0.18, Math.PI + 0.95, 8.5, 0.95);
        physicsEngine.addCollisionObject(leftFlipper);
        physicsEngine.addCollisionObject(rightFlipper);

        // 初始化畫布，嚴格限制為 400x550 
        pinballCanvas = new PinballCanvas(400.0, 550.0);
        pinballCanvas.setPhysicsEngine(physicsEngine);
        canvasContainer.getChildren().addAll(pinballCanvas, plungerBlock);

        installKeyHandlers();
        Platform.runLater(canvasContainer::requestFocus);

        // 啟動遊戲迴圈
        gameLoop = new GameLoop(physicsEngine, pinballCanvas) {
            @Override
            public void handle(long now) {
                super.handle(now);
                if (isCharging) {
                    chargePower += 15.0; // Charge speed
                    if (chargePower > MAX_CHARGE) {
                        chargePower = MAX_CHARGE;
                    }
                    // 視覺回饋：發射台隨蓄力往下沉
                    plungerBlock.setTranslateY(250 + (chargePower * 0.05));
                } else if (chargePower == 0) {
                    plungerBlock.setTranslateY(230); // reset visually
                }
            }
        };
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
        if (event.getCode() == KeyCode.SPACE) {
            // 開始蓄力
            isCharging = true;
        }
    }

    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.SHIFT || event.getCode() == KeyCode.LEFT) {
            leftFlipper.retract();
        }
        if (event.getCode() == KeyCode.SHIFT || event.getCode() == KeyCode.RIGHT) {
            rightFlipper.retract();
        }
        if (event.getCode() == KeyCode.SPACE) {
            // 釋放發射器
            isCharging = false;
            if (physicsEngine.getBalls().size() > 0) {
                Ball ball = physicsEngine.getBalls().get(0);
                // 如果此時彈珠位於發射通道內
                if (ball.getPositionX() > 345 && ball.getPositionY() > 400) {
                    ball.setVelocityY(-chargePower);
                }
            }
            chargePower = 0;
            plungerBlock.setTranslateY(230);
        }
    }

    public StackPane getCanvasContainer() {
        return canvasContainer;
    }

    public PinballCanvas getPinballCanvas() {
        return pinballCanvas;
    }

    public PinballPhysicsEngine getPhysicsEngine() {
        return physicsEngine;
    }
}
