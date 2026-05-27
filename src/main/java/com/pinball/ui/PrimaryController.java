package com.pinball.ui;

import com.pinball.core.GameLoop;
import com.pinball.core.PinballPhysicsEngine;
import com.pinball.model.Ball;
import com.pinball.model.Bumper;
import com.pinball.model.Wall;
import com.pinball.model.Flipper;

import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class PrimaryController {
    @FXML // UI
    private Label scoreLabel;
    private int score = 0;
    
    @FXML // Main Game 
    private StackPane canvasContainer;
    private PinballCanvas pinballCanvas;
    private PinballPhysicsEngine physicsEngine;
    private GameLoop gameLoop;
    private Flipper leftFlipper;
    private Flipper rightFlipper;

    // 蓄力發射變數
    private double chargePower = 0;
    private boolean isCharging = false;
    private final double MAX_CHARGE = 1400.0; // 宇宙級推力，保證衝破屋頂

    public Rectangle plungerBlock;

    @FXML
    private void initialize() {
        physicsEngine = new PinballPhysicsEngine();
        physicsEngine.setOnScoreAdded(this::addScore);

        // ================== V3 完美軌道版邊界 (一筆畫到底的連續外殼) ==================
        // 1. 遊戲主舞台外圍 + 發射通道外牆 (融合為單一連續牆壁)
        physicsEngine.addCollisionObject(new Wall(5, 432, 5, 150));    // 左側外牆
        physicsEngine.addCollisionObject(new Wall(5, 150, 40, 70));    // 左上圓弧
        physicsEngine.addCollisionObject(new Wall(40, 70, 100, 30));
        physicsEngine.addCollisionObject(new Wall(100, 30, 175, 10));
        physicsEngine.addCollisionObject(new Wall(175, 10, 280, 20));  // 頂部圓弧
        physicsEngine.addCollisionObject(new Wall(280, 20, 345, 50));
        physicsEngine.addCollisionObject(new Wall(345, 50, 385, 90));  // 導流屋頂 (強迫左彎)
        physicsEngine.addCollisionObject(new Wall(385, 90, 385, 150)); // 銜接通道右牆
        physicsEngine.addCollisionObject(new Wall(385, 150, 385, 550));// 通道右牆

        // 2. 發射通道內牆 (兼具遊戲區右側死牆)
        // 注意：高度只蓋到 y=150，上方完全敞開，讓球順利滾入遊戲區！
        physicsEngine.addCollisionObject(new Wall(345, 150, 345, 432));

        // 3. 內外球道分隔島 (拓寬至 30 pixels)
        physicsEngine.addCollisionObject(new Wall(30, 320, 30, 380));
        physicsEngine.addCollisionObject(new Wall(320, 320, 320, 380));

        // 4. 左側三角彈弓
        physicsEngine.addCollisionObject(new Wall(85, 330, 60, 400));
        physicsEngine.addCollisionObject(new Wall(60, 400, 95, 430));
        Wall leftSlingshot = new Wall(95, 430, 85, 330);
        leftSlingshot.setBounciness(1.2); // 💥 高反彈加速
        physicsEngine.addCollisionObject(leftSlingshot);

        // 5. 右側三角彈弓
        physicsEngine.addCollisionObject(new Wall(265, 330, 290, 400));
        physicsEngine.addCollisionObject(new Wall(290, 400, 255, 430));
        Wall rightSlingshot = new Wall(255, 430, 265, 330);
        rightSlingshot.setBounciness(1.2); // 💥 高反彈加速
        physicsEngine.addCollisionObject(rightSlingshot);

        // 6. 底部漏斗球道 (強制延伸穿過擋板軸心)
        physicsEngine.addCollisionObject(new Wall(5, 390, 105, 474));
        physicsEngine.addCollisionObject(new Wall(345, 390, 245, 474));

        // 7. 發射通道 (加寬防卡死)
        physicsEngine.addCollisionObject(new Wall(360, 100, 360, 550));
        physicsEngine.addCollisionObject(new Wall(360, 100, 345, 150));
        // physicsEngine.addCollisionObject(new Wall(400, 100, 400, 550));

        // 8. 發射通道頂部導流屋頂 (防止飛到外太空，強迫彎入主台面)
        // physicsEngine.addCollisionObject(new Wall(400, 100, 360, 30));
        physicsEngine.addCollisionObject(new Wall(300, 30, 280, 50)); // 完美接合頂部圓弧
        
        
        // 9. 圓形彈簧旁的牆
        physicsEngine.addCollisionObject(new Wall(320, 100, 330, 120));
        physicsEngine.addCollisionObject(new Wall(330, 120, 330, 140));
        physicsEngine.addCollisionObject(new Wall(330, 140, 300, 220));
        Wall rightBumperWall = new Wall(300, 220, 260, 260);
        rightBumperWall.setBounciness(1.2);
        physicsEngine.addCollisionObject(rightBumperWall);

        physicsEngine.addCollisionObject(new Wall(80, 140, 80, 220));
        Wall leftBumperWall = new Wall(80, 220, 140, 240);
        leftBumperWall.setBounciness(1.5);
        physicsEngine.addCollisionObject(leftBumperWall);
        physicsEngine.addCollisionObject(new Wall(140, 240, 80, 260));
        physicsEngine.addCollisionObject(new Wall(80, 260, 50, 220));
        physicsEngine.addCollisionObject(new Wall(50, 220, 80, 140));
        // =======================================================

        // 實例化台面動態物件
        Ball ball = new Ball(365.0, 500.0, 8.0); 
        ball.setVelocityX(0.0);
        ball.setVelocityY(0.0); 
        physicsEngine.addBall(ball);
        
        physicsEngine.addCollisionObject(new Bumper(150.0, 150.0, 15.0));
        physicsEngine.addCollisionObject(new Bumper(200.0, 200.0, 15.0));
        physicsEngine.addCollisionObject(new Bumper(250.0, 130.0, 15.0, 0.92));

        leftFlipper = new Flipper(110.0, 480.0, 60.0, 0.48, -0.85, 8.5, 0.95);
        rightFlipper = new Flipper(240.0, 480.0, 60.0, Math.PI - 0.48, Math.PI + 0.85, 8.5, 0.95);
        physicsEngine.addCollisionObject(leftFlipper);
        physicsEngine.addCollisionObject(rightFlipper);

        // UI 渲染層：注意加入 StackPane 的順序與偏移量計算
        pinballCanvas = new PinballCanvas(400, 550);
        pinballCanvas.setPhysicsEngine(physicsEngine);
        
        plungerBlock = new Rectangle(30, 20, Color.web("#6c7086"));
        plungerBlock.setTranslateX(165); // 相對畫布中心向右推
        plungerBlock.setTranslateY(240); // 相對畫布中心向下推
        
        // 必須先加畫布，再加彈簧方塊，方塊才會在最上層
        canvasContainer.getChildren().addAll(pinballCanvas, plungerBlock);

        // 確保視窗載入後綁定鍵盤事件並取得焦點
        Platform.runLater(() -> {
            if (canvasContainer.getScene() != null) {
                canvasContainer.getScene().setOnKeyPressed(this::handleKeyPressed);
                canvasContainer.getScene().setOnKeyReleased(this::handleKeyReleased);
            }
            canvasContainer.requestFocus();
        });

        // 啟動主遊戲迴圈
        gameLoop = new GameLoop(physicsEngine, pinballCanvas) {
            @Override
            public void handle(long now) {
                super.handle(now); 
                updateChargePower(0.016); 

                Ball chuteBall = physicsEngine.getBalls().isEmpty() ? null : physicsEngine.getBalls().get(0);
                boolean inChute = (chuteBall != null && chuteBall.getPositionX() > 340);

                if (isCharging) {
                    // 蓄力時：彈簧下沉，並強制把球往下扯，營造彈簧壓縮的實體感
                    double visualOffset = chargePower * 0.04; // 隨推力最大下沉 56 pixels
                    plungerBlock.setTranslateY(240 + visualOffset);

                    if (inChute && chuteBall.getPositionY() >= 500) {
                        chuteBall.setPositionY(500 + visualOffset);
                        chuteBall.setVelocityY(0); // 抵銷重力，避免球亂彈
                    }
                } else {
                    // 沒在蓄力：彈簧歸位
                    plungerBlock.setTranslateY(240); 

                    // 靜止時的「實體地板」：只要球掉到 y=500，就托住它不讓它穿透
                    if (inChute && chargePower == 0 && chuteBall != null && chuteBall.getPositionY() >= 500 && chuteBall.getVelocityY() > 0) {
                        chuteBall.setPositionY(500);
                        chuteBall.setVelocityY(0);
                    }
                }

                // ================== 死球與異次元重生機制 ==================
                if (chuteBall != null) {
                    if (chuteBall.getPositionY() > 600 || chuteBall.getPositionY() < -100 || chuteBall.getPositionX() < -50 || chuteBall.getPositionX() > 450) {
                        chuteBall.setPositionX(365.0); 
                        chuteBall.setPositionY(500.0);
                        chuteBall.setVelocityX(0);
                        chuteBall.setVelocityY(0);
                    }
                }
            }
        };
        gameLoop.start();
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.SHIFT || event.getCode() == KeyCode.LEFT) {
            leftFlipper.extend();
        }
        if (event.getCode() == KeyCode.SHIFT || event.getCode() == KeyCode.RIGHT) {
            rightFlipper.extend();
        }
        if (event.getCode() == KeyCode.SPACE) {
            isCharging = true;
        }
    }               

    @FXML
    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.SHIFT || event.getCode() == KeyCode.LEFT) {
            leftFlipper.retract();
        }
        if (event.getCode() == KeyCode.SHIFT || event.getCode() == KeyCode.RIGHT) {
            rightFlipper.retract();
        }
        if (event.getCode() == KeyCode.SPACE && isCharging) {
            isCharging = false;
            
            if (!physicsEngine.getBalls().isEmpty()) {
                Ball ball = physicsEngine.getBalls().get(0); 
                // 確保球在發射通道內才給予推力
                if (ball.getPositionX() > 340 && ball.getPositionY() > 400) {
                    double launchVelocity = Math.min(chargePower, MAX_CHARGE);  
                    
                    // 給予微小的 +X 速度，讓球貼著最外側牆壁滑行
                    ball.setVelocityX(20.0);
                    ball.setVelocityY(-launchVelocity); 
                }
            }
            chargePower = 0; // 重置充能
            plungerBlock.setTranslateY(240); // 視覺瞬間歸位
        }
    }

    public void updateChargePower(double deltaTime) {
        if (isCharging) {
            chargePower += 1000.0 * deltaTime; // 縮短蓄力時間
            chargePower = Math.min(chargePower, MAX_CHARGE); // 限制最大充能
        }
    }

    public void addScore(int points) {
        score += points;
        scoreLabel.setText("Score: " + score); 
    }
}