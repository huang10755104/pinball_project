package com.pinball.ui;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class PrimaryController {
    @FXML
    private Pane canvasContainer;
    private PinballCanvas pinballCanvas;

    @FXML
    private void initialize() {
        pinballCanvas = new PinballCanvas();
        canvasContainer.getChildren().add(pinballCanvas);
    }

    public Pane getCanvasContainer() {
        return canvasContainer;
    }

    public PinballCanvas getPinballCanvas() {
        return pinballCanvas;
    }
}
