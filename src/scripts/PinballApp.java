package scripts;

import javafx.application.Application;
import javafx.stage.Stage;

public class PinballApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        PinballController controller = new PinballController(() -> primaryStage.close());

        primaryStage.setTitle("JavaFX 獨立彈珠台專案");
        primaryStage.setScene(controller.getScene());
        primaryStage.setResizable(false); 
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}