
package com.fujun.browser.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.fujun.browser.constants.Constants;
import com.fujun.browser.model.DownloadItem;
import com.fujun.browser.provider.BrowserProvider;

public class BrowserDownloadThread extends Thread {

	private static final long UPDATE_PROGRESS_TIME = 1200;

	private Context mContext;
	private DownloadItem mDownloadItem;
	private Handler mHandler;
	// when mDelete is true, you need to delete the recoed in db
	private boolean mPause;
	private boolean mDelete;

	public BrowserDownloadThread(Context context, DownloadItem item, Handler handler) {
		mDownloadItem = item;
		mHandler = handler;
		mContext = context;
	}

	public void pause() {
		mPause = true;
	}

	public void delete() {
		mDelete = true;
	}

	private void updateDb() {
		Cursor cursor = null;
		ContentValues values = new ContentValues();
		ContentResolver resolver = mContext.getContentResolver();
		try {
			cursor = resolver.query(BrowserProvider.TABLE_DOWNLOAD_URI, null,
					BrowserProvider.TABLE_DOWNLOAD_URL + "=?", new String[] {
						mDownloadItem.url
					}, null);
			if (cursor != null && cursor.getCount() > 0) {
				values.put(BrowserProvider.TABLE_DOWNLOAD_STATUS,
						DownloadItem.DOWNLOAD_STATUS_DOWNLOADING);
				resolver.update(BrowserProvider.TABLE_DOWNLOAD_URI, values,
						BrowserProvider.TABLE_DOWNLOAD_URL + "=?", new String[] {
							mDownloadItem.url
						});
			} else {
				values.put(BrowserProvider.TABLE_DOWNLOAD_CURRENT_SIZE, mDownloadItem.currentSize);
				values.put(BrowserProvider.TABLE_DOWNLOAD_FILE_NAME, mDownloadItem.fileName);
				values.put(BrowserProvider.TABLE_DOWNLOAD_MIMETYPE, mDownloadItem.mimeType);
				values.put(BrowserProvider.TABLE_DOWNLOAD_FILE_SIZE, mDownloadItem.fileSize);
				values.put(BrowserProvider.TABLE_DOWNLOAD_URL, mDownloadItem.url);
				values.put(BrowserProvider.TABLE_DOWNLOAD_STATUS,
						DownloadItem.DOWNLOAD_STATUS_DOWNLOADING);
				resolver.insert(BrowserProvider.TABLE_DOWNLOAD_URI, values);
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
				cursor = null;
			}
		}
	}

	private void sendDoneMessage() {
		Message message = Message.obtain();
		message.obj = mDownloadItem;
		message.what = BrowserDownloadManager.MESSAGE_DOWNLOAD_THREAD_DONE;
		mHandler.sendMessage(message);
	}

	private void sendOpenMessage() {
		Message message = Message.obtain();
		message.obj = mDownloadItem;
		message.what = BrowserDownloadManager.MESSAGE_OPEN_FILE;
		mHandler.sendMessage(message);
	}

	private void sendStartNotificationMessage() {
		Message message = Message.obtain();
		message.obj = mDownloadItem;
		message.what = BrowserDownloadManager.MESSAGE_START_NOTIFICATION;
		mHandler.sendMessage(message);
	}

	private void sendUpdateNotificationMessage() {
		Message message = Message.obtain();
		message.obj = mDownloadItem;
		message.what = BrowserDownloadManager.MESSAGE_UPDATE_NOTIFICATION;
		mHandler.sendMessageDelayed(message, UPDATE_PROGRESS_TIME);
	}

	private void sendCancelNotificationMessage() {
		Message message = Message.obtain();
		message.obj = mDownloadItem;
		message.what = BrowserDownloadManager.MESSAGE_CANCEL_NOTIFICATION;
		mHandler.sendMessage(message);
	}

	private void sendDownloadedNotificationMessage() {
		Message message = Message.obtain();
		message.obj = mDownloadItem;
		message.what = BrowserDownloadManager.MESSAGE_DOWNLOADED_NOTIFICATION;
		mHandler.sendMessage(message);
	}

	@Override
	public void run() {
		ContentResolver resolver = mContext.getContentResolver();
		ContentValues values = new ContentValues();
		try {
			updateDb();
			File file = new File(Environment.getExternalStorageDirectory(),
					Constants.DOWNLOAD_FOLDER + "/" + mDownloadItem.fileName);
			long currentSize = 0;
			if (file.exists()) {
				// resume
				currentSize = file.length();
			} else {
				file.createNewFile();
			}
			sendStartNotificationMessage();
			URL url = new URL(mDownloadItem.url);
			HttpURLConnection connection = null;
			InputStream in = null;
			RandomAccessFile targetFile = null;
			try {
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(Constants.CONNECT_TIME_OUT);
				String sProperty = "bytes=" + currentSize + "-";
				connection.setRequestProperty("RANGE", sProperty);
				in = connection.getInputStream();
				targetFile = new RandomAccessFile(file, "rw");
				byte[] bytes = new byte[1024 * 1024];
				int len = 0;
				long length = currentSize;
				targetFile.seek(currentSize);
				while ((len = in.read(bytes)) > 0) {
					targetFile.write(bytes, 0, len);
					length += len;
					mDownloadItem.currentSize = length;
					sendUpdateNotificationMessage();
					Message message = Message.obtain();
					message.what = BrowserDownloadManager.MESSAGE_UPDATE_PROGRESS;
					message.obj = mDownloadItem;
					mHandler.sendMessageDelayed(message, UPDATE_PROGRESS_TIME);
					if (mPause) {
						values.clear();
						values.put(BrowserProvider.TABLE_DOWNLOAD_STATUS,
								DownloadItem.DOWNLOAD_STATUS_PAUSED);
						resolver.update(BrowserProvider.TABLE_DOWNLOAD_URI, values,
								BrowserProvider.TABLE_DOWNLOAD_URL + "=?", new String[] {
									mDownloadItem.url
								});
						mHandler.removeMessages(BrowserDownloadManager.MESSAGE_UPDATE_PROGRESS);
						sendDoneMessage();
						sendCancelNotificationMessage();
						return;
					}
					if (mDelete) {
						mHandler.removeMessages(BrowserDownloadManager.MESSAGE_UPDATE_PROGRESS);
						sendDoneMessage();
						sendCancelNotificationMessage();
						resolver.delete(BrowserProvider.TABLE_DOWNLOAD_URI,
								BrowserProvider.TABLE_DOWNLOAD_URL + "=?", new String[] {
									mDownloadItem.url
								});
						return;
					}
				}

				values.clear();
				values.put(BrowserProvider.TABLE_DOWNLOAD_STATUS,
						DownloadItem.DOWNLOAD_STATUS_DOWNLOADED);
				resolver.update(BrowserProvider.TABLE_DOWNLOAD_URI, values,
						BrowserProvider.TABLE_DOWNLOAD_URL + "=?", new String[] {
							mDownloadItem.url
						});
				sendDownloadedNotificationMessage();
				sendDoneMessage();
				sendOpenMessage();
			} finally {
				if (in != null) {
					in.close();
					in = null;
				}
				if (connection != null) {
					connection.disconnect();
				}
				if (targetFile != null) {
					targetFile.close();
					targetFile = null;
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			sendCancelNotificationMessage();
		} catch (IOException e) {
			e.printStackTrace();
			sendCancelNotificationMessage();
		}
	}
}
