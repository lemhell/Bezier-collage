package com.lemhell.beziercollage.filter.gestures;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.lemhell.beziercollage.filter.math.Vector2D;


public class SandboxView extends View implements OnTouchListener {

	private Bitmap bitmap;
	private final int width;
	private final int height;
	private Matrix transform = new Matrix();

	private Vector2D position = new Vector2D();
	private float scale = 1;
	private float angle = 0;

	private TouchManager touchManager = new TouchManager(2);
	private boolean isInitialized = false;
	private boolean isRotatable = false;

	private Paint paint;

	public SandboxView(Context context, Bitmap bitmap) {
		super(context);

		this.bitmap = bitmap;
		this.width = bitmap.getWidth()/4;
		this.height = bitmap.getHeight()/4;
		paint = new Paint();
		setOnTouchListener(this);
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		invalidate();
	}

	public Bitmap getBitmap() {
		return Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth() * scale), (int)(bitmap.getHeight() * scale), false);
	}


	private static float getDegreesFromRadians(float angle) {
		return (float)(angle * 180.0 / Math.PI);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (!isInitialized) {
			int w = getWidth()/4;
			int h = getHeight()/4;
			position.set(w / 2, h / 2);
			isInitialized = true;
		}

		transform.reset();
		transform.postTranslate(-width / 2.0f, -height / 2.0f);
		if (isRotatable)
			transform.postRotate(getDegreesFromRadians(angle));
		if (scale <= 0.5) scale = 0.5f;
		if (scale >= 1.5) scale = 1.5f;
		transform.postScale(scale, scale);
		transform.postTranslate(position.getX(), position.getY());
		canvas.drawBitmap(bitmap, transform, paint);
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		try {
			touchManager.update(event);

			if (touchManager.getPressCount() == 1) {
				position.add(touchManager.moveDelta(0));
			}
			else {
				if (touchManager.getPressCount() == 2) {
					Vector2D current = touchManager.getVector(0, 1);
					Vector2D previous = touchManager.getPreviousVector(0, 1);
					float currentDistance = current.getLength();
					float previousDistance = previous.getLength();

					if (previousDistance != 0) {
						scale *= currentDistance / previousDistance;
					}

					angle -= Vector2D.getSignedAngleBetween(current, previous);
				}
			}

			invalidate();
		}
		catch(Throwable t) {
		}
		return true;
	}

}
