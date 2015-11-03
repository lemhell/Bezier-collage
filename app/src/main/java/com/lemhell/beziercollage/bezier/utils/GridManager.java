package com.lemhell.beziercollage.bezier.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.lemhell.beziercollage.bezier.BezierPoint;
import com.lemhell.beziercollage.bezier.BezierView;

import java.util.ArrayList;

public class GridManager {

    public static int STEPX = 300;

    BezierPoint[][] allPoints;

    ArrayList<BezierView> views;

    public GridManager(Context context) {
        setScreenDimens(context);

        allPoints = new BezierPoint[34][34];
        for (int j = 0; j < 34; j++) {
            for (int i = 0; i < 34; i++) {
                allPoints[i][j] = new BezierPoint(i * STEPX / 8, j * STEPX / 8);
            }
        }
        views = new ArrayList<>();
    }

    void setScreenDimens(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
    }

    public void updateViews() {
        for (BezierView view : views) {
            if (!view.isUpdated) {
                view.invalidate();
            }
        }
    }

    public void setViews(ArrayList<BezierView> views) {
        this.views = views;
    }

    public BezierPoint getPoint(int i, int j) {
        return allPoints[i][j];
    }

}
