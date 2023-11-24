package com.makuzmin.apps.magnifier;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.graphics.Rect;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

public class ScrollImageView extends View {
	private final int MAX_ZOOM = 5;
	private final int DEFAULT_PADDING = 10;
	private Display mDisplay;
	private Bitmap mImage;

	/* Current x and y of the touch */
	private float mCurrentX = 0;
	private float mCurrentY = 0;

	/* The touch distance change from the current touch */
	private float mDeltaX = 0;
	private float mDeltaY = 0;
	
	private ScaleGestureDetector mScaleDetector;
	
	Rect rectSrc;
	Rect rectDst;
	
	float factor;
	
	float moveX, moveY;
	
	int mDisplayWidth;
	int mDisplayHeight;
	int mPadding;
	// int k; // negative when front camera is used
	
	ScaleFinishedListener mSFListener;
	
	public interface ScaleFinishedListener{
		public void onScaleFinished(float factor);
	}

	public ScrollImageView(Context context) {
		super(context);
		initScrollImageView(context);
	}
	public ScrollImageView(Context context, AttributeSet attributeSet) {
		super(context);
		initScrollImageView(context);
	}

	private void initScrollImageView(Context context) {
		mDisplay = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		mPadding = DEFAULT_PADDING;
		factor = 1;
		moveX = moveY = 0;
		
		try {
            // Instantiate the SelectDialogListener so we can send events to the host
            mSFListener = (ScaleFinishedListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
										 + " must implement SelectDialogListener");
        }
		
		mScaleDetector = new ScaleGestureDetector(context, new OnScaleGestureListener() {
				@Override
				public void onScaleEnd(ScaleGestureDetector detector) {

				}
				@Override
				public boolean onScaleBegin(ScaleGestureDetector detector) {
					return true;
				}
				@Override
				public boolean onScale(ScaleGestureDetector detector) {

					factor = factor + (detector.getScaleFactor() -1)/10;
	        		if (factor < 1) factor = 1;
					if (factor > MAX_ZOOM) factor = MAX_ZOOM;
					
					//other version of code above
					
				/*	factor *= detector.getScaleFactor();
					factor = Math.max(0.1f, Math.min(factor, 5.0f));
					if(factor < 1) factor = 1;
					if(factor > 10) factor = 10; */

                    invalidate();
				//	mSFListener.onScaleFinished(factor);
					return false;
				}
			});
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = measureDim(widthMeasureSpec, mDisplay.getWidth());
		int height = measureDim(heightMeasureSpec, mDisplay.getHeight());
		setMeasuredDimension(width, height);
	}

	private int measureDim(int measureSpec, int size) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = size;
            if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
            }
        }
        return result;
    }

	public Bitmap getImage() {
		return mImage;
	}

	public void setImage(Bitmap image/*, int k*/) {
		mImage = image;
		rectSrc = new Rect(0, 0, mImage.getWidth(), mImage.getHeight());
	//	this.k = k;
	}
	
	public int getPadding() {
		return mPadding;
	}

	public void setPadding(int padding) {
		this.mPadding = padding;
	}

	public boolean onTouchEvent(MotionEvent event) {
		mScaleDetector.onTouchEvent(event);
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mCurrentX = event.getRawX();
			mCurrentY = event.getRawY();
		} 
		else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			float x = event.getRawX();
			float y = event.getRawY();

			// Update how much the touch moved
			mDeltaX = x - mCurrentX;
			mDeltaY = y - mCurrentY;

			mCurrentX = x;
			mCurrentY = y;

			invalidate();
		}
		// Consume event
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mImage == null) {
			return;
		}
	
		mSFListener.onScaleFinished(factor);
		
		moveX += mDeltaX;
		moveY += mDeltaY;
		
	//	int width = mImage.getWidth();
	//	int height = mImage.getHeight();
	
	//	Log.d("myLogs", "measures = " + mImage.getWidth() + ", " + mImage.getHeight() + ", " + getMeasuredWidth() + ", " + getMeasuredHeight());
		
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		
		//destination rectangle, center after scaling
		int left = (int)((getMeasuredWidth() - width*factor)/2 + /*k**/moveX);
		int bottom = (int)((getMeasuredHeight() - height*factor)/2 + moveY);
		int right = (int)((width*factor - getMeasuredWidth())/2 + getMeasuredWidth() + /*k**/moveX);
		int top = (int)((height*factor - getMeasuredHeight())/2 + getMeasuredHeight() + moveY);	
		
	    // Don't scroll off the left or right edges of the bitmap.
    	if(left > 0) {left = 0; right = (int)(width*factor);}
		if(left < getMeasuredWidth()- width*factor) {
			left = (int)( getMeasuredWidth()- width*factor); right = getMeasuredWidth();}
		// Don't scroll off the top or bottom edges of the bitmap.
		if(bottom > 0) {bottom = 0; top = (int)(height*factor);}
		if(bottom < getMeasuredHeight()- height*factor) {
			bottom = (int)( getMeasuredHeight()- height*factor); top = getMeasuredHeight();}
			
	//	Log.d("myLogs", "rect = " + left + ", " + bottom + ", " + right + ", " + top);
		rectDst = new Rect(left,bottom,right,top);

		Paint paint = new Paint();
		canvas.drawBitmap(mImage, rectSrc, rectDst, paint);
	}
}
