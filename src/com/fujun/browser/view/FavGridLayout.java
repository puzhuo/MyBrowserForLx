
package com.fujun.browser.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.kukuai.daohang.R;

public class FavGridLayout extends ViewGroup {

	private static final int DEFAULT_COLUMN_COUNT = 3;
	private static final int DEFAULT_ROW_COUNT = 3;

	private static int mCellCount;
	private int mColumnCount = DEFAULT_COLUMN_COUNT;
	private int mRowCount = DEFAULT_ROW_COUNT;
	private int mCellHorizontalGap;
	private int mCellVerticalGap;

	private static final int INVAILD_INT = -1;
	private int mCellWidth = INVAILD_INT;
	private int mCellHeight = INVAILD_INT;

	public FavGridLayout(Context context) {
		super(context);
		init(context, null);
	}

	public FavGridLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public FavGridLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		int defHorizontalGap = context.getResources().getDimensionPixelSize(
				R.dimen.grid_view_horizontal_space);
		int defVerticalGap = context.getResources().getDimensionPixelSize(
				R.dimen.grid_view_vertical_space);
		if (attrs != null) {
			TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FavGrid);
			mColumnCount = array.getInt(R.styleable.FavGrid_columnCount, 3);
			mRowCount = array.getInt(R.styleable.FavGrid_rowCount, 3);
			mCellHorizontalGap = array.getDimensionPixelSize(R.styleable.FavGrid_horizontalGap,
					defHorizontalGap);
			mCellVerticalGap = array.getDimensionPixelSize(R.styleable.FavGrid_verticalGap,
					defVerticalGap);
			mCellWidth = array.getDimensionPixelSize(R.styleable.FavGrid_cellWidth,
					INVAILD_INT);
			mCellHeight = array.getDimensionPixelSize(R.styleable.FavGrid_cellHeight,
					INVAILD_INT);
			array.recycle();
		} else {
			mCellHorizontalGap = defHorizontalGap;
			mCellVerticalGap = defVerticalGap;
		}
		setWillNotDraw(false);
	}

	public static int getCellCount() {
		return mCellCount;
	}

	private void setCellCount() {
		mCellCount = mColumnCount * mRowCount;
	}

	public void setColumnCount(int columnCount) {
		mColumnCount = columnCount;
		setCellCount();
	}

	public void setRowCount(int rowCount) {
		mRowCount = rowCount;
		setCellCount();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		int numWidthGaps = mColumnCount - 1;
		int numHeightGaps = mRowCount - 1;

		if (mCellWidth == INVAILD_INT) {
			mCellWidth = (widthSpecSize - getPaddingLeft() - getPaddingRight() - numWidthGaps
					* mCellHorizontalGap)
					/ mColumnCount;
		}
		if (mCellHeight == INVAILD_INT) {
			mCellHeight = (heightSpecSize - getPaddingTop() - getPaddingBottom() - numHeightGaps
					* mCellVerticalGap)
					/ mRowCount;
		}

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			int childWidthMeasureSpec = MeasureSpec
					.makeMeasureSpec(mCellWidth, MeasureSpec.EXACTLY);
			int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(mCellHeight,
					MeasureSpec.EXACTLY);
			child.measure(childWidthMeasureSpec, childheightMeasureSpec);
		}

		int height = ((count - 1) / mColumnCount) * (mCellHeight + mCellVerticalGap)
				+ getPaddingTop() + mCellHeight + getPaddingBottom();
		setMeasuredDimension(widthSpecSize, height);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();
		View child = null;
		for (int i = 0; i < count; i++) {
			child = getChildAt(i);
			int left = (i % mColumnCount) * (mCellWidth + mCellHorizontalGap) + getPaddingLeft();
			int top = (i / mColumnCount) * (mCellHeight + mCellVerticalGap) + getPaddingTop();
			child.layout(left, top, left + mCellWidth, top + mCellHeight);
		}
	}
}
