package com.lemhell.beziercollage.bezier;

import android.graphics.Color;
import android.graphics.Paint;

import java.util.Observable;

public class BezierCurve extends Observable {
    public BezierPoint point1, point2, controlPoint;
    public Paint controlPaint, normalPaint;

    public BezierCurve(BezierPoint point1, BezierPoint controlPoint, BezierPoint point2) {
        this.point1 = point1;
        this.point2 = point2;
        this.controlPoint = controlPoint;
        normalPaint = initPaint(Color.argb(50, 200, 255, 200));
        controlPaint = initPaint(Color.argb(50, 255, 255, 200));
    }

    private Paint initPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        return paint;
    }

    public boolean checkPoints(float x, float y) {
        boolean sthSelected = point1.isSelected(x, y);
        if (!sthSelected)
            sthSelected = point2.isSelected(x, y);
        if (!sthSelected)
            sthSelected = controlPoint.isSelected(x, y);
        return sthSelected;
    }

    public void move(float x, float y) {
        point1.move(x, y);
        point2.move(x, y);
        controlPoint.move(x, y);
        notifyObservers();
    }

    public void moveBorder(float dx, float dy) {
        point1.moveWithoutSelection(point1.getX() + dx, point1.getY() + dy);
        point2.moveWithoutSelection(point2.getX() + dx, point2.getY() + dy);
        controlPoint.moveWithoutSelection(controlPoint.getX() + dx, controlPoint.getY() + dy);
    }

    public void resetSelected() {
        point1.setSelected(false);
        point2.setSelected(false);
        controlPoint.setSelected(false);
    }

    public String toString() {
        return "p1: " + point1.x + " " + point1.y + " p2: " + point2.x + " " + point2.y +
                " c: " + controlPoint.x + " " + controlPoint.y;
    }
}