package com.lemhell.beziercollage.bezier.utils;


import com.lemhell.beziercollage.bezier.BezierCurve;
import com.lemhell.beziercollage.bezier.BezierPoint;

import java.util.ArrayList;
import java.util.LinkedList;

public class CurveHolder {
    private ArrayList<BezierPoint> points;
    private FloatHolder floatHolder;
    public LinkedList<BezierCurve> curves = new LinkedList<>();

    public CurveHolder(ArrayList<BezierPoint> points) {
        floatHolder = new FloatHolder(points);
        this.points = points;
        curves = new LinkedList<>();
        createCurvesFromPoints();
    }

    private void createCurvesFromPoints() {
        BezierCurve curve = new BezierCurve(points.get(0), points.get(1), points.get(2));
        curves.add(curve);
        for (int i = 2; i < points.size(); i += 2) {
            if (i == points.size() - 2) {
                curve = new BezierCurve(points.get(i), points.get(i + 1), points.get(0));
                curves.add(curve);
            } else {
                curve = new BezierCurve(points.get(i), points.get(i + 1), points.get(i + 2));
                curves.add(curve);
            }
        }
    }

    public void resetCurves() {
        int j = 0;
        for (BezierCurve c : curves) {
            c.point1.x = floatHolder.x.get(j);
            c.point1.y = floatHolder.y.get(j);
            c.controlPoint.x = floatHolder.x.get(j + 1);
            c.controlPoint.y = floatHolder.y.get(j + 1);
            if (j == points.size() - 2) {
                c.point2.x = floatHolder.x.get(0);
                c.point2.y = floatHolder.y.get(0);
            } else {
                c.point2.x = floatHolder.x.get(j + 2);
                c.point2.y = floatHolder.y.get(j + 2);
            }
            j+=2;
            c.point1.setBorders();
            c.point2.setBorders();
            c.controlPoint.setBorders();
        }
    }
}