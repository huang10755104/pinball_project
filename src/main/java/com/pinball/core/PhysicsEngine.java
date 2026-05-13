package com.pinball.core;

public interface PhysicsEngine {
    void update(double deltaTime);

    void checkCollision();
}
