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
import javafx.scene.layout.*;
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

    // dynamic wall
    private Wall dynamicGateWall = null;
    private boolean isGateClosed = false;
    // 蓄力發射變數
    private double chargePower = 0;
    private boolean isCharging = false;
    private final double MAX_CHARGE = 1400.0; // 宇宙級推力，保證衝破屋頂

    public Rectangle plungerBlock;

    private static final SoundManager sound = new SoundManager();

    // bumper random positions
    private final double[][] BUMPER_POSITIONS = {
            {120.0, 150.0},
            {200.0, 250.0},
            {280.0, 150.0},
            {30.0, 180.0},
            {320.0, 250.0},
            {120.0, 360.0},
            {230.0, 360.0},
            {200.0, 70.0},
            {65.0, 290.0},
            {290.0, 290.0}
    };
    // saving bumper locations
    private final Bumper[] activeBumpers = new Bumper[3];

    @FXML
    private void initialize() {
        // 幫分數加上字型大小與霓虹青藍（#00f0ff）的發光效果 (dropshadow)
        scoreLabel.setStyle(
                "-fx-font-family: 'Consolas', 'Monospaced';" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #00f0ff;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,240,255,0.4), 8, 0, 0, 0);"
        );

        livesLabel.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-text-fill: #ff5555;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(255,85,85,0.3), 8, 0, 0, 0);"
        );


        if (scoreLabel.getParent() instanceof VBox) {
            VBox rightPanel = (VBox) scoreLabel.getParent();

            rightPanel.setStyle("-fx-background-color: #0d1117; -fx-padding: 25; -fx-alignment: TOP_CENTER;");
            rightPanel.setSpacing(25);

            // PINBALL GAME title
            VBox titleBox = new VBox(2);
            titleBox.setAlignment(javafx.geometry.Pos.CENTER);

            Label pLabel = new Label("PINBALL");
            pLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: #ff007f; -fx-effect: dropshadow(three-pass-box, rgba(255,0,127,0.5), 10, 0, 0, 0);");

            Label gLabel = new Label("GAME");
            gLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #00f0ff; -fx-effect: dropshadow(three-pass-box, rgba(0,240,255,0.5), 10, 0, 0, 0);");

            titleBox.getChildren().addAll(pLabel, gLabel);

            // create control hints
            VBox hintsCard = new VBox(10);
            hintsCard.setStyle(
                    "-fx-background-color: #1e1e2e;" +     // 獨立深色卡片背景
                            "-fx-background-radius: 8;" +
                            "-fx-border-color: #313244;" +          // 卡片細暗框
                            "-fx-border-radius: 8;" +
                            "-fx-padding: 15;" +
                            "-fx-fill-width: true;"
            );

            Label cardTitle = new Label("CONTROLS 🎮");
            cardTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f5c2e7;");
            hintsCard.getChildren().add(cardTitle);

            // 加入三行快捷鍵提示
            hintsCard.getChildren().add(createHintRow("發射球：", "長按 SPACE", "#89b4fa"));
            hintsCard.getChildren().add(createHintRow("左撥片：", "◀ 左方向鍵 ", "#74c7ec"));
            hintsCard.getChildren().add(createHintRow("右撥片：", "右方向鍵 ▶", "#74c7ec"));

            // 🌟 5. 重新調整右側面板的排列順序，讓標題在最上面、卡片在最下面
            rightPanel.getChildren().clear();

            // 建立一個彈性墊片，自動把操作卡片推到最右下角
            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            // 依序排列：大標題 -> 分數標籤 -> 生命值標籤 -> 彈性墊片 -> 操作卡片
            rightPanel.getChildren().addAll(titleBox, scoreLabel, livesLabel, spacer, hintsCard);
        }

        // 啟動遊戲
        startGame();
    }

    private HBox createHintRow(String labelText, String keyText, String keyColor) {
        HBox row = new HBox();
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 12px;");

        Region innerSpacer = new Region();
        HBox.setHgrow(innerSpacer, Priority.ALWAYS); // 讓左右推開排整齊

        Label key = new Label(keyText);
        key.setStyle("-fx-text-fill: " + keyColor + "; -fx-font-weight: bold; -fx-font-size: 12px;");

        row.getChildren().addAll(lbl, innerSpacer, key);
        return row;
    }

    @FXML
    private void startGame() {
        score = 0;
        lives = 3;
        scoreLabel.setText("000000");
        livesLabel.setText("Balls: " + lives);
        gameOverOverlay.setVisible(false);

        // 重置動態閘門狀態
        dynamicGateWall = null;
        isGateClosed = false;

        physicsEngine = new PinballPhysicsEngine();
        physicsEngine.setSoundManager(sound);

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
        Ball ball = new Ball(373.0, 500.0, 8.0);
        ball.setVelocityX(0.0);
        ball.setVelocityY(0.0);
        physicsEngine.addBall(ball);


        // initialize bumper position
        activeBumpers[0] = new Bumper(BUMPER_POSITIONS[0][0], BUMPER_POSITIONS[0][1], 15.0);
        activeBumpers[1] = new Bumper(BUMPER_POSITIONS[1][0], BUMPER_POSITIONS[1][1], 15.0);
        activeBumpers[2] = new Bumper(BUMPER_POSITIONS[2][0], BUMPER_POSITIONS[2][1], 15.0);
        // save bumper positions
        physicsEngine.addCollisionObject(activeBumpers[0]);
        physicsEngine.addCollisionObject(activeBumpers[1]);
        physicsEngine.addCollisionObject(activeBumpers[2]);

        leftFlipper = new Flipper(110.0, 480.0, 60.0, 0.48, -0.85, 10.5, 0.95);
        rightFlipper = new Flipper(240.0, 480.0, 60.0, Math.PI - 0.48, Math.PI + 0.85, 10.5, 0.95);
        physicsEngine.addCollisionObject(leftFlipper);
        physicsEngine.addCollisionObject(rightFlipper);

        pinballCanvas = new PinballCanvas(400, 550);
        pinballCanvas.setPhysicsEngine(physicsEngine);

        plungerBlock = new Rectangle(30, 20, Color.web("#6c7086"));
        plungerBlock.setTranslateX(172);
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
                    if (!isGateClosed && (chuteBall.getPositionY() < 140.0 || chuteBall.getPositionX() < 340.0)) {
                        isGateClosed = true;
                        if (dynamicGateWall != null) {
                            dynamicGateWall.setActive(true);
                        }
                    }
                    for (int i = 0; i < activeBumpers.length; i++) {
                        Bumper bumper = activeBumpers[i];
                        // 計算彈珠中心點與 Bumper 中心點的幾何距離
                        double dx = chuteBall.getPositionX() - bumper.getCenterX();
                        double dy = chuteBall.getPositionY() - bumper.getCenterY();
                        double distance = Math.hypot(dx, dy);
                        // checking collision
                        if (distance < 24.5) {
                            addScore(1000);
                            if (distance > 0) {
                                double nx = dx / distance; // 碰撞法向量 X
                                double ny = dy / distance; // 碰撞法向量 Y
                                // 計算球原本的速度大小 (Speed)
                                double currentSpeed = Math.hypot(chuteBall.getVelocityX(), chuteBall.getVelocityY());
                                // 給予一個基礎反彈噴射速度（確保即便是慢速滾入，也會被強力彈開）
                                double bounceSpeed = Math.max(currentSpeed * 1.2, 400.0);
                                // 設定新速度方向：沿著碰撞中心點向外散射
                                chuteBall.setVelocityX(nx * bounceSpeed);
                                chuteBall.setVelocityY(ny * bounceSpeed);
                            }
                            int randomIndex = (int) (Math.random() * BUMPER_POSITIONS.length);
                            double newX = BUMPER_POSITIONS[randomIndex][0];
                            double newY = BUMPER_POSITIONS[randomIndex][1];
                            // checking if the new spot is too close to the other bumpers
                            boolean isOverlapping = false;
                            for (int j = 0; j < activeBumpers.length; j++) {
                                if (i != j) {
                                    double bx = newX - activeBumpers[j].getCenterX();
                                    double by = newY - activeBumpers[j].getCenterY();
                                    if (Math.hypot(bx, by) < 40.0) {
                                        isOverlapping = true;
                                        break;
                                    }
                                }
                            }
                            if (!isOverlapping) {
                                bumper.setCenterX(newX);
                                bumper.setCenterY(newY);
                                chuteBall.setPositionX(chuteBall.getPositionX() + chuteBall.getVelocityX() * 0.016);
                                chuteBall.setPositionY(chuteBall.getPositionY() + chuteBall.getVelocityY() * 0.016);
                            }

                        }
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

                        if (isGateClosed && dynamicGateWall != null) {
                            dynamicGateWall.setActive(false);
                            isGateClosed = false;
                        }

                        chuteBall.setPositionX(373.0);
                        chuteBall.setPositionY(480.0);
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
        scoreLabel.setText(String.format("%06d", score));

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