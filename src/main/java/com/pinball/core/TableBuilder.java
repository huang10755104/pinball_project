package com.pinball.core;

import com.pinball.model.Bumper;
import com.pinball.model.GameObject;
import com.pinball.model.SpringWall;
import com.pinball.model.Wall;

import java.util.ArrayList;
import java.util.List;

public class TableBuilder {
    private static final double INTERNAL_WIDTH = 400.0;
    private static final double INTERNAL_HEIGHT = 550.0;

    private static final double[][] BUMPER_POSITIONS = {
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

    private static final int[] INITIAL_BUMPER_INDICES = {0, 1, 2};

    private final double scaleX;
    private final double scaleY;

    public TableBuilder(double canvasWidth, double canvasHeight) {
        this.scaleX = canvasWidth / INTERNAL_WIDTH;
        this.scaleY = canvasHeight / INTERNAL_HEIGHT;
    }

    private double mapX(double x) {
        return x * scaleX;
    }

    private double mapY(double y) {
        return y * scaleY;
    }

    private double mapRadius(double radius) {
        return radius * Math.min(scaleX, scaleY);
    }

    public List<Wall> buildBoundaryWalls() {
        List<Wall> walls = new ArrayList<>();
        addTopArcWalls(walls);
        walls.add(new Wall(mapX(5.0), mapY(150.0), mapX(5.0), mapY(432.0)));
        walls.add(new Wall(mapX(385.0), mapY(150.0), mapX(385.0), mapY(550.0)));
        return walls;
    }

    public TableGeometry buildTableGeometries() {
        List<GameObject> objects = new ArrayList<>();
        List<Bumper> bumpers = new ArrayList<>();
        double[][] mappedPositions = mapPositions(BUMPER_POSITIONS);
        double bumperRadius = mapRadius(15.0);

        for (int index : INITIAL_BUMPER_INDICES) {
            double[] position = mappedPositions[index];
            Bumper bumper = new Bumper(position[0], position[1], bumperRadius);
            bumpers.add(bumper);
            objects.add(bumper);
        }

        objects.add(new Wall(mapX(345.0), mapY(150.0), mapX(345.0), mapY(432.0)));
        objects.add(new Wall(mapX(30.0), mapY(320.0), mapX(30.0), mapY(380.0)));
        objects.add(new Wall(mapX(320.0), mapY(320.0), mapX(320.0), mapY(380.0)));

        objects.add(new Wall(mapX(85.0), mapY(330.0), mapX(60.0), mapY(400.0)));
        objects.add(new Wall(mapX(60.0), mapY(400.0), mapX(95.0), mapY(430.0)));
        Wall leftSlingshot = new Wall(mapX(95.0), mapY(430.0), mapX(85.0), mapY(330.0));
        leftSlingshot.setBounciness(1.2);
        objects.add(leftSlingshot);

        objects.add(new Wall(mapX(265.0), mapY(330.0), mapX(290.0), mapY(400.0)));
        objects.add(new Wall(mapX(290.0), mapY(400.0), mapX(255.0), mapY(430.0)));
        Wall rightSlingshot = new Wall(mapX(255.0), mapY(430.0), mapX(265.0), mapY(330.0));
        rightSlingshot.setBounciness(1.2);
        objects.add(rightSlingshot);

        objects.add(new Wall(mapX(5.0), mapY(390.0), mapX(105.0), mapY(474.0)));
        objects.add(new Wall(mapX(345.0), mapY(390.0), mapX(245.0), mapY(474.0)));
        objects.add(new Wall(mapX(360.0), mapY(180.0), mapX(360.0), mapY(550.0)));

        Wall dynamicGateWall = new Wall(mapX(345.0), mapY(150.0), mapX(385.0), mapY(150.0));
        dynamicGateWall.setActive(false);
        objects.add(dynamicGateWall);

        objects.add(new Wall(mapX(320.0), mapY(100.0), mapX(330.0), mapY(120.0)));
        objects.add(new Wall(mapX(330.0), mapY(120.0), mapX(330.0), mapY(140.0)));
        objects.add(new Wall(mapX(330.0), mapY(140.0), mapX(300.0), mapY(220.0)));
        objects.add(new Wall(mapX(260.0), mapY(260.0), mapX(250.0), mapY(270.0)));
        objects.add(new Wall(mapX(250.0), mapY(270.0), mapX(290.0), mapY(250.0)));
        objects.add(new Wall(mapX(290.0), mapY(250.0), mapX(300.0), mapY(220.0)));
        SpringWall rightBumperWall = new SpringWall(mapX(300.0), mapY(220.0), mapX(260.0), mapY(260.0), 0.8);
        rightBumperWall.setBounciness(1.2);
        objects.add(rightBumperWall);

        objects.add(new Wall(mapX(80.0), mapY(140.0), mapX(80.0), mapY(220.0)));
        objects.add(new Wall(mapX(140.0), mapY(240.0), mapX(80.0), mapY(260.0)));
        objects.add(new Wall(mapX(80.0), mapY(260.0), mapX(50.0), mapY(220.0)));
        objects.add(new Wall(mapX(50.0), mapY(220.0), mapX(80.0), mapY(140.0)));
        SpringWall leftBumperWall = new SpringWall(mapX(80.0), mapY(220.0), mapX(140.0), mapY(240.0));
        leftBumperWall.setBounciness(1.5);
        objects.add(leftBumperWall);

        return new TableGeometry(objects, bumpers, dynamicGateWall, mappedPositions, INITIAL_BUMPER_INDICES);
    }

    private void addTopArcWalls(List<Wall> walls) {
        double leftWallX = 5.0;
        double rightOuterWallX = 385.0;
        double playableWidth = rightOuterWallX - leftWallX;
        double centerX = leftWallX + (playableWidth / 2.0);
        double startY = 150.0;
        double radiusX = playableWidth / 2.0;
        double radiusY = 140.0;
        int segments = 50;

        double lastX = leftWallX;
        double lastY = startY;
        for (int i = 1; i <= segments; i++) {
            double angleRad = Math.toRadians(180.0 - (180.0 / segments) * i);
            double nextX = centerX + radiusX * Math.cos(angleRad);
            double nextY = startY - radiusY * Math.sin(angleRad);
            walls.add(new Wall(mapX(lastX), mapY(lastY), mapX(nextX), mapY(nextY)));
            lastX = nextX;
            lastY = nextY;
        }
    }

    private double[][] mapPositions(double[][] positions) {
        double[][] mapped = new double[positions.length][2];
        for (int i = 0; i < positions.length; i++) {
            mapped[i][0] = mapX(positions[i][0]);
            mapped[i][1] = mapY(positions[i][1]);
        }
        return mapped;
    }

    public static final class TableGeometry {
        private final List<GameObject> objects;
        private final List<Bumper> bumpers;
        private final Wall dynamicGateWall;
        private final double[][] bumperPositions;
        private final int[] bumperPositionIndices;

        private TableGeometry(
                List<GameObject> objects,
                List<Bumper> bumpers,
                Wall dynamicGateWall,
                double[][] bumperPositions,
                int[] bumperPositionIndices) {
            this.objects = objects;
            this.bumpers = bumpers;
            this.dynamicGateWall = dynamicGateWall;
            this.bumperPositions = bumperPositions;
            this.bumperPositionIndices = bumperPositionIndices;
        }

        public List<GameObject> getObjects() {
            return objects;
        }

        public List<Bumper> getBumpers() {
            return bumpers;
        }

        public Wall getDynamicGateWall() {
            return dynamicGateWall;
        }

        public double[][] getBumperPositions() {
            return bumperPositions;
        }

        public int[] getBumperPositionIndices() {
            return bumperPositionIndices;
        }
    }
}
