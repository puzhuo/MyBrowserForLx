
package com.fujun.browser.loader;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

public class BaseLoader<T> extends AsyncTaskLoader<T> {

	protected Context mContext;
	protected Cursor mCursor;
	protected LoadContentObserver mObserver = new LoadContentObserver(
			new Handler());

	public BaseLoader(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public T loadInBackground() {
		return null;
	}

	private class LoadContentObserver extends ContentObserver {

		public LoadContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			onContentChanged();
		}
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		forceLoad();
	}

	@Override
	protected void onReset() {
		super.onReset();
		if (mCursor != null && !mCursor.isClosed()) {
			getContext().getContentResolver().unregisterContentObserver(
					mObserver);
			mCursor.close();
			mCursor = null;
		}
	}

}
