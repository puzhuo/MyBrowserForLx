
package com.fujun.browser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.fujun.browser.Tab;

public class SlidingWebView extends WebView {

	public interface OnWebViewMoveListener {
		public void onWebViewMove();

		public void onLoadingStateChanged(boolean loading);
	}

	private static final int MIN_SLIDE_DISTANCE = 250;

	private int mLastX;
	private Tab mTab;
	private OnWebViewMoveListener mListener;

	public void setOnWebViewMoveListener(OnWebViewMoveListener listener) {
		mListener = listener;
	}

	public SlidingWebView(Context context) {
		super(context);
	}

	public SlidingWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SlidingWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setTab(Tab tab) {
		mTab = tab;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public void goForward() {
		super.goForward();
		if (mListener != null) {
			mListener.onWebViewMove();
		}
	}

	@Override
	public void goBack() {
		super.goBack();
		if (mListener != null) {
			mListener.onWebViewMove();
		}
	}

	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// int action = event.getAction();
	// int x = (int) event.getX();
	// switch (action) {
	// case MotionEvent.ACTION_DOWN:
	// mLastX = x;
	// break;
	// case MotionEvent.ACTION_MOVE:
	// break;
	// case MotionEvent.ACTION_UP:
	// if (mLastX - x > MIN_SLIDE_DISTANCE && canGoForward()) {
	// // sliding to left
	// goForward();
	// return true;
	// } else if (x - mLastX > MIN_SLIDE_DISTANCE) {
	// // sliding to right
	// if (canGoBack()) {
	// goBack();
	// } else {
	// if (mTab != null) {
	// mTab.setWebViewVisible(false);
	// }
	// if (mListener != null) {
	// mListener.onWebViewMove();
	// }
	// }
	// return true;
	// }
	// break;
	// }
	// return super.onTouchEvent(event);
	// }
}
