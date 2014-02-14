
package com.fujun.browser.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

public class MyViewPager extends ViewPager {

	public interface onMoveToWebViewListener {
		public void onMoveToWebView();
	}

	private static final int MIN_MOVE_DISTANCE = 20;
	private static final int MIN_SLIDE_DISTANCE = 150;

	private int mLastX;
	private onMoveToWebViewListener mListener;

	public MyViewPager(Context context) {
		super(context);
	}

	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setOnMoveToWebViewListener(onMoveToWebViewListener listener) {
		mListener = listener;
	}

	/*
	 * @Override public boolean onInterceptTouchEvent(MotionEvent event) { if
	 * (getCurrentItem() == getAdapter().getCount() - 1) { int action =
	 * event.getAction(); int x = (int) event.getX(); switch (action) { case
	 * MotionEvent.ACTION_DOWN: mLastX = x; break; case MotionEvent.ACTION_MOVE:
	 * if (mLastX - x > MIN_MOVE_DISTANCE) { return true; } break; case
	 * MotionEvent.ACTION_UP: return x < mLastX; } } return
	 * super.onInterceptTouchEvent(event); }
	 * @Override public boolean onTouchEvent(MotionEvent event) { int action =
	 * event.getAction(); int x = (int) event.getX(); switch (action) { case
	 * MotionEvent.ACTION_UP: // sliding to left if (mLastX - x >
	 * MIN_SLIDE_DISTANCE && mListener != null) { mListener.onMoveToWebView();
	 * return true; } default: break; } return super.onTouchEvent(event); }
	 */

}
