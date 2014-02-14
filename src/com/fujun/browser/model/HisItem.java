
package com.fujun.browser.model;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

public class HisItem extends Items {

	private static final String TAG = "NaviInfo";

	public Bitmap icon;
	public long accessTime;

	public HisItem() {
	}

	public HisItem(HisItem info) {
		title = info.title;
		url = info.url;
		icon = info.icon;
		accessTime = info.accessTime;
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
		if (accessTime < 0) {
			Log.e(TAG, "position is negative");
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("HisInfo [ ").append("title = " + title + "; ")
				.append("url = " + url + "; ")
				.append("accessTime = " + accessTime + "; ")
				.append("icon = " + icon).append(" ]");
		return buffer.toString();
	}
}
