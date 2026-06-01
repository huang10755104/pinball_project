package com.pinball.ui;

import com.pinball.core.GameLoop;
import com.pinball.core.PinballPhysicsEngine;
import com.pinball.core.SoundManager;
import com.pinball.core.TableBuilder;
import com.pinball.model.Ball;
import com.pinball.model.Flipper;
import com.pinball.model.GameObject;
import com.pinball.model.Wall;

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
    private static final double CANVAS_WIDTH = 400.0;
    private static final double CANVAS_HEIGHT = 550.0;

    public Rectangle plungerBlock;

    private static final SoundManager sound = new SoundManager();

    @FXML
    private void initialize() {
        // adding score
        scoreLabel.setStyle(
                "-fx-font-family: 'Consolas', 'Monospaced';" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #00f0ff;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,240,255,0.4), 8, 0, 0, 0);"
        );

        livesLabel.setStyle(
                "-fx-font-size: 16px;" +
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
                    "-fx-background-color: #1e1e2e;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-color: #313244;" +
                            "-fx-border-radius: 8;" +
                            "-fx-padding: 15;" +
                            "-fx-fill-width: true;"
            );

            Label cardTitle = new Label("CONTROLS 🎮");
            cardTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f5c2e7;");
            hintsCard.getChildren().add(cardTitle);

            // add hint
            hintsCard.getChildren().add(createHintRow("發射球：", "長按 SPACE", "#89b4fa"));
            hintsCard.getChildren().add(createHintRow("左撥片：", "◀ 左方向鍵 ", "#74c7ec"));
            hintsCard.getChildren().add(createHintRow("右撥片：", "右方向鍵 ▶", "#74c7ec"));

            rightPanel.getChildren().clear();

            // push control hint down
            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            // sort
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
        scoreLabel.setText("Score: " + score);
        livesLabel.setText("Balls: " + lives);
        gameOverOverlay.setVisible(false);

        // 重置動態閘門狀態
        dynamicGateWall = null;
        isGateClosed = false;

        physicsEngine = new PinballPhysicsEngine();
        physicsEngine.setSoundManager(sound);
        physicsEngine.setOnScoreAdded(this::addScore);

        TableBuilder tableBuilder = new TableBuilder(CANVAS_WIDTH, CANVAS_HEIGHT);
        for (Wall wall : tableBuilder.buildBoundaryWalls()) {
            physicsEngine.addCollisionObject(wall);
        }

        TableBuilder.TableGeometry tableGeometry = tableBuilder.buildTableGeometries();
        for (GameObject gameObject : tableGeometry.getObjects()) {
            physicsEngine.addCollisionObject(gameObject);
        }
        dynamicGateWall = tableGeometry.getDynamicGateWall();
        physicsEngine.configureBumperRespawn(
                tableGeometry.getBumpers(),
                tableGeometry.getBumperPositions(),
                tableGeometry.getBumperPositionIndices());

        // 實例化台面動態物件
        Ball ball = new Ball(373.0, 500.0, 8.0);
        ball.setVelocityX(0.0);
        ball.setVelocityY(0.0);
        physicsEngine.addBall(ball);


        leftFlipper = new Flipper(110.0, 480.0, 60.0, 0.48, -0.85, 10.5, 0.95);
        rightFlipper = new Flipper(240.0, 480.0, 60.0, Math.PI - 0.48, Math.PI + 0.85, 10.5, 0.95);
        physicsEngine.addCollisionObject(leftFlipper);
        physicsEngine.addCollisionObject(rightFlipper);

        pinballCanvas = new PinballCanvas(CANVAS_WIDTH, CANVAS_HEIGHT);
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
            protected void onFixedUpdate(double timeStep) {
                updateChargePower(timeStep);
            }

            @Override
            public void handle(long now) {
                super.handle(now);

                Ball chuteBall = physicsEngine.getBalls().isEmpty() ? null : physicsEngine.getBalls().get(0);
                boolean inChute = (chuteBall != null && chuteBall.getPositionX() > 340);

                if (chuteBall != null) {
                    if (!isGateClosed && (chuteBall.getPositionY() < 140.0 || chuteBall.getPositionX() < 340.0)) {
                        isGateClosed = true;
                        if (dynamicGateWall != null) {
                            dynamicGateWall.setActive(true);
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

                        livesLabel.setText("Balls: "+ lives);

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