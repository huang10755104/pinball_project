package scripts;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class CollisionEngine {
    private Circle ball;
    public double speedX = 0;
    public double speedY = 0;

    public CollisionEngine(Circle ball) {
        this.ball = ball;
    }

    /**
     * 通用向量反射計算 (分離法向與切向速度)
     */
    public void resolveCollision(double nx, double ny, double bounciness) {
        double len = Math.sqrt(nx * nx + ny * ny);
        if (len == 0) return;
        nx /= len;
        ny /= len;

        double dotProduct = speedX * nx + speedY * ny;
        
        // 若內積大於0，表示物體已經在遠離表面，不執行反彈
        if (dotProduct > 0) return;

        double normalVelX = dotProduct * nx;
        double normalVelY = dotProduct * ny;
        double tangentVelX = speedX - normalVelX;
        double tangentVelY = speedY - normalVelY;

        speedX = tangentVelX - normalVelX * bounciness;
        speedY = tangentVelY - normalVelY * bounciness;
    }

    /**
     * 無限延伸平面碰撞
     */
    public boolean checkPlaneCollision(double nx, double ny, double distanceToOrigin, double bounciness) {
        double currentDist = ball.getCenterX() * nx + ball.getCenterY() * ny - distanceToOrigin;
        if (currentDist < ball.getRadius()) {
            double overlap = ball.getRadius() - currentDist;
            ball.setCenterX(ball.getCenterX() + nx * overlap);
            ball.setCenterY(ball.getCenterY() + ny * overlap);
            resolveCollision(nx, ny, bounciness);
            return true;
        }
        return false;
    }

    /**
     * 線段碰撞 (加入法向量反轉防穿透保護)
     */
    public boolean checkLineSegmentCollision(double x1, double y1, double x2, double y2, double bounciness) {
        double currentX = ball.getCenterX();
        double currentY = ball.getCenterY();
        double radius = ball.getRadius();

        double abX = x2 - x1;
        double abY = y2 - y1;
        double acX = currentX - x1;
        double acY = currentY - y1;

        double abLengthSq = abX * abX + abY * abY;
        if (abLengthSq == 0) return false; 
        double t = (acX * abX + acY * abY) / abLengthSq;
        t = Math.max(0, Math.min(1, t));

        double closestX = x1 + t * abX;
        double closestY = y1 + t * abY;
        
        double dx = currentX - closestX;
        double dy = currentY - closestY;

        // 【關鍵修正】：如果法向量與速度同向，代表球因為速度太快已穿過中心線
        // 強制反轉法向量，確保球被推回原本的那一面！
        if (dx * speedX + dy * speedY > 0) {
            dx = -dx;
            dy = -dy;
        }

        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < radius) {
            if (distance > 0) {
                ball.setCenterX(closestX + (dx / distance) * radius);
                ball.setCenterY(closestY + (dy / distance) * radius);
            }
            resolveCollision(dx, dy, bounciness);
            return true;
        }
        return false;
    }

    public boolean checkLineCollision(Line line, double bounciness) {
        return checkLineSegmentCollision(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY(), bounciness);
    }

    /**
     * 矩形碰撞 (AABB)
     */
    public boolean checkRectangleCollision(Rectangle rect, double bounciness) {
        double cx = ball.getCenterX();
        double cy = ball.getCenterY();
        double radius = ball.getRadius();
        
        double rx = rect.getX();
        double ry = rect.getY();
        double rw = rect.getWidth();
        double rh = rect.getHeight();

        double closestX = Math.max(rx, Math.min(cx, rx + rw));
        double closestY = Math.max(ry, Math.min(cy, ry + rh));
        
        double dx = cx - closestX;
        double dy = cy - closestY;

        // 【關鍵修正】：防穿透機制
        if (dx * speedX + dy * speedY > 0) {
            dx = -dx;
            dy = -dy;
        }

        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < radius) {
            if (distance > 0) {
                ball.setCenterX(closestX + (dx / distance) * radius);
                ball.setCenterY(closestY + (dy / distance) * radius);
            }
            resolveCollision(dx, dy, bounciness);
            return true;
        }
        return false;
    }

    /**
     * 圓形碰撞 (Bumpers)
     */
    public boolean checkCircleCollision(Circle target, double bounciness) {
        double dx = ball.getCenterX() - target.getCenterX();
        double dy = ball.getCenterY() - target.getCenterY();
        
        // 【關鍵修正】：防穿透機制
        if (dx * speedX + dy * speedY > 0) {
            dx = -dx;
            dy = -dy;
        }
        
        double distance = Math.sqrt(dx * dx + dy * dy);
        double minDist = ball.getRadius() + target.getRadius();

        if (distance < minDist) {
            if (distance > 0) {
                ball.setCenterX(target.getCenterX() + (dx / distance) * minDist);
                ball.setCenterY(target.getCenterY() + (dy / distance) * minDist);
            }
            resolveCollision(dx, dy, bounciness);
            return true;
        }
        return false;
    }

    public void updatePosition(double gravity, double friction) {
        speedY += gravity;
        speedX *= friction;
        speedY *= friction;

        ball.setCenterX(ball.getCenterX() + speedX);
        ball.setCenterY(ball.getCenterY() + speedY);
    }
}