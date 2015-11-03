package com.lemhell.beziercollage.bezier;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import com.lemhell.beziercollage.R;
import com.lemhell.beziercollage.bezier.utils.CurveHolder;
import com.lemhell.beziercollage.bezier.utils.GridManager;
import com.lemhell.beziercollage.bezier.utils.ListHolder;
import com.lemhell.beziercollage.filter.math.Polygon;

import java.util.ArrayList;
import java.util.Iterator;

@SuppressLint("ViewConstructor")
public class BezierView extends View {
    public float mLastTouchX = 0, mLastTouchY = 0, mPosX, mPosY, lastPosX, lastPosY;
    public boolean sthSelected = false, isBallsEnabled = true,
        isUpdated = false, isActive = true, isBoldStrokeWidth = false,
        isStatic = true, curveEnabled = true;
    public int mScreenWidth, mScreenHeight;

    public Polygon polygon;
    public String picturePath;
    public CurveHolder curveHolder;

    private Paint paint;
    private Path paths;
    private Bitmap imageToClip;

    public BezierView(Context context, int width, int height, ListHolder holder, GridManager manager) {
        super(context);
        this.setDrawingCacheEnabled(true);
        init(width, height, holder, manager);
    }

    private void init(final int width, final int height,
                      ListHolder holder, GridManager manager) {
        paint = new Paint();
        paths = new Path();
        imageToClip = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(),
                R.drawable.blank), 300, 300, false);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(10f); // 2.5f
        paint.setAntiAlias(true);
        ArrayList<BezierPoint> points = convertListHolderToPoints(holder, manager);
        post(() -> {
            mScreenWidth = width;
            mScreenHeight = height;
        });
        curveHolder = new CurveHolder(points);
        polygon = new Polygon(points);
        lastPosX = (mScreenWidth - imageToClip.getWidth()) / 2;
        lastPosY = (mScreenHeight - imageToClip.getHeight()) / 2;
    }

    private ArrayList<BezierPoint> convertListHolderToPoints(ListHolder holder, GridManager manager) {
        ArrayList<BezierPoint> points = new ArrayList<>();
        BezierPoint point;
        for (int i = 0; i < holder.size(); i++) {
            float x = holder.x.get(i), y = holder.y.get(i);
            point = manager.getPoint((int)x, (int)y);
            points.add(point);
        }
        return points;
    }

    public String getPicturePath() {
        if (picturePath != null)
            return picturePath;
        else
            return "null";
    }

    public void resetPath() {
        curveHolder.resetCurves();
        invalidate();
    }

    boolean first = true;

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        Iterator<BezierCurve> iterator;
        if (isCurveEnabled()) {
            iterator = curveHolder.curves.iterator();
            paths.reset();
            first = true;
            BezierCurve curve;
            for (; iterator.hasNext();) {
                curve = iterator.next();
                if (first) {
                    first = false;
                    paths.moveTo(curve.point1.x, curve.point1.y);
                }
                paths.quadTo(curve.controlPoint.x , curve.controlPoint.y,
                        curve.point2.x, curve.point2.y);
            }
        }
        canvas.clipPath(paths);
        if (isActive) {
            canvas.translate(mPosX, mPosY);
            lastPosX = mPosX;
            lastPosY = mPosY;
            if (imageToClip != null) {
                canvas.translate((mScreenWidth - imageToClip.getWidth()) / 2,
                        (mScreenHeight - imageToClip.getHeight()) / 2);
            }
        }
        assert imageToClip != null;
        canvas.drawBitmap(imageToClip, 0, 0, null);
        if (isCurveEnabled()) {
            canvas.translate(-(mScreenWidth - imageToClip.getWidth()) / 2 - mPosX,
                    -(mScreenHeight - imageToClip.getHeight()) / 2 - mPosY);
            canvas.drawPath(paths, paint);
            canvas.translate((mScreenWidth - imageToClip.getWidth()) / 2 + mPosX,
                    (mScreenHeight - imageToClip.getHeight()) / 2 + mPosY);
            canvas.restore();
            drawBalls(canvas);
        }
    }

    private void drawBalls(Canvas canvas) {
        Iterator<BezierCurve> iterator;
        if (isBallsEnabled && !isStatic) {
            iterator = curveHolder.curves.iterator();
            for (; iterator.hasNext(); ) {
                BezierCurve curve = iterator.next();
                canvas.drawCircle(curve.point1.x, curve.point1.y,
                        BezierPoint.RADIUS, curve.normalPaint);
                canvas.drawCircle(curve.point2.x, curve.point2.y,
                        BezierPoint.RADIUS, curve.normalPaint);
                canvas.drawCircle(curve.controlPoint.x, curve.controlPoint.y,
                        BezierPoint.RADIUS, curve.controlPaint);
            }
        }
    }


    public boolean isCurveEnabled() {
        return curveEnabled;
    }

    public void setImageToClip(Bitmap imageToClip) {
        this.imageToClip = imageToClip;
        invalidate();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(GridManager.STEPX * 4, GridManager.STEPX * 4);
    }

    public void disableCurve() {
        setCurveEnabled(false);
        invalidate();
    }

    public void enableCurve() {
        setCurveEnabled(true);
        invalidate();
    }

    private void setCurveEnabled(boolean curveEnabled) {
        this.curveEnabled = curveEnabled;
    }

    public int getSelectedCurveId() {
        int t = 0;
        for (BezierCurve c : curveHolder.curves) {
            if (c.point1.getIsSelected() || c.point2.getIsSelected() || c.controlPoint.getIsSelected()) {
                return t;
            }
            t++;
        }
        return -1;
    }
}