
package com.fujun.browser.download;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.fujun.browser.BrowserApplication;
import com.fujun.browser.constants.Constants;
import com.fujun.browser.fragment.NotifyDialogFragment;
import com.fujun.browser.model.DownloadItem;
import com.fujun.browser.provider.BrowserProvider;
import com.fujun.browser.utils.NotificationUtils;
import com.fujun.browser.utils.Utils;
import com.kukuai.daohang.R;

public class BrowserDownloadManager extends Service {

	private static final int DOWNLOAD_THREAD_COUNT = 3;
	public static final int MESSAGE_DOWNLOAD_THREAD_DONE = 0;
	public static final int MESSAGE_UPDATE_PROGRESS = 1;
	public static final int MESSAGE_OPEN_FILE = 2;
	public static final int MESSAGE_START_NOTIFICATION = 3;
	public static final int MESSAGE_UPDATE_NOTIFICATION = 4;
	public static final int MESSAGE_DOWNLOADED_NOTIFICATION = 5;
	public static final int MESSAGE_CANCEL_NOTIFICATION = 6;

	private HashMap<String, BrowserDownloadThread> mDownloadMap = new HashMap<String, BrowserDownloadThread>();
	private Queue<DownloadItem> mWaitingQueue = new LinkedList<DownloadItem>();
	private NotificationManager mNotificationManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void sendNotification(Notification notification, String tag) {
		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		mNotificationManager.notify(tag, 0, notification);
	}

	private void cancelNotification(String tag) {
		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		mNotificationManager.cancel(tag, 0);
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case MESSAGE_DOWNLOAD_THREAD_DONE:
				{
					DownloadItem item = (DownloadItem) msg.obj;
					switchDownloadTask(item.url);
				}
					break;
				case MESSAGE_UPDATE_PROGRESS:
				{
					DownloadItem item = (DownloadItem) msg.obj;
					ContentValues values = new ContentValues();
					values.put(BrowserProvider.TABLE_DOWNLOAD_CURRENT_SIZE,
							item.currentSize);
					getContentResolver().update(BrowserProvider.TABLE_DOWNLOAD_URI,
							values,
							BrowserProvider.TABLE_DOWNLOAD_URL + "=?", new String[] {
								item.url
							});
					removeMessages(MESSAGE_UPDATE_PROGRESS);
				}
					break;
				case MESSAGE_OPEN_FILE:
				{
					DownloadItem item = (DownloadItem) msg.obj;
					File file = new File(Environment.getExternalStorageDirectory(),
							Constants.DOWNLOAD_FOLDER + "/" + item.fileName);
					Utils.openFile(BrowserDownloadManager.this, file, item.mimeType);
				}
					break;
				case MESSAGE_START_NOTIFICATION:
				{
					DownloadItem item = (DownloadItem) msg.obj;
					item.notification = NotificationUtils.getDownloadNotification(
							BrowserDownloadManager.this, item);
					RemoteViews content = item.notification.contentView;
					content.setTextViewText(R.id.notification_text, "0 %");
					sendNotification(item.notification, item.fileName);
				}
					break;
				case MESSAGE_UPDATE_NOTIFICATION:
				{
					DownloadItem item = (DownloadItem) msg.obj;
					if (item.notification == null) {
						item.notification = NotificationUtils.getDownloadNotification(
								BrowserDownloadManager.this, item);
					}
					RemoteViews content = item.notification.contentView;
					int percent = Utils.getProgress(item.currentSize, item.fileSize);
					content.setProgressBar(
							R.id.notification_progress, 100, percent, false);
					content.setTextViewText(R.id.notification_text, percent + " %");
					sendNotification(item.notification, item.fileName);
					removeMessages(MESSAGE_UPDATE_NOTIFICATION);
				}
					break;
				case MESSAGE_DOWNLOADED_NOTIFICATION:
				{
					DownloadItem item = (DownloadItem) msg.obj;
					cancelNotification(item.fileName);
					item.notification = NotificationUtils.getDownloadedNotification(
							BrowserDownloadManager.this, item);
					sendNotification(item.notification, item.fileName);
				}
					break;
				case MESSAGE_CANCEL_NOTIFICATION:
				{
					DownloadItem item = (DownloadItem) msg.obj;
					cancelNotification(item.fileName);
					removeMessages(MESSAGE_START_NOTIFICATION);
					removeMessages(MESSAGE_UPDATE_NOTIFICATION);
				}
					break;
			}
		};
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			if (intent.getBooleanExtra(Constants.EXTRA_PAUSE_DOWNLOAD, false)) {
				// pause download
				String url = intent.getStringExtra(Constants.EXTRA_DOWNLOAD_URL);
				if (mDownloadMap.containsKey(url)) {
					mDownloadMap.get(url).pause();
				}
			} else if (intent.getBooleanExtra(Constants.EXTRA_DELETE_DOWNLOAD,
					false)) {
				// delete download
				String url = intent.getStringExtra(Constants.EXTRA_DOWNLOAD_URL);
				if (mDownloadMap.containsKey(url)) {
					mDownloadMap.get(url).delete();
				} else {
					removeWaitingTask(url);
					deleteFromDb(url);
				}
			} else if (intent
					.getBooleanExtra(Constants.EXTRA_START_DOWNLOAD, false)) {
				// start download
				final DownloadItem item = (DownloadItem) intent
						.getSerializableExtra(Constants.EXTRA_DOWNLOAD_ITEM);
				new AsyncTask<Void, Void, Boolean>() {

					@Override
					protected Boolean doInBackground(Void... params) {
						File file = new File(Environment.getExternalStorageDirectory(),
								Constants.DOWNLOAD_FOLDER + "/" + item.fileName);
						return Boolean.valueOf(file.exists());
					}

					protected void onPostExecute(Boolean result) {
						if (result.booleanValue()) {
							NotifyDialogFragment fragment = new NotifyDialogFragment() {

								@Override
								public void positiveButtonClicked(Serializable info) {
									Intent intent = new Intent(BrowserDownloadManager.this,
											BrowserDownloadManager.class);
									intent.putExtra(Constants.EXTRA_RESTART_DOWNLOAD, true);
									intent.putExtra(Constants.EXTRA_DOWNLOAD_ITEM, item);
									startService(intent);
								}
							};
							Bundle bundle = new Bundle();
							bundle.putInt(NotifyDialogFragment.ARGUMENT_NOTIFY_ID,
									NotifyDialogFragment.DOWNLOAD_OVERLAP_ID);
							fragment.setArguments(bundle);
							fragment.show(BrowserApplication.getFragmentManager(), "");
						} else {
							// just start the download
							startDownload(item);
						}
					};

				}.execute();
			} else if (intent.getBooleanExtra(Constants.EXTRA_RESUME_DOWNLOAD,
					false)) {
				final DownloadItem item = (DownloadItem) intent
						.getSerializableExtra(Constants.EXTRA_DOWNLOAD_ITEM);
				startDownload(item);
			} else if (intent.getBooleanExtra(Constants.EXTRA_RESTART_DOWNLOAD,
					false)) {
				final DownloadItem item = (DownloadItem) intent
						.getSerializableExtra(Constants.EXTRA_DOWNLOAD_ITEM);
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						// first we stop the previous one
						if (mDownloadMap.containsKey(item.url)) {
							mDownloadMap.get(item.url).delete();
						} else {
							removeWaitingTask(item.url);
						}
						File file = new File(Environment
								.getExternalStorageDirectory(),
								Constants.DOWNLOAD_FOLDER + "/"
										+ item.fileName);
						if (file.exists()) {
							file.delete();
						}
						getContentResolver().delete(BrowserProvider.TABLE_DOWNLOAD_URI,
								BrowserProvider.TABLE_DOWNLOAD_URL + "=?", new String[] {
									item.url
								});
						return null;
					}

					protected void onPostExecute(Void result) {
						// then we start the new one
						startDownload(item);
					};

				}.execute();

			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void removeWaitingTask(String url) {
		Iterator<DownloadItem> iterator = mWaitingQueue.iterator();
		DownloadItem item = null;
		while (iterator.hasNext()) {
			item = iterator.next();
			if (item.url.equals(url)) {
				iterator.remove();
			}
		}
	}

	private void switchDownloadTask(String url) {
		if (mDownloadMap.containsKey(url)) {
			mDownloadMap.remove(url);
			DownloadItem item = mWaitingQueue.poll();
			if (item != null) {
				BrowserDownloadThread thread = new BrowserDownloadThread(this, item, mHandler);
				thread.start();
				mDownloadMap.put(item.url, thread);
			}
		}
	}

	private void startDownload(final DownloadItem item) {
		Utils.showMessage(this, getString(R.string.add_to_download));
		if (mDownloadMap.size() < DOWNLOAD_THREAD_COUNT) {
			BrowserDownloadThread thread = new BrowserDownloadThread(this, item, mHandler);
			thread.start();
			mDownloadMap.put(item.url, thread);
		} else {
			mWaitingQueue.add(item);
			final ContentValues values = new ContentValues();
			new AsyncTask<Void, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(Void... params) {
					Cursor cursor = null;
					try {
						cursor = getContentResolver().query(BrowserProvider.TABLE_DOWNLOAD_URI,
								null,
								BrowserProvider.TABLE_DOWNLOAD_URL + "=?", new String[] {
									item.url
								}, null);
						return (cursor != null && cursor.getCount() > 0);
					} finally {
						if (cursor != null && !cursor.isClosed()) {
							cursor.close();
							cursor = null;
						}
					}
				}

				protected void onPostExecute(Boolean result) {
					if (result.booleanValue()) {
						values.put(BrowserProvider.TABLE_DOWNLOAD_STATUS,
								DownloadItem.DOWNLOAD_STATUS_WAIT);
						getContentResolver().update(BrowserProvider.TABLE_DOWNLOAD_URI, values,
								BrowserProvider.TABLE_DOWNLOAD_URL + "=?", new String[] {
									item.url
								});
					} else {
						values.put(BrowserProvider.TABLE_DOWNLOAD_STATUS,
								DownloadItem.DOWNLOAD_STATUS_WAIT);
						values.put(BrowserProvider.TABLE_DOWNLOAD_CURRENT_SIZE, item.currentSize);
						values.put(BrowserProvider.TABLE_DOWNLOAD_MIMETYPE, item.mimeType);
						values.put(BrowserProvider.TABLE_DOWNLOAD_FILE_NAME, item.fileName);
						values.put(BrowserProvider.TABLE_DOWNLOAD_FILE_SIZE, item.fileSize);
						values.put(BrowserProvider.TABLE_DOWNLOAD_URL, item.url);
						getContentResolver().insert(BrowserProvider.TABLE_DOWNLOAD_URI, values);
					}
				};

			}.execute();
		}
	}

	private void deleteFromDb(final String url) {
		if (url != null) {
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					getContentResolver()
							.delete(BrowserProvider.TABLE_DOWNLOAD_URI,
									BrowserProvider.TABLE_DOWNLOAD_URL + "=?", new String[] {
										url
									});
					return null;
				}
			}.execute();
		}
	}
}
