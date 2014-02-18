package com.fujun.browser.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FitWidthImageView extends ImageView {
	
	private float sourceWidth;
	private float sourceHeight;
	private float width = -1;
	private float height = -1;

	public FitWidthImageView(Context context){
		this(context, null);
	}
	
	public FitWidthImageView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}
	
	public FitWidthImageView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		
		this.setScaleType(ScaleType.MATRIX);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if(width != -1 || height != -1){
			setMeasuredDimension((int) width, (int) height);
		}else{
			width = MeasureSpec.getSize(widthMeasureSpec);
			height = MeasureSpec.getSize(heightMeasureSpec);
		}
	}
	
	@Override
	public void setImageBitmap(Bitmap source){
		super.setImageBitmap(source);
		sourceWidth = source.getWidth();
		sourceHeight = source.getHeight();
		
		Matrix matrix = new Matrix();
		float scale = width / sourceWidth;
		matrix.postScale(scale, scale, 0, 0);
		setImageMatrix(matrix);
		height = sourceHeight * scale;
		requestLayout();
	}
}
