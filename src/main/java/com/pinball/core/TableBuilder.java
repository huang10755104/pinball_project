package com.pinball.core;

import com.pinball.model.Bumper;
import com.pinball.model.Flipper;
import com.pinball.model.GameObject;
import com.pinball.model.Wall;

import java.util.ArrayList;
import java.util.List;

public class TableBuilder {
    private static final double INTERNAL_WIDTH = 600.0;
    private static final double INTERNAL_HEIGHT = 416.0;

    private double scaleX;
    private double scaleY;

    public TableBuilder(double canvasWidth, double canvasHeight) {
        this.scaleX = canvasWidth / INTERNAL_WIDTH;
        this.scaleY = canvasHeight / INTERNAL_HEIGHT;
    }

    private double mapX(double x) { return x * scaleX; }
    private double mapY(double y) { return y * scaleY; }

    public List<GameObject> buildTableGeometries() {
        List<GameObject> objects = new ArrayList<>();
        
        // Attack Bumpers (Exact float values mapped from DAT center points)
        double bumperRadius = 15.0 * scaleX; 
        objects.add(new Bumper(mapX(295.0), mapY(100.0), bumperRadius));
        objects.add(new Bumper(mapX(240.0), mapY(140.0), bumperRadius));
        objects.add(new Bumper(mapX(350.0), mapY(140.0), bumperRadius));

        // Launch Bumpers / Plunger guides
        objects.add(new Wall(mapX(580.0), mapY(416.0), mapX(580.0), mapY(100.0))); // Inner Plunger Chute
        objects.add(new Wall(mapX(600.0), mapY(416.0), mapX(600.0), mapY(0.0)));   // Outer Plunger Chute

        // Exact angled lanes (Left/Right Inlane/Outlane divider)
        objects.add(new Wall(mapX(100.0), mapY(250.0), mapX(150.0), mapY(330.0)));
        objects.add(new Wall(mapX(480.0), mapY(250.0), mapX(430.0), mapY(330.0)));

        return objects;
    }
    
    public List<Wall> buildBoundaryWalls() {
        List<Wall> walls = new ArrayList<>();
        // Slingshots & Table Hull (Bounds)
        walls.add(new Wall(mapX(0.0), mapY(0.0), mapX(0.0), mapY(416.0)));
        walls.add(new Wall(mapX(0.0), mapY(416.0), mapX(600.0), mapY(416.0)));
        walls.add(new Wall(mapX(0.0), mapY(0.0), mapX(600.0), mapY(0.0)));
        return walls;
    }
}
