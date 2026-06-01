package com.pinball.core;

import com.pinball.model.Ball;
import com.pinball.model.Bumper;
import com.pinball.model.Flipper;
import com.pinball.model.GameObject;
import com.pinball.model.SpringWall;
import com.pinball.model.Wall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;

public class PinballPhysicsEngine implements PhysicsEngine {
    private static final double GRAVITY = 980.0;
    private static final double NO_HIT = 1_000_000_000.0;
    private static final double CONTACT_EPSILON = 0.0005;
    
    private final List<Ball> balls = new ArrayList<>();
    private final List<GameObject> collisionObjects = new ArrayList<>();
    
    private SoundManager sound;
    private IntConsumer onScoreAdded; 

    // 🌟 這裡統一改成基本型態 int[] 陣列
    private Bumper[] activeBumpers;
    private double[][] bumperPositions;
    private int[] activeBumpersIndex;

    public void setOnScoreAdded(IntConsumer listener) {
        this.onScoreAdded = listener;
    }

    public void setSoundManager(SoundManager sound) {
        this.sound = sound;
    }

    public List<Ball> getBalls() {
        return Collections.unmodifiableList(balls);
    }

    public List<GameObject> getCollisionObjects() {
        return Collections.unmodifiableList(collisionObjects);
    }

    public void addBall(Ball ball) {
        if (ball != null) {
            balls.add(ball);
        }
    }

    public void removeBall(Ball ball) {
        balls.remove(ball);
    }

    public void addCollisionObject(GameObject gameObject) {
        if (gameObject != null) {
            collisionObjects.add(gameObject);
        }
    }

    public void removeCollisionObject(GameObject gameObject) {
        collisionObjects.remove(gameObject);
    }

    // 🌟 這裡的方法簽章也統一改成接收 int[] indices
    public void configureBumperRespawn(Bumper[] bumpers, double[][] positions, int[] indices) {
        this.activeBumpers = bumpers;
        this.bumperPositions = positions;
        this.activeBumpersIndex = indices;
    }

    @Override
    public void update(double deltaTime) {
        if (deltaTime <= 0.0) {
            return;
        }

        for (GameObject gameObject : collisionObjects) {
            if (gameObject instanceof Flipper flipper) {
                flipper.update(deltaTime);
            }
        }

        for (Ball ball : balls) {
            ball.setAccelerationY(GRAVITY);
            ball.update(deltaTime);
        }
    }

    @Override
    public void checkCollision() {
        for (Ball ball : balls) {
            for (GameObject gameObject : collisionObjects) {
                if (gameObject == null || !gameObject.isActive()) {
                    continue;
                }

                if (gameObject instanceof Bumper bumper) {
                    resolveBumperCollision(ball, bumper);
                } else if (gameObject instanceof Wall wall) {
                    resolveWallCollision(ball, wall);
                } else if (gameObject instanceof Flipper flipper) {
                    resolveFlipperCollision(ball, flipper);
                }
            }
        }
    }

    private void resolveFlipperCollision(Ball ball, Flipper flipper) {
        double startX = flipper.getPivotX();
        double startY = flipper.getPivotY();
        double endX = flipper.getEndX();
        double endY = flipper.getEndY();

        double segmentX = endX - startX;
        double segmentY = endY - startY;
        double segmentLengthSq = segmentX * segmentX + segmentY * segmentY;
        if (segmentLengthSq <= 0.0) {
            return;
        }

        double ballX = ball.getPositionX();
        double ballY = ball.getPositionY();
        double projection = ((ballX - startX) * segmentX + (ballY - startY) * segmentY) / segmentLengthSq;
        projection = clamp(projection, 0.0, 1.0);

        double closestX = startX + segmentX * projection;
        double closestY = startY + segmentY * projection;
        double offsetX = ballX - closestX;
        double offsetY = ballY - closestY;
        double distance = Math.hypot(offsetX, offsetY);
        
        boolean crossed = false;
        double originX = ball.getPreviousPositionX();
        double originY = ball.getPreviousPositionY();
        double deltaX = ballX - originX;
        double deltaY = ballY - originY;
        double travelDistance = Math.hypot(deltaX, deltaY);

        if (travelDistance > 0.0) {
            double[] hitPoint = new double[2];
            double hitDistance = rayIntersectLine(
                    originX, originY, 
                    deltaX / travelDistance, deltaY / travelDistance, travelDistance,
                    startX, startY, endX, endY, hitPoint);
            
            if (hitDistance < NO_HIT) {
                crossed = true;
            }
        }

        if (distance > ball.getRadius() && !crossed) {
            return;
        }

        double normalX;
        double normalY;
        if (distance > 0.0) {
            normalX = offsetX / distance;
            normalY = offsetY / distance;
        } else {
            normalX = -segmentY;
            normalY = segmentX;
            double normalLength = Math.hypot(normalX, normalY);
            if (normalLength == 0.0) {
                return;
            }

            normalX /= normalLength;
            normalY /= normalLength;
        }

        double velocityX = ball.getVelocityX();
        double velocityY = ball.getVelocityY();
        double approachSpeed = velocityX * normalX + velocityY * normalY;

        if (approachSpeed >= 0.0) {
            return;
        }

        double angularVelocity = flipper.getAngularVelocity();
        double relativeX = closestX - flipper.getPivotX();
        double relativeY = closestY - flipper.getPivotY();
        double surfaceVelocityX = -angularVelocity * relativeY;
        double surfaceVelocityY = angularVelocity * relativeX;

        double relativeVelocityX = velocityX - surfaceVelocityX;
        double relativeVelocityY = velocityY - surfaceVelocityY;
        double relativeNormalSpeed = relativeVelocityX * normalX + relativeVelocityY * normalY;

        if (relativeNormalSpeed >= 0.0) {
            return;
        }

        double elasticity = clamp(flipper.getElasticity(), 0.0, 1.0);
        double reflectedRelativeX = relativeVelocityX - (1.0 + elasticity) * relativeNormalSpeed * normalX;
        double reflectedRelativeY = relativeVelocityY - (1.0 + elasticity) * relativeNormalSpeed * normalY;

        if (flipper.isExtending()) {
            reflectedRelativeY -= Math.min(220.0, Math.abs(angularVelocity) * 42.0);
        }

        ball.setPositionX(closestX + normalX * (ball.getRadius() + CONTACT_EPSILON));
        ball.setPositionY(closestY + normalY * (ball.getRadius() + CONTACT_EPSILON));
        ball.setVelocityX(reflectedRelativeX + surfaceVelocityX);
        ball.setVelocityY(reflectedRelativeY + surfaceVelocityY);
    }

    private void resolveBumperCollision(Ball ball, Bumper bumper) {
        double originX = ball.getPreviousPositionX();
        double originY = ball.getPreviousPositionY();
        double deltaX = ball.getPositionX() - originX;
        double deltaY = ball.getPositionY() - originY;
        double travelDistance = Math.hypot(deltaX, deltaY);

        if (travelDistance <= 0.0) {
            return;
        }

        double directionX = deltaX / travelDistance;
        double directionY = deltaY / travelDistance;
        double radius = ball.getRadius() + bumper.getRadius();

        double hitDistance = rayIntersectCircle(
                originX,
                originY,
                directionX,
                directionY,
                travelDistance,
                bumper.getCenterX(),
                bumper.getCenterY(),
                radius);

        if (hitDistance >= NO_HIT || hitDistance > travelDistance) {
            return;
        }

        double contactX = originX + directionX * hitDistance;
        double contactY = originY + directionY * hitDistance;
        double normalX = contactX - bumper.getCenterX();
        double normalY = contactY - bumper.getCenterY();
        double normalLength = Math.hypot(normalX, normalY);

        if (normalLength == 0.0) {
            return;
        }

        normalX /= normalLength;
        normalY /= normalLength;

        double velocityX = ball.getVelocityX();
        double velocityY = ball.getVelocityY();
        double velocityDotNormal = velocityX * normalX + velocityY * normalY;

        if (velocityDotNormal >= 0.0) {
            return;
        }

        double elasticity = clamp(bumper.getElasticity(), 0.0, 1.0);
        double reflectedVelocityX = velocityX - (1.0 + elasticity) * velocityDotNormal * normalX;
        double reflectedVelocityY = velocityY - (1.0 + elasticity) * velocityDotNormal * normalY;

        ball.setPositionX(contactX + normalX * CONTACT_EPSILON);
        ball.setPositionY(contactY + normalY * CONTACT_EPSILON);
        ball.setVelocityX(reflectedVelocityX);
        ball.setVelocityY(reflectedVelocityY);
        
        bumper.registerHit();
        if (sound != null) {
            sound.playBumper(0.3);
        }

        // 隊友的隨機 Bumper 類型切換系統
        double rand = Math.random();
        if (rand < 0.03) {
            bumper.setBumperType("Diamond", javafx.scene.paint.Color.web("#b9f2ff"), 2000);
        } else if (rand < 0.13) {
            bumper.setBumperType("Gold", javafx.scene.paint.Color.web("#ffd700"), 500);
        } else if (rand < 0.38) {
            bumper.setBumperType("Silver", javafx.scene.paint.Color.web("#c0c0c0"), 300);
        } else {
            bumper.setBumperType("Bronze", javafx.scene.paint.Color.web("#782323"), 100);
        }

        // 🌟 這裡的 for 迴圈也完美改成走純 int 分支
        if (activeBumpers != null && bumperPositions != null && activeBumpersIndex != null) {
            int bumperIdx = -1;
            for (int i = 0; i < activeBumpers.length; i++) {
                if (activeBumpers[i] == bumper) {
                    bumperIdx = i;
                    break;
                }
            }
            if (bumperIdx != -1) {
                int randomIndex = (int) (Math.random() * bumperPositions.length);
                boolean contains = true;
                int attempts = 0;
                while (contains && attempts < 100) {
                    contains = false;
                    for (int idx : activeBumpersIndex) {
                        if (idx == randomIndex) {
                            contains = true;
                            break;
                        }
                    }
                    if (contains) {
                        randomIndex = (int) (Math.random() * bumperPositions.length);
                    }
                    attempts++;
                }

                activeBumpersIndex[bumperIdx] = randomIndex;
                bumper.setCenterX(bumperPositions[randomIndex][0]);
                bumper.setCenterY(bumperPositions[randomIndex][1]);
            }
        }

        if (onScoreAdded != null) {
            onScoreAdded.accept(bumper.getScoreValue()); 
        }
    }

    private void resolveWallCollision(Ball ball, Wall wall) {
        double originX = ball.getPreviousPositionX();
        double originY = ball.getPreviousPositionY();
        double ballX = ball.getPositionX();
        double ballY = ball.getPositionY();
        double deltaX = ballX - originX;
        double deltaY = ballY - originY;
        double travelDistance = Math.hypot(deltaX, deltaY);

        double wallDx = wall.getEndX() - wall.getStartX();
        double wallDy = wall.getEndY() - wall.getStartY();
        double wallLenSq = wallDx * wallDx + wallDy * wallDy;

        if (wallLenSq == 0.0) return;

        double normalX = wallDy;
        double normalY = -wallDx;
        double normalLength = Math.sqrt(wallLenSq);
        normalX /= normalLength;
        normalY /= normalLength;

        if ((originX - wall.getStartX()) * normalX + (originY - wall.getStartY()) * normalY < 0) {
            normalX = -normalX;
            normalY = -normalY;
        }

        if (travelDistance > 0.0) {
            double directionX = deltaX / travelDistance;
            double directionY = deltaY / travelDistance;
            
            double r = ball.getRadius();
            double vStartX = wall.getStartX() + normalX * r;
            double vStartY = wall.getStartY() + normalY * r;
            double vEndX = wall.getEndX() + normalX * r;
            double vEndY = wall.getEndY() + normalY * r;

            double[] hitPoint = new double[2];
            
            double hitDistance = rayIntersectLine(
                    originX, originY, directionX, directionY, travelDistance,
                    vStartX, vStartY, vEndX, vEndY, hitPoint);

            if (hitDistance < NO_HIT) {
                double velocityX = ball.getVelocityX();
                double velocityY = ball.getVelocityY();
                double velocityDotNormal = velocityX * normalX + velocityY * normalY;

                if (velocityDotNormal < 0.0) {
                    double reflectedVelocityX;
                    double reflectedVelocityY;

                    if (wall instanceof SpringWall springWall) {
                        double influence = 0.1;
                        double kick = springWall.getKickForce();
                        reflectedVelocityX = velocityX * influence + normalX * kick;
                        reflectedVelocityY = velocityY * influence + normalY * kick;
                    } else {
                        double elasticity = clamp(wall.getBounciness(), 0.0, 1.0);
                        reflectedVelocityX = velocityX - (1.0 + elasticity) * velocityDotNormal * normalX;
                        reflectedVelocityY = velocityY - (1.0 + elasticity) * velocityDotNormal * normalY;
                    }

                    double remainingDistance = Math.max(0.0, travelDistance - hitDistance);
                    double currentSpeed = Math.hypot(reflectedVelocityX, reflectedVelocityY);
                    if (currentSpeed > 0.0) {
                        double dirX = reflectedVelocityX / currentSpeed;
                        double dirY = reflectedVelocityY / currentSpeed;
                        
                        ball.setPositionX(hitPoint[0] + normalX * CONTACT_EPSILON + dirX * remainingDistance);
                        ball.setPositionY(hitPoint[1] + normalY * CONTACT_EPSILON + dirY * remainingDistance);
                    } else {
                        ball.setPositionX(hitPoint[0] + normalX * CONTACT_EPSILON);
                        ball.setPositionY(hitPoint[1] + normalY * CONTACT_EPSILON);
                    }

                    ball.setVelocityX(reflectedVelocityX);
                    ball.setVelocityY(reflectedVelocityY);
                    return;
                }
            }
        }

        ballX = ball.getPositionX();
        ballY = ball.getPositionY();

        double t = ((ballX - wall.getStartX()) * wallDx + (ballY - wall.getStartY()) * wallDy) / wallLenSq;
        t = Math.max(0.0, Math.min(1.0, t));

        double closestX = wall.getStartX() + t * wallDx;
        double closestY = wall.getStartY() + t * wallDy;

        double offsetX = ballX - closestX;
        double offsetY = ballY - closestY;
        double distance = Math.hypot(offsetX, offsetY);

        if (distance > 0.0 && distance < ball.getRadius()) {
            double pushNormalX = offsetX / distance;
            double pushNormalY = offsetY / distance;

            ball.setPositionX(closestX + pushNormalX * (ball.getRadius() + CONTACT_EPSILON));
            ball.setPositionY(closestY + pushNormalY * (ball.getRadius() + CONTACT_EPSILON));

            double velocityX = ball.getVelocityX();
            double velocityY = ball.getVelocityY();
            double vDotN = velocityX * pushNormalX + velocityY * pushNormalY;

            if (vDotN < 0.0) {
                if (wall instanceof SpringWall springWall) {
                    double influence = 0.1;
                    double kick = springWall.getKickForce();
                    ball.setVelocityX(velocityX * influence + pushNormalX * kick);
                    ball.setVelocityY(velocityY * influence + pushNormalY * kick);
                } else {
                    double elasticity = clamp(wall.getBounciness(), 0.0, 1.0);
                    ball.setVelocityX(velocityX - (1.0 + elasticity) * vDotN * pushNormalX);
                    ball.setVelocityY(velocityY - (1.0 + elasticity) * vDotN * pushNormalY);
                }
            }
        }
    }

    private double rayIntersectLine(
            double originX,
            double originY,
            double directionX,
            double directionY,
            double maxDistance,
            double lineStartX,
            double lineStartY,
            double lineEndX,
            double lineEndY,
            double[] hitPoint) {
        double lineX = lineEndX - lineStartX;
        double lineY = lineEndY - lineStartY;
        double denominator = cross(directionX, directionY, lineX, lineY);

        if (Math.abs(denominator) < 1.0E-12) {
            return NO_HIT;
        }

        double startDiffX = lineStartX - originX;
        double startDiffY = lineStartY - originY;
        double t = cross(startDiffX, startDiffY, lineX, lineY) / denominator;
        double u = cross(startDiffX, startDiffY, directionX, directionY) / denominator;

        if (t < 0.0 || t > maxDistance || u < 0.0 || u > 1.0) {
            return NO_HIT;
        }

        hitPoint[0] = originX + directionX * t;
        hitPoint[1] = originY + directionY * t;
        return t;
    }

    private double cross(double ax, double ay, double bx, double by) {
        return ax * by - ay * bx;
    }

    private double rayIntersectCircle(
            double originX,
            double originY,
            double directionX,
            double directionY,
            double maxDistance,
            double centerX,
            double centerY,
            double radius) {
        double lx = centerX - originX;
        double ly = centerY - originY;
        double tca = lx * directionX + ly * directionY;

        if (tca < 0.0) {
            return NO_HIT;
        }

        double lMagSq = lx * lx + ly * ly;
        double radiusSq = radius * radius;
        double thcSq = radiusSq - lMagSq + tca * tca;

        if (lMagSq < radiusSq) {
            double hit = tca - Math.sqrt(Math.max(thcSq, 0.0));
            return hit >= 0.0 ? hit : NO_HIT;
        }

        if (thcSq < 0.0) {
            return NO_HIT;
        }

        double hit = tca - Math.sqrt(thcSq);
        if (hit < 0.0 || hit > maxDistance) {
            return NO_HIT;
        }

        return hit;
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}