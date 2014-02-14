
package com.fujun.browser.view;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.fujun.browser.model.NaviItem;
import com.fujun.browser.utils.Utils;
import com.kukuai.daohang.R;

public class NaviItemLayout extends View {

	public interface onNaviItemClickListener {
		public void onNaviItemClick(NaviItem item);
	}

	private static final int MIN_TOUCH_GAP = 20;
	private static final int DEFAULT_COUNT_IN_ROW = 6;

	private int mCountInRow = DEFAULT_COUNT_IN_ROW;
	private int mRowCount;
	private int mTextWidth;
	private int mTextHeight;
	private int mRowHeight;

	private boolean mClosed = true;
	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private Rect mTempRect = new Rect();

	private ArrayList<NaviItem> mContent;

	private int mLastX;
	private int mLastY;

	private int mDefaultColor;
	private int mPaddingTop;

	private Rect mBitmapRect = new Rect();
	private Bitmap mBitmap;
	private int mOpenDrawableId;
	private int mCloseDrawableId;
	private boolean mWithImage = false;

	private boolean mTouchDown = false;
	private int mTouchDownPosition = -1;
	private Bitmap mHighlightBitmap;
	private Rect mTouchDownRect = new Rect();
	private onNaviItemClickListener mListener;

	public NaviItemLayout(Context context) {
		super(context);
		init(context, null);
	}

	public NaviItemLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public NaviItemLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		Resources resources = context.getResources();
		if (attrs != null) {
			TypedArray array = context.obtainStyledAttributes(attrs,
					R.styleable.NaviItemLayout);

			int textSize = resources.getDimensionPixelSize(array.getResourceId(
					R.styleable.NaviItemLayout_textSize,
					R.dimen.navi_item_text_size));
			mPaint.setTextSize(textSize);

			mCountInRow = array.getInt(R.styleable.NaviItemLayout_countInRow,
					DEFAULT_COUNT_IN_ROW);

			int id = array.getResourceId(
					R.styleable.NaviItemLayout_highlightBitmap,
					R.drawable.common_paragraph_bg_p);
			setHighlightBitmap(id);

			mDefaultColor = resources.getColor(array.getResourceId(
					R.styleable.NaviItemLayout_defaultTextColor,
					R.color.text_color_normal));
			mPaint.setColor(mDefaultColor);

			mWithImage = array.getBoolean(R.styleable.NaviItemLayout_withImage,
					true);
			mOpenDrawableId = array.getResourceId(
					R.styleable.NaviItemLayout_openDrawable,
					R.drawable.indicator_open);
			mCloseDrawableId = array.getResourceId(
					R.styleable.NaviItemLayout_closeDrawable,
					R.drawable.indicator_close);
			setBitmap(mCloseDrawableId);
			array.recycle();
		} else {
			mDefaultColor = resources.getColor(R.color.text_color_normal);
			mPaint.setColor(mDefaultColor);
		}
	}

	public void setOnNaviItemClickListener(onNaviItemClickListener listener) {
		mListener = listener;
	}

	public void setWithImage(boolean withImage) {
		mWithImage = withImage;
		invalidate();
	}

	public void setCountInRow(int num) {
		mCountInRow = num;
		invalidate();
	}

	public void setTextSize(float textSize) {
		mPaint.setTextSize(textSize);
		invalidate();
	}

	public void setBitmap(Bitmap bitmap) {
		mBitmap = bitmap;
		invalidate();
	}

	public void setBitmap(int resId) {
		if (resId < 0) {
			return;
		}
		mBitmap = ((BitmapDrawable) getContext().getResources().getDrawable(
				resId)).getBitmap();
		invalidate();
	}

	public void setHighlightBitmap(Bitmap bitmap) {
		mHighlightBitmap = bitmap;
	}

	public void setHighlightBitmap(int resId) {
		if (resId < 0) {
			return;
		}
		Drawable drawable = getContext().getResources().getDrawable(resId);
		mHighlightBitmap = Utils.drawable2Bitmap(drawable);
		invalidate();
	}

	public void setContent(ArrayList<NaviItem> content) {
		mContent = content;
		invalidate();
	}

	private int getPosition(int x, int y) {
		int position = 0;
		int i = x / mTextWidth;
		int j = y / (mRowHeight);
		if (mBitmapRect.contains(x, y) || (x > mBitmapRect.left)
				|| (j * mCountInRow + i) >= mContent.size()) {
			position = -1;
		} else {
			position = j * mCountInRow + i;
		}
		return position;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastX = x;
				mLastY = y;
				mTouchDown = true;
				mTouchDownPosition = getPosition(x, y);
				if (mTouchDownPosition > 0) {
					int i = mTouchDownPosition % mCountInRow;
					int j = mTouchDownPosition / mCountInRow;
					int left = i * mTextWidth;
					int top = j * mRowHeight;
					mTouchDownRect.set(left, top, left + mTextWidth, top
							+ mRowHeight);
					invalidate();
				}
				return true;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mTouchDown = false;
				mTouchDownPosition = -1;
				mTouchDownRect.setEmpty();

				if (Math.abs(x - mLastX) > MIN_TOUCH_GAP || Math.abs(y - mLastY) > MIN_TOUCH_GAP) {
					invalidate();
					return true;
				}
				int position = getPosition(x, y);
				if (position > 0) {
					if (mListener != null) {
						mListener.onNaviItemClick(mContent.get(position));
					}
				} else {
					mClosed = mClosed ? false : true;
					setBitmap(mClosed ? mCloseDrawableId : mOpenDrawableId);
					requestLayout();
				}
				invalidate();
				return true;
		}

		return super.onTouchEvent(event);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		mPaddingTop = getPaddingTop();
		if (mContent != null && mContent.size() > 0) {
			mTextWidth = (mWithImage && mBitmap != null) ? (widthSpecSize - mBitmap
					.getWidth()) / mCountInRow
					: widthSpecSize / mCountInRow;

			// use mBitmapRect for a sec. later we will reassign it.
			String testStr = getContext().getString(R.string.test_words);
			mPaint.getTextBounds(testStr, 0, testStr.length(), mBitmapRect);
			mTextHeight = mBitmapRect.height();
			int width = mBitmapRect.width();
			if (width > mTextWidth) {
				mCountInRow--;
				measure(widthMeasureSpec, heightMeasureSpec);
				return;
			}
			mRowHeight = mTextHeight + mPaddingTop * 2;
			if (!mClosed) {
				mRowCount = (int) Math.ceil(mContent.size()
						/ (double) mCountInRow);
			} else {
				mRowCount = 1;
			}
			int allRowHeight = mRowCount * (mRowHeight);
			heightSpecSize = allRowHeight;

			if (mWithImage) {
				mBitmapRect.setEmpty();
				mBitmapRect.set(widthSpecSize - mBitmap.getWidth(),
						(mRowHeight - mBitmap.getHeight()) / 2, widthSpecSize,
						(mRowHeight + mBitmap.getHeight()) / 2);
			}
		}
		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}

	/*
	 * draw text in the center of a rect params text text to be drawn params x
	 * the x-coordinate of the rect's left-top corner params y the y-coordinate
	 * of the rect left-top corner
	 */
	private void drawTextToRectCenter(NaviItem item, int x, int y, Canvas canvas) {
		int left = 0;
		int top = 0;
		int width = 0;
		mTempRect.setEmpty();
		mPaint.getTextBounds(item.title, 0, item.title.length(), mTempRect);
		width = mTempRect.width();

		if (width <= mTextWidth) {
			left = (mTextWidth - width) / 2 + x;
		} else {
			int count = mPaint.breakText(item.title, true, mTextWidth, null);
			String substr = item.title.substring(0, count - 1) + "..";
			NaviItem item2 = new NaviItem(item);
			item2.title = substr;
			drawTextToRectCenter(item2, x, y, canvas);
			return;
		}

		top = y;
		if (item.color == -1) {
			canvas.drawText(item.title, left, top, mPaint);
		} else {
			mPaint.setColor(item.color);
			canvas.drawText(item.title, left, top, mPaint);
			mPaint.setColor(mDefaultColor);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if ((mHighlightBitmap != null) && mTouchDown
				&& (mTouchDownPosition != -1)) {
			mTempRect.setEmpty();
			mTempRect.set(0, 0, mHighlightBitmap.getWidth(),
					mHighlightBitmap.getHeight());
			canvas.drawBitmap(mHighlightBitmap, mTempRect, mTouchDownRect,
					mPaint);
		}

		int position = 0;
		int x = 0;
		int y = 0;
		for (int i = 0; i < mRowCount; i++) {
			if (mClosed && i > 0) {
				break;
			}
			y = (mPaddingTop + mTextHeight) * (i + 1) + i * mPaddingTop;
			for (int j = 0; j < mCountInRow; j++) {
				position = mCountInRow * i + j;
				if (position < mContent.size()) {
					x = j * mTextWidth;
					drawTextToRectCenter(mContent.get(position), x, y, canvas);
				}
			}
		}
		if (mWithImage && mBitmap != null) {
			mTempRect.setEmpty();
			mTempRect.set(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
			canvas.drawBitmap(mBitmap, mTempRect, mBitmapRect, mPaint);
		}
	}
}
