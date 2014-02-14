
package com.fujun.browser.model;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

public class FavItem extends Items {

	private static final String TAG = "FavInfo";

	public int position = -1;
	public Bitmap icon;

	public FavItem() {
	}

	public FavItem(FavItem info) {
		title = info.title;
		url = info.url;
		position = info.position;
		icon = info.icon;
	}

	public boolean isLegal() {
		if (TextUtils.isEmpty(title)) {
			Log.e(TAG, "title is null");
			return false;
		}
		if (TextUtils.isEmpty(url)) {
			Log.e(TAG, "url is null");
			return false;
		}
		if (icon == null) {
			Log.e(TAG, "bitmap is null");
			return false;
		}
		if (position < 0) {
			Log.e(TAG, "position is negative");
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("NaviInfo [ ").append("title = " + title + "; ")
				.append("url = " + url + "; ")
				.append("position = " + position + "; ")
				.append("icon = " + icon).append(" ]");
		return buffer.toString();
	}
}
