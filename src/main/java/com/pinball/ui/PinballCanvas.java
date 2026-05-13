package com.pinball.ui;

import com.pinball.core.Renderable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class PinballCanvas extends Canvas implements Renderable {
    public PinballCanvas() {
        this(800, 600);
    }

    public PinballCanvas(double width, double height) {
        super(width, height);
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Rendering is implemented by game-specific components.
    }
}
