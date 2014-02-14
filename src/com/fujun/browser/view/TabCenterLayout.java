
package com.fujun.browser.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kukuai.daohang.R;

public class TabCenterLayout extends LinearLayout {

	public TabCenterLayout(Context context) {
		super(context);
	}

	public TabCenterLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public TabCenterLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int top = 0;
		int center = (r - l) / 10 * 7;
		int count = getChildCount();
		View child = null;
		for (int i = 0; i < count; i++) {
			child = getChildAt(i);
			if (child instanceof ImageView && top > 0) {
				int halfWidth = child.getMeasuredWidth() / 2;
				top -= getContext().getResources().getDimensionPixelSize(
						R.dimen.tab_center_arrow_margin_top);
				child.layout(
						center - halfWidth,
						top,
						center + halfWidth,
						top + child.getMeasuredHeight());
			} else if (child instanceof LinearLayout) {
				top += child.getMeasuredHeight();
				child.layout(l, t, r, top);
			}
		}
	}
}
