package scripts;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;

public class PinballUI {
    public BorderPane root;
    public Button exitBtn;
    public Label scoreLabel;
    
    public Pane gameBoard;
    public Circle ball;
    public Rectangle leftFlipper;
    public Rectangle rightFlipper;
    public Rectangle launchWall;
    public Circle[] bumpers;
    public Polygon topDeflector;
    public Line leftInlane;
    public Line rightInlane;
    public Rotate leftFlipperRotate;
    public Rotate rightFlipperRotate;

    public Button launchBtn;

    public PinballUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e2e;");
        root.setPadding(new Insets(20, 25, 20, 25));

        // ================= 1. 頂部區域 =================
        exitBtn = new Button("❌ 離開遊戲");
        exitBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f38ba8; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0;");

        scoreLabel = new Label("分數: 0");
        scoreLabel.setStyle("-fx-text-fill: #f9e2af; -fx-font-size: 24px; -fx-font-weight: bold;");

        BorderPane topBox = new BorderPane();
        topBox.setLeft(exitBtn);
        topBox.setRight(scoreLabel);
        topBox.setPadding(new Insets(0, 0, 15, 0));
        root.setTop(topBox);

        // ================= 2. 中央遊戲台區域 =================
        gameBoard = new Pane();
        gameBoard.setPrefSize(400, 550);
        gameBoard.setMaxSize(400, 550);
        gameBoard.setStyle("-fx-background-color: #11111b; -fx-border-color: #45475a; -fx-border-width: 4px; -fx-border-radius: 12px; -fx-background-radius: 12px;");

        bumpers = new Circle[3];
        bumpers[0] = new Circle(120, 150, 25, Color.valueOf("#f38ba8")); 
        bumpers[1] = new Circle(280, 150, 25, Color.valueOf("#f38ba8"));
        bumpers[2] = new Circle(200, 250, 30, Color.valueOf("#cba6f7")); 

        launchWall = new Rectangle(350, 100, 5, 450);
        launchWall.setFill(Color.valueOf("#45475a"));

        topDeflector = new Polygon(330.0, 0.0, 400.0, 0.0, 400.0, 70.0);
        topDeflector.setFill(Color.valueOf("#f9e2af"));

        // ================= 3. 靜態斜板與擋板設計 =================

        leftInlane = new Line(5, 432, 100, 470);
        leftInlane.setStroke(Color.valueOf("#45475a"));
        leftInlane.setStrokeWidth(5);

        leftFlipper = new Rectangle(100, 470, 80, 10);
        leftFlipper.setFill(Color.valueOf("#a6e3a1")); 
        leftFlipperRotate = new Rotate(20, 100, 470); 
        leftFlipper.getTransforms().add(leftFlipperRotate);


        rightInlane = new Line(350, 432, 255, 470);
        rightInlane.setStroke(Color.valueOf("#45475a"));
        rightInlane.setStrokeWidth(5);

        rightFlipper = new Rectangle(170, 470, 80, 10);
        rightFlipper.setFill(Color.valueOf("#a6e3a1"));
        rightFlipperRotate = new Rotate(-20, 250, 470);
        rightFlipper.getTransforms().add(rightFlipperRotate);

        ball = new Circle(375, 520, 10, Color.valueOf("#89b4fa")); 

        gameBoard.getChildren().addAll(bumpers[0], bumpers[1], bumpers[2], launchWall, topDeflector, 
                                       leftInlane, rightInlane, leftFlipper, rightFlipper, ball);
        
        VBox boardContainer = new VBox(gameBoard);
        boardContainer.setAlignment(Pos.CENTER);
        root.setCenter(boardContainer);

        // ================= 4. 底部控制區域 =================
        launchBtn = new Button("🚀 發射彈珠");
        launchBtn.setStyle("-fx-background-color: #f9e2af; -fx-text-fill: #11111b; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-pref-width: 200px; -fx-pref-height: 45px; -fx-cursor: hand;");

        Label tipLabel = new Label("提示：使用左方向鍵 (←) 與右方向鍵 (→) 控制擋板");
        tipLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 12px;");

        VBox bottomBox = new VBox(10, launchBtn, tipLabel);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));
        root.setBottom(bottomBox);
    }
}

