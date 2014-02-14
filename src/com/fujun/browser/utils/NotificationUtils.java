
package com.fujun.browser.utils;

import java.io.File;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.RemoteViews;

import com.fujun.browser.activity.DownloadManagerActivity;
import com.fujun.browser.constants.Constants;
import com.fujun.browser.model.DownloadItem;
import com.kukuai.daohang.R;

public class NotificationUtils {

	public static Notification getDownloadNotification(Context context, DownloadItem item) {
		Builder builder = new Builder(context);
		builder.setTicker(context.getString(R.string.notifi_download_start));
		builder.setOngoing(true);
		builder.setSmallIcon(android.R.drawable.stat_sys_download);
		builder.setContentTitle((String) item.fileName);
		RemoteViews content = new RemoteViews(context.getPackageName(),
				R.layout.notification_progress_layout);
		content.setTextViewText(R.id.notification_title, item.fileName);
		builder.setContent(content);
		builder.setContentIntent(PendingIntent.getActivity(
				context, 0, new Intent(context,
						DownloadManagerActivity.class), 0));
		return builder.getNotification();
	}

	public static Notification getDownloadedNotification(Context context, DownloadItem item) {
		Builder builder = new Builder(context);
		builder.setTicker(context.getString(R.string.notifi_download_done));
		builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
		builder.setContentTitle((String) item.fileName);
		builder.setOngoing(false);
		builder.setAutoCancel(true);
		builder.setContentText(context.getString(R.string.notifi_download_done));

		File file = new File(Environment.getExternalStorageDirectory(),
				Constants.DOWNLOAD_FOLDER + "/" + item.fileName);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(file), item.mimeType);
		builder.setContentIntent(PendingIntent.getActivity(
				context, 0, intent, 0));
		return builder.getNotification();
	}
}
