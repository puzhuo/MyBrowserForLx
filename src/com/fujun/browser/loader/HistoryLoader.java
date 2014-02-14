
package com.fujun.browser.loader;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;

import com.fujun.browser.model.HisItem;
import com.fujun.browser.provider.BrowserProvider;

public class HistoryLoader extends BaseLoader<ArrayList<HisItem>> {

	public HistoryLoader(Context context) {
		super(context);
	}

	@Override
	public ArrayList<HisItem> loadInBackground() {
		final ArrayList<HisItem> list = new ArrayList<HisItem>();
		Cursor cursor = null;
		try {
			cursor = mContext.getContentResolver().query(
					BrowserProvider.TABLE_HIS_URI, null, null, null,
					BrowserProvider.TABLE_HIS_ACCESS_TIME + " desc");
			if (cursor != null) {
				mCursor = cursor;
				getContext().getContentResolver().registerContentObserver(
						BrowserProvider.TABLE_HIS_URI, true, mObserver);
				HisItem item = null;
				while (cursor.moveToNext()) {
					item = new HisItem();
					item.title = cursor
							.getString(cursor
									.getColumnIndexOrThrow(BrowserProvider.TABLE_HIS_TITLE));
					item.url = cursor
							.getString(cursor
									.getColumnIndexOrThrow(BrowserProvider.TABLE_HIS_URL));
					item.accessTime = cursor
							.getLong(cursor
									.getColumnIndexOrThrow(BrowserProvider.TABLE_HIS_ACCESS_TIME));
					byte[] bytes = cursor
							.getBlob(cursor
									.getColumnIndexOrThrow(BrowserProvider.TABLE_HIS_ICON));
					if (bytes != null && bytes.length > 0) {
						item.icon = BitmapFactory.decodeByteArray(bytes, 0,
								bytes.length);
					}
					list.add(item);
				}
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
				cursor = null;
			}
		}
		return list;
	}
}
