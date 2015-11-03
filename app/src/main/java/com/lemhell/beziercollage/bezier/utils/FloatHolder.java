package com.lemhell.beziercollage.bezier.utils;

import com.lemhell.beziercollage.bezier.BezierPoint;

import java.util.ArrayList;

public class FloatHolder {
    public ArrayList<Float> x, y;
    public FloatHolder(ArrayList<BezierPoint> points) {
        x = new ArrayList<>();
        y = new ArrayList<>();

        for (BezierPoint p : points) {
            x.add(p.getX());
            y.add(p.getY());
        }
    }
}
