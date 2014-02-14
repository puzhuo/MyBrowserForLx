package com.fujun.browser.loader;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;

import com.fujun.browser.model.FavItem;
import com.fujun.browser.provider.BrowserProvider;

public class FavLoader extends BaseLoader<ArrayList<FavItem>> {

	public FavLoader(Context context) {
		super(context);
	}

	@Override
	public ArrayList<FavItem> loadInBackground() {
		ArrayList<FavItem> list = new ArrayList<FavItem>();
		Cursor cursor = null;
		try {
			cursor = mContext.getContentResolver().query(
					BrowserProvider.TABLE_FAV_URI, null, null, null, null);
			if (cursor != null) {
				mCursor = cursor;
				mContext.getContentResolver().registerContentObserver(
						BrowserProvider.TABLE_FAV_URI, true, mObserver);
				FavItem item = null;
				while (cursor.moveToNext()) {
					item = new FavItem();
					item.title = cursor
							.getString(cursor
									.getColumnIndexOrThrow(BrowserProvider.TABLE_FAV_TITLE));
					item.url = cursor
							.getString(cursor
									.getColumnIndexOrThrow(BrowserProvider.TABLE_FAV_URL));
					byte[] bytes = cursor
							.getBlob(cursor
									.getColumnIndexOrThrow(BrowserProvider.TABLE_FAV_ICON));
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
