
package com.fujun.browser.loader;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;

import com.fujun.browser.model.DownloadItem;
import com.fujun.browser.provider.BrowserProvider;

public class DownloadLoader extends BaseLoader<ArrayList<DownloadItem>> {

	public DownloadLoader(Context context) {
		super(context);
	}

	@Override
	protected ArrayList<DownloadItem> onLoadInBackground() {
		ArrayList<DownloadItem> downloadItems = new ArrayList<DownloadItem>();
		Cursor cursor = null;
		try {
			cursor = mContext.getContentResolver().query(
					BrowserProvider.TABLE_DOWNLOAD_URI, null, null, null, null);
			mCursor = cursor;
			mContext.getContentResolver().registerContentObserver(
					BrowserProvider.TABLE_DOWNLOAD_URI, true, mObserver);
			DownloadItem item = null;
			while (cursor.moveToNext()) {
				item = new DownloadItem();
				item.fileName = cursor
						.getString(cursor
								.getColumnIndexOrThrow(BrowserProvider.TABLE_DOWNLOAD_FILE_NAME));
				item.url = cursor
						.getString(cursor
								.getColumnIndexOrThrow(BrowserProvider.TABLE_DOWNLOAD_URL));
				item.mimeType = cursor
						.getString(cursor
								.getColumnIndexOrThrow(BrowserProvider.TABLE_DOWNLOAD_MIMETYPE));
				item.fileSize = cursor
						.getLong(cursor
								.getColumnIndexOrThrow(BrowserProvider.TABLE_DOWNLOAD_FILE_SIZE));
				item.currentSize = cursor
						.getLong(cursor
								.getColumnIndexOrThrow(BrowserProvider.TABLE_DOWNLOAD_CURRENT_SIZE));
				item.status = cursor
						.getInt(cursor
								.getColumnIndexOrThrow(BrowserProvider.TABLE_DOWNLOAD_STATUS));
				downloadItems.add(item);
			}
			return downloadItems;
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
				cursor = null;
			}
		}
	}
}
