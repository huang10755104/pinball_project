package scripts;

import java.io.File;
import javafx.scene.media.AudioClip;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class PinballController {

    private PinballUI ui;
    private Scene scene;
    private AnimationTimer gameLoop;
    private CollisionEngine physics;

    private int score = 0;
    private boolean isPlaying = false;

    private AudioClip flipperSound;
    private AudioClip bumperSound;
    private AudioClip launchSound;

    // 彈珠物理變數
    private final double GRAVITY = 0.1;
    private final double FRICTION = 0.99;

    private double leftTargetAngle = 20;
    private double rightTargetAngle = -20;
    private final double FLIPPER_SPEED = 15.0;

    public PinballController(Runnable onExitAction) {
        ui = new PinballUI();
        scene = new Scene(ui.root, 500, 750); 

        ui.exitBtn.setOnAction(e -> onExitAction.run());
        physics = new CollisionEngine(ui.ball);

        initAudio();
        setupListeners();
        setupGameLoop();
    }

    private void initAudio() {
        try {
            // 使用 File 轉換路徑，確保無論在 IDE 還是命令列都能讀取到
            String flipperPath = new File("src/res/audios/flipper.mp3").toURI().toString();
            String bumperPath = new File("src/res/audios/bumper.mp3").toURI().toString();
            String launchPath = new File("src/res/audios/launch.mp3").toURI().toString();

            flipperSound = new AudioClip(flipperPath);
            bumperSound = new AudioClip(bumperPath);
            launchSound = new AudioClip(launchPath);
        } catch (Exception e) {
            System.out.println("音效載入失敗 (如果尚未準備音效檔，可忽略此訊息): " + e.getMessage());
        }
    }

    public Scene getScene() {
        return scene;
    }

    private void setupListeners() {
        ui.launchBtn.setOnAction(e -> {
            if (!isPlaying) startGame();
            // 發射後，焦點必須還給 scene，鍵盤控制才會生效
            ui.root.requestFocus(); 
        });

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                leftTargetAngle = -20;
                if (flipperSound != null) flipperSound.play(0.4);
            } else if (event.getCode() == KeyCode.RIGHT) {
                rightTargetAngle = 20;
                if (flipperSound != null) flipperSound.play(0.4);
            }
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                leftTargetAngle = 20;
            } else if (event.getCode() == KeyCode.RIGHT) {
                rightTargetAngle = -20;
            }
        });
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isPlaying) {
                    updatePhysics();
                }
            }
        };
    }

    private void updatePhysics() {
        double previousY = ui.ball.getCenterY();
        
        physics.updatePosition(GRAVITY, FRICTION);
        
        double currentLeftAngle = ui.leftFlipperRotate.getAngle();
        if (currentLeftAngle > leftTargetAngle) {
            currentLeftAngle = Math.max(leftTargetAngle, currentLeftAngle - FLIPPER_SPEED);
        } else if (currentLeftAngle < leftTargetAngle) {
            currentLeftAngle = Math.min(leftTargetAngle, currentLeftAngle + FLIPPER_SPEED);
        }
        ui.leftFlipperRotate.setAngle(currentLeftAngle);

        double currentRightAngle = ui.rightFlipperRotate.getAngle();
        if (currentRightAngle < rightTargetAngle) {
            currentRightAngle = Math.min(rightTargetAngle, currentRightAngle + FLIPPER_SPEED);
        } else if (currentRightAngle > rightTargetAngle) {
            currentRightAngle = Math.max(rightTargetAngle, currentRightAngle - FLIPPER_SPEED);
        }
        ui.rightFlipperRotate.setAngle(currentRightAngle);

        // ================== 通用物理靜態碰撞區 ==================

        physics.checkPlaneCollision(1, 0, 0, 0.8);      
        physics.checkPlaneCollision(-1, 0, -400, 0.8);  
        physics.checkPlaneCollision(0, 1, 0, 0.8);
        physics.checkLineSegmentCollision(330, 0, 400, 70, 0.7);
        physics.checkRectangleCollision(ui.launchWall, 0.8);
        physics.checkLineCollision(ui.leftInlane, 0.3);
        physics.checkLineCollision(ui.rightInlane, 0.3);

        for (Circle bumper : ui.bumpers) {
            if (physics.checkCircleCollision(bumper, 1.2)){
                addScore(100);
                if (bumperSound != null) bumperSound.play(0.6);
            } 
        }

        // ================== 動態玩家控制區 ==================
        
        boolean hitLeft = !Shape.intersect(ui.ball, ui.leftFlipper).getBoundsInLocal().isEmpty();
        boolean hitRight = !Shape.intersect(ui.ball, ui.rightFlipper).getBoundsInLocal().isEmpty();

        if (hitLeft || hitRight) {
            boolean isLeftSwingingUp = (leftTargetAngle == -20 && currentLeftAngle > -20);
            boolean isRightSwingingUp = (rightTargetAngle == 20 && currentRightAngle < 20);
            
            boolean isStrongStrike = (hitLeft && isLeftSwingingUp) || (hitRight && isRightSwingingUp);

            ui.ball.setCenterY(previousY - 2); 

            if (isStrongStrike) {
                if (physics.speedY > -8) { 
                    double distanceFromPivot = hitLeft ? (ui.ball.getCenterX() - 120) : (280 - ui.ball.getCenterX());
                    distanceFromPivot = Math.max(0, Math.min(distanceFromPivot, 80)); 
                    
                    double powerMultiplier = 1.0 + (distanceFromPivot / 80.0) * 0.6;
                    physics.speedY = -9 * powerMultiplier; 
                    physics.speedX = (hitLeft ? 5 : -5) * (distanceFromPivot / 80.0);
                }
            } else {
                if (physics.speedY > 0) {
                    physics.speedY = -physics.speedY * 0.1; 
                    if (hitLeft) {
                        physics.speedX += (currentLeftAngle <= 0) ? -1.0 : 1.0; 
                    }
                    if (hitRight) {
                        physics.speedX += (currentRightAngle >= 0) ? 1.0 : -1.0; 
                    }
                }
            }
        }

        // ================== 全域物理安全鎖 ==================
        
        double MAX_SPEED = 16.0;
        if (physics.speedX > MAX_SPEED) physics.speedX = MAX_SPEED;
        if (physics.speedX < -MAX_SPEED) physics.speedX = -MAX_SPEED;
        if (physics.speedY > MAX_SPEED) physics.speedY = MAX_SPEED;
        if (physics.speedY < -MAX_SPEED) physics.speedY = -MAX_SPEED;

        // ================== 遊戲判定區 ==================
        
        if (ui.ball.getCenterY() + ui.ball.getRadius() >= 550) {
            stopGame();
        }
    }

    private void startGame() {
        isPlaying = true;
        ui.launchBtn.setDisable(true);
        score = 0;
        addScore(0);
        if (launchSound != null) launchSound.play(1.0);
        // 初始發射力道 (往上並往左拋出通道)
        physics.speedY = -20;
        physics.speedX = 2;
        
        gameLoop.start();
    }

    private void stopGame() {
        isPlaying = false;
        ui.launchBtn.setDisable(false);
        gameLoop.stop();
        
        // 彈珠重置回發射區
        ui.ball.setCenterX(375);
        ui.ball.setCenterY(520);
        physics.speedX = 0;
        physics.speedY = 0;
    }

    private void addScore(int points) {
        score += points;
        ui.scoreLabel.setText("分數: " + score);
    }
}
