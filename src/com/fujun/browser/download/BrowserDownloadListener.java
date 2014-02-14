
package com.fujun.browser.download;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebView;

import com.fujun.browser.Tab;
import com.fujun.browser.constants.Constants;
import com.fujun.browser.model.DownloadItem;
import com.kukuai.daohang.R;

public class BrowserDownloadListener implements DownloadListener {

	private static final String TAG = "BrowserDownloadListener";
	private Context mContext;
	private Tab mTab;
	private WebView mWebView;

	public BrowserDownloadListener(Context context, Tab tab) {
		mContext = context;
		mTab = tab;
		mWebView = tab.getWebView();
	}

	@Override
	public void onDownloadStart(String url, String userAgent,
			String contentDisposition, String mimetype, long contentLength) {
		mTab.getTitleBar().setTitle(mTab.getCurrentTitle());
		if (contentDisposition == null
				|| !contentDisposition.regionMatches(true, 0, "attachment", 0,
						10)) {
			// query the package manager to see if there's a registered handler
			// that matches.
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(url), mimetype);
			ResolveInfo info = mContext.getPackageManager().resolveActivity(
					intent, PackageManager.MATCH_DEFAULT_ONLY);
			if (info != null) {
				ComponentName myName = ((Activity) mContext).getComponentName();
				// If we resolved to ourselves, we don't want to attempt to
				// load the url only to try and download it again.
				if (!myName.getPackageName().equals(
						info.activityInfo.packageName)
						|| !myName.getClassName()
								.equals(info.activityInfo.name)) {
					// someone (other than us) knows how to handle this mime
					// type with this scheme, don't download.
					try {
						mContext.startActivity(intent);
						return;
					} catch (ActivityNotFoundException ex) {
						Log.d(TAG, "activity not found for " + mimetype
								+ " over " + Uri.parse(url).getScheme(), ex);
						// Best behavior is to fall back to a download in this
						// case
					}
				}
			}
		}

		onDownloadStartNoStream(url, userAgent, contentDisposition, mimetype,
				contentLength);
	}

	private void onDownloadStartNoStream(String url, String userAgent,
			String contentDisposition, String mimetype, long contentLength) {

		// Check to see if we have an SDCard
		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			mWebView.goBack();
			int title;
			String msg;

			// Check to see if the SDCard is busy, same as the music app
			if (status.equals(Environment.MEDIA_SHARED)) {
				msg = mContext.getString(R.string.download_sdcard_busy_dlg_msg);
				title = R.string.download_sdcard_busy_dlg_title;
			} else {
				msg = mContext.getString(R.string.download_no_sdcard_dlg_msg);
				title = R.string.download_no_sdcard_dlg_title;
			}

			new AlertDialog.Builder(mContext).setTitle(title)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage(msg).setPositiveButton(R.string.ok, null)
					.show();
			return;
		}

		DownloadItem item = new DownloadItem();
		item.url = url;
		item.mimeType = mimetype;
		item.fileSize = contentLength;
		item.fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);

		Intent intent = new Intent(mContext, BrowserDownloadManager.class);
		intent.putExtra(Constants.EXTRA_START_DOWNLOAD, true);
		intent.putExtra(Constants.EXTRA_DOWNLOAD_ITEM, item);
		mContext.startService(intent);
	}

}
