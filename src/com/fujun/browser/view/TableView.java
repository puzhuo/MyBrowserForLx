package com.fujun.browser.view;

import com.kukuai.daohang.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class TableView extends View {

	private Context context;
	
	private String[] contents;
	private String[] urls;
	private int contentsSize;
	
	public final static int SHADERMODE_HORIZONTAL = 0;
	public final static int SHADERMODE_VERTICAL = 1;
	
	private Paint paint;
	
	private LinearGradient horizontalLine;
	private LinearGradient verticalLine;
	
	private int width;
	private int height;
	
	private int cellWidth;
	private int cellHeight;
	
	private int cellPadding;
	
	private Rect bound;
	
	private OnContentClickListener onContentClickListener;
	public interface OnContentClickListener{
		public void onContentClick(String url);
	};
	
	private GestureDetector gestureDetector;
	private SimpleOnGestureListener simpleGestureListener = new SimpleOnGestureListener(){
		@Override
		public boolean onDown(MotionEvent e){
			return true;
		}
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e){
			if(contents != null){
				int clickX = (int) Math.floor(e.getX() / cellWidth);
				int clickY = (int) Math.floor(e.getY() / cellHeight);
				
				int position = clickX + (clickY * 4);
				if(onContentClickListener != null && position < urls.length){
					onContentClickListener.onContentClick(urls[position]);
				}
			}
			return true;
		}
	};
	
	private OnTouchListener gestureHandler = new OnTouchListener(){
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return gestureDetector.onTouchEvent(event);
		}
	};
	
	public TableView(Context context){
		this(context, null);
	}
	
	public TableView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}
	
	public TableView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		
		this.context = context;
		
		gestureDetector = new GestureDetector(context, simpleGestureListener);
		setOnTouchListener(gestureHandler);
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TableView);
		paint.setTextSize(ta.getDimension(R.styleable.TableView_android_textSize, context.getResources().getDimension(R.dimen.nav_page_bottomlist_table_text_size)));
		paint.setColor(ta.getColor(R.styleable.TableView_android_textColor, 0xFF666666));
		cellPadding = ta.getDimensionPixelOffset(R.styleable.TableView_cellPadding, context.getResources().getDimensionPixelOffset(R.dimen.nav_page_bottomlist_table_cell_padding));
		ta.recycle();
		
		horizontalLine = new LinearGradient(0, 0, 4, 0, new int[]{0xFF999999, 0xFF999999, 0x0, 0x0}, new float[]{0F, 0.5F, 0.5F, 1F}, TileMode.REPEAT);
		verticalLine = new LinearGradient(0, 0, 0, 4, new int[]{0xFF999999, 0xFF999999, 0x0, 0x0}, new float[]{0F, 0.5F, 0.5F, 1F}, TileMode.REPEAT);
		
		bound = new Rect();
		paint.getTextBounds("园", 0, 1, bound);
		cellHeight = bound.height() + cellPadding + cellPadding;
	}
	
	public void setOnContentClickListener(OnContentClickListener onContentClickListener){
		this.onContentClickListener = onContentClickListener;
	}
	
	public OnContentClickListener getOnContentClickListener(){
		return onContentClickListener;
	}
	
	public void setContents(String[] contents, String[] urls){
		this.contents = contents;
		this.urls = urls;
		contentsSize = contents.length;
		
		invalidate();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		width = MeasureSpec.getSize(widthMeasureSpec);
		height = MeasureSpec.getSize(heightMeasureSpec);
		if(contents != null){
			int heightCount = (int) (Math.ceil(contentsSize / 4) + 1);
			if(contentsSize % 4 == 0){
				heightCount --;
			}
			int intrinsicHeight = (int) (heightCount * cellHeight) + getPaddingTop() + getPaddingBottom();
			if(height < intrinsicHeight || MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED){
				setMeasuredDimension(width, intrinsicHeight);
			}
		}
		
		width = width - getPaddingLeft() - getPaddingRight();
		height = height - getPaddingTop() - getPaddingBottom();
		
		cellWidth = width / 4;
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		
		if(contents != null){
			for(int i = 0; i < contentsSize; i++){
				int left = (i % 4 * cellWidth) + getPaddingLeft();
				int top = (int) (Math.ceil(i / 4) * cellHeight) + getPaddingTop();
				
				paint.setShader(null);
				paint.getTextBounds(contents[i], 0, contents[i].length(), bound);
				canvas.drawText(contents[i], left + (cellWidth - bound.width()) / 2, top + cellHeight - (cellHeight - bound.height()) / 2, paint);
				int offset = contentsSize % 4;
				if(offset == 0) offset = 4;
				
				if(i < contentsSize - offset) drawLine(canvas, SHADERMODE_HORIZONTAL, left, top + cellHeight, cellWidth);
				if((i + 1) % 4 != 0) drawLine(canvas, SHADERMODE_VERTICAL, left + cellWidth, top, cellHeight);
			}
		}
	}
	
	private void drawLine(Canvas canvas, int orientation, int startX, int startY, int length){
		if(orientation == 0){
			paint.setShader(horizontalLine);
			canvas.drawRect(startX,  startY, startX + length, startY + 1, paint);
		}else{
			paint.setShader(verticalLine);
			canvas.drawRect(startX, startY, startX + 1, startY + length, paint);
		}
	}
}