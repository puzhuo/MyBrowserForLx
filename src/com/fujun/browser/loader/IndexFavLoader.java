
package com.fujun.browser.loader;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;

import com.fujun.browser.model.FavItem;
import com.fujun.browser.provider.BrowserProvider;

public class IndexFavLoader extends BaseLoader<List<FavItem>> {

	public IndexFavLoader(Context context) {
		super(context);
	}

	@Override
	public List<FavItem> loadInBackground() {
		Cursor cursor = mContext.getContentResolver().query(
				BrowserProvider.TABLE_INDEX_NAVI_URI, null, null, null, null);
		List<FavItem> list = null;
		if (cursor != null && cursor.getCount() > 0) {
			mCursor = cursor;
			getContext().getContentResolver().registerContentObserver(
					BrowserProvider.TABLE_INDEX_NAVI_URI, true, mObserver);
			int count = cursor.getCount();
			list = new ArrayList<FavItem>();
			FavItem info = null;
			for (int i = 0; i < count; i++) {
				cursor.moveToPosition(i);
				info = new FavItem();
				info.title = cursor
						.getString(cursor
								.getColumnIndexOrThrow(BrowserProvider.TABLE_NAVI_TITLE));
				info.url = cursor.getString(cursor
						.getColumnIndexOrThrow(BrowserProvider.TABLE_NAVI_URL));
				info.position = cursor
						.getInt(cursor
								.getColumnIndexOrThrow(BrowserProvider.TABLE_NAVI_POSITION));
				byte[] bytes = cursor
						.getBlob(cursor
								.getColumnIndexOrThrow(BrowserProvider.TABLE_NAVI_ICON));
				if (bytes != null && bytes.length > 0) {
					info.icon = BitmapFactory.decodeByteArray(bytes, 0,
							bytes.length);
				}
				list.add(info);
			}
		}
		return list;
	}

}
