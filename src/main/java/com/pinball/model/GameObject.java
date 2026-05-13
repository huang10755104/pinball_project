package com.pinball.model;

import com.pinball.core.Renderable;

public abstract class GameObject implements Renderable {
    private boolean active = true;
    private double elasticity = 0.8;

    protected GameObject() {
    }

    protected GameObject(double elasticity) {
        this.elasticity = elasticity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getElasticity() {
        return elasticity;
    }

    public void setElasticity(double elasticity) {
        this.elasticity = elasticity;
    }
}