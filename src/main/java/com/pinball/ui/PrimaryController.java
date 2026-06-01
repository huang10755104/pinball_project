package com.pinball.ui;

import com.pinball.core.GameLoop;
import com.pinball.core.PinballPhysicsEngine;
import com.pinball.core.SoundManager;
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
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class PrimaryController {
    // UI
    @FXML private Label scoreLabel;
    @FXML private Label livesLabel;
    @FXML private VBox gameOverOverlay;
    @FXML private Label finalScoreLabel;
    private int score = 0;
    private int lives = 3;
    
    @FXML // Main Game 
    private StackPane canvasContainer;
    private PinballCanvas pinballCanvas;
    private PinballPhysicsEngine physicsEngine;
    private GameLoop gameLoop;
    private Flipper leftFlipper;
    private Flipper rightFlipper;

    // 🌟 核心新增：動態防落閘門變數與狀態標記
    private Wall dynamicGateWall = null;
    private boolean isGateClosed = false;
    // 蓄力發射變數
    private double chargePower = 0;
    private boolean isCharging = false;
    private final double MAX_CHARGE = 1400.0; // 宇宙級推力，保證衝破屋頂

    public Rectangle plungerBlock;

    private static final SoundManager sound = new SoundManager();

    @FXML
    private void initialize() {
        startGame();
    }

    @FXML
    private void startGame() {
        score = 0;
        lives = 3;
        scoreLabel.setText("Score: " + score);
        livesLabel.setText("Balls: " + lives);
        gameOverOverlay.setVisible(false);

        // 重置動態閘門狀態
        dynamicGateWall = null;
        isGateClosed = false;

        physicsEngine = new PinballPhysicsEngine();
        physicsEngine.setSoundManager(sound);
        physicsEngine.setOnScoreAdded(this::addScore);

        double leftWallX = 5.0;
        double rightOuterWallX = 385.0;

        double playableWidth = rightOuterWallX - leftWallX; // 380
        double centerX = leftWallX + (playableWidth / 2.0); // 195.0

        double startY = 150.0;
        double radiusX = playableWidth / 2.0;     // 190.0
        double radiusY = 140.0;                   // 圓潤拱頂

        int segments = 50;
        double lastX = leftWallX;
        double lastY = startY;

        for (int i = 1; i <= segments; i++) {
            double angleRad = Math.toRadians(180.0 - (180.0 / segments) * i);
            double nextX = centerX + radiusX * Math.cos(angleRad);
            double nextY = startY - radiusY * Math.sin(angleRad);
            physicsEngine.addCollisionObject(new Wall(lastX, lastY, nextX, nextY));

            lastX = nextX;
            lastY = nextY;
        }

        // 基礎外牆結構
        physicsEngine.addCollisionObject(new Wall(5.0, 150.0, 5.0, 432.0));     // 左側外牆
        physicsEngine.addCollisionObject(new Wall(385.0, 150.0, 385.0, 550.0));  // 右側最外牆
        physicsEngine.addCollisionObject(new Wall(345.0, 150.0, 345.0, 432.0));  // 通道固定內牆

        // 內外球道分隔島
        physicsEngine.addCollisionObject(new Wall(30, 320, 30, 380));
        physicsEngine.addCollisionObject(new Wall(320, 320, 320, 380));

        // 左側三角彈弓
        physicsEngine.addCollisionObject(new Wall(85, 330, 60, 400));
        physicsEngine.addCollisionObject(new Wall(60, 400, 95, 430));
        Wall leftSlingshot = new Wall(95, 430, 85, 330);
        leftSlingshot.setBounciness(1.2);
        physicsEngine.addCollisionObject(leftSlingshot);

        // 右側三角彈弓
        physicsEngine.addCollisionObject(new Wall(265, 330, 290, 400));
        physicsEngine.addCollisionObject(new Wall(290, 400, 255, 430));
        Wall rightSlingshot = new Wall(255, 430, 265, 330);
        rightSlingshot.setBounciness(1.2);
        physicsEngine.addCollisionObject(rightSlingshot);

        // 底部漏斗球道
        physicsEngine.addCollisionObject(new Wall(5, 390, 105, 474));
        physicsEngine.addCollisionObject(new Wall(345, 390, 245, 474));

        // 發射通道下段固定引導線
        physicsEngine.addCollisionObject(new Wall(360, 180, 360, 550));

        dynamicGateWall = new Wall(345.0, 150.0, 385.0, 150.0);
        dynamicGateWall.setActive(false);
        physicsEngine.addCollisionObject(dynamicGateWall);

        // 圓形彈簧旁的牆
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
        Ball ball = new Ball(365.0, 500.0, 8.0);
        ball.setVelocityX(0.0);
        ball.setVelocityY(0.0);
        physicsEngine.addBall(ball);

        physicsEngine.addCollisionObject(new Bumper(150.0, 150.0, 15.0));
        physicsEngine.addCollisionObject(new Bumper(200.0, 200.0, 15.0));
        physicsEngine.addCollisionObject(new Bumper(250.0, 130.0, 15.0, 0.92));

        leftFlipper = new Flipper(110.0, 480.0, 60.0, 0.48, -0.85, 10.5, 0.95);
        rightFlipper = new Flipper(240.0, 480.0, 60.0, Math.PI - 0.48, Math.PI + 0.85, 10.5, 0.95);
        physicsEngine.addCollisionObject(leftFlipper);
        physicsEngine.addCollisionObject(rightFlipper);

        pinballCanvas = new PinballCanvas(400, 550);
        pinballCanvas.setPhysicsEngine(physicsEngine);

        plungerBlock = new Rectangle(30, 20, Color.web("#6c7086"));
        plungerBlock.setTranslateX(165);
        plungerBlock.setTranslateY(240);

        canvasContainer.getChildren().clear(); // 清理舊元件避免重疊
        canvasContainer.getChildren().addAll(pinballCanvas, plungerBlock);

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

                if (chuteBall != null) {
                    // 當球衝入主戰場，且閘門還沒關閉時
                    if (!isGateClosed && (chuteBall.getPositionY() < 140.0 || chuteBall.getPositionX() < 340.0)) {
                        isGateClosed = true;

                        if (dynamicGateWall != null) {
                            dynamicGateWall.setActive(true); // 💥 激活牆壁！從此死死堵住通道出口
                        }
                        System.out.println("💥 發射通道已偵測球通過，防落閘門已自動鎖定！");
                    }
                }

                if (isCharging) {
                    double visualOffset = chargePower * 0.04;
                    plungerBlock.setTranslateY(240 + visualOffset);

                    if (inChute && chuteBall.getPositionY() >= 500) {
                        chuteBall.setPositionY(500 + visualOffset);
                        chuteBall.setVelocityY(0);
                    }
                } else {
                    plungerBlock.setTranslateY(240);

                    if (inChute && chargePower == 0 && chuteBall != null && chuteBall.getPositionY() >= 500 && chuteBall.getVelocityY() > 0) {
                        chuteBall.setPositionY(500);
                        chuteBall.setVelocityY(0);
                    }
                }
                if (chuteBall != null) {
                    if (chuteBall.getPositionY() > 600 || chuteBall.getPositionY() < -100 || chuteBall.getPositionX() < -50 || chuteBall.getPositionX() > 450) {

                        // 🌟 死球重生時，將閘門設回失效，下一顆球才能順利打上去
                        if (isGateClosed && dynamicGateWall != null) {
                            dynamicGateWall.setActive(false); // 🔓 閘門解除、變回穿透狀態
                            isGateClosed = false;
                            System.out.println("🔓 球已洗掉，發射通道閘門重新開啟。");
                        }

                        chuteBall.setPositionX(365.0);
                        chuteBall.setPositionY(500.0);
                        chuteBall.setVelocityX(0);
                        chuteBall.setVelocityY(0);

                        lives--;
                        livesLabel.setText("Balls: " + lives);
                        if (lives == 0) {
                            handleGameOver();
                        }
                    }
                }
            }
        };
        gameLoop.start();
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.SHIFT || event.getCode() == KeyCode.LEFT) {
            if (!leftFlipper.isExtending()) {
                leftFlipper.extend();
                sound.playFlipper();
            }
        }
        if (event.getCode() == KeyCode.SHIFT || event.getCode() == KeyCode.RIGHT) {
            if (!rightFlipper.isExtending()) {
                rightFlipper.extend();
                sound.playFlipper();
            }
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

    private void handleGameOver() {
        gameLoop.stop(); 
        finalScoreLabel.setText("Final Score: " + score);
        gameOverOverlay.setVisible(true);
    }

    @FXML
    private void restartGame() {
        startGame(); // 重新呼叫初始化方法
    }
}