package com.lemhell.beziercollage.bezier;

import android.graphics.PointF;

import com.lemhell.beziercollage.bezier.utils.GridManager;

public class BezierPoint extends PointF {
    private static final int SENSITIVITY = 50;
    public static final int RADIUS = 15;

    public float left;
    public float top;
    float right;
    float bottom;
    private boolean isSelected = false;

    public float getX() {return x;}
    public float getY() {return y;}

    public BezierPoint(float x, float y) {
        super(x, y);
        setBorders();
    }

    public void move(float newX, float newY) {
        if (isSelected) {
            this.x = newX;
            this.y = newY;
            setBorders();
        }
    }

    public void moveWithoutSelection (float newX, float newY) {
        if (newX > GridManager.STEPX * 4) newX = GridManager.STEPX * 4;
        if (newY > GridManager.STEPX * 4) newY = GridManager.STEPX * 4;
        if (newX < 0) newX = 0;
        if (newY < 0) newY = 0;
        this.x = newX;
        this.y = newY;
        setBorders();
    }

    public void setBorders() {
        left = x - RADIUS - SENSITIVITY;
        top = y - RADIUS - SENSITIVITY;
        right = x + RADIUS + SENSITIVITY;
        bottom = y + RADIUS + SENSITIVITY;
    }

    public boolean isSelected(float x, float y) {
        isSelected = x > left && x < right && y < bottom && y > top;
        return isSelected;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String toString() {
        return "x: " + x + " y: " + y;
    }
}