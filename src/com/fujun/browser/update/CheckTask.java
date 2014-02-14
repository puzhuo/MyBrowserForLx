
package com.fujun.browser.update;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;

import com.fujun.browser.constants.Constants;
import com.fujun.browser.download.BrowserDownloadManager;
import com.fujun.browser.fragment.NotifyDialogFragment;
import com.fujun.browser.model.DownloadItem;
import com.fujun.browser.utils.Utils;
import com.kukuai.daohang.R;

public class CheckTask {

	// http://kknet.com.cn/
	// http://123.kknet.com.cn?cid=100
	private static final String WEB_DOMAIN = "http://kknet.com.cn/";
	private static final String TAG = "CheckTask";
	private static final String UPDATE_URL = "http://app.hao3608.com/";

	public static void start(Context context, int checkId, FragmentManager manager) {
		String urlForCheck = WEB_DOMAIN + "&v="
				+ Utils.getVersion(context);
		if (TextUtils.isEmpty(urlForCheck)) {
			return;
		}
		String checkInfo = getCheckInfo(context, checkId, urlForCheck);
		if (checkInfo == null) {
			return;
		}
		Log.e(TAG, checkInfo);
		parseCheckInfo(context, checkId, checkInfo, urlForCheck, manager);
	}

	private static void parseCheckInfo(final Context context, int checkId, String checkInfo,
			String urlForCheck, FragmentManager manager) {
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(checkInfo);
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
		SharedPreferences preferences = null;
		switch (checkId) {
			case Constants.CHECK_UPDATE_ID:
				preferences = context.getSharedPreferences(
						Constants.PREF_NAME, Context.MODE_PRIVATE);
				preferences.edit()
						.putLong(Constants.SHARED_LAST_CHECK_TIME, System.currentTimeMillis())
						.commit();
				try {
					String url = jObject.getString(Constants.JSON_RECHECK_URL);
					if (jObject.getInt(Constants.JSON_ERROR_OCCUR) == 1
							|| !urlForCheck.equals(url)
							|| jObject.getInt(Constants.JSON_IF_UPDATE) == 0) {
						return;
					}
					UpdateInfo info = new UpdateInfo();
					info.remoteVersion = jObject
							.getString(Constants.JSON_REMOTE_VERSION);
					info.updateDescription = jObject
							.getString(Constants.JSON_DESCRIPTION);
					info.updateUrl = jObject
							.getString(Constants.JSON_DOWNLOAD_URL);
					info.contentLength = jObject
							.getLong(Constants.JSON_CONTENT_SIZE);

					NotifyDialogFragment fragment = new NotifyDialogFragment() {

						@Override
						public void positiveButtonClicked(final Serializable info) {
							if (info == null || !(info instanceof UpdateInfo)) {
								return;
							}
							if (!Environment.getExternalStorageState().equals(
									Environment.MEDIA_MOUNTED)) {
								Utils.showMessage(context,
										context.getString(R.string.no_sdcard_no_update));
								return;
							}
							UpdateInfo updateInfo = (UpdateInfo) info;
							DownloadItem item = new DownloadItem();
							item.url = updateInfo.updateUrl;
							item.fileSize = updateInfo.contentLength;
							item.fileName = context.getString(R.string.app_name)
									+ updateInfo.remoteVersion + ".apk";

							Intent intent = new Intent(context, BrowserDownloadManager.class);
							intent.putExtra(Constants.EXTRA_START_DOWNLOAD, true);
							intent.putExtra(Constants.EXTRA_DOWNLOAD_ITEM, item);
							context.startService(intent);
						}
					};
					Bundle bundle = new Bundle();
					bundle.putSerializable(NotifyDialogFragment.ARGUMENT_DOWNLOAD_ITEM, info);
					bundle.putInt(NotifyDialogFragment.ARGUMENT_NOTIFY_ID,
							NotifyDialogFragment.DOWNLOADED_UPDATE_ID);
					fragment.setArguments(bundle);
					fragment.show(manager, "");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case Constants.CHECK_LOADING_ID:
				break;
		}
	}

	private static String getCheckInfo(Context context, int checkId, String urlForCheck) {
		String urlString = null;
		switch (checkId) {
			case Constants.CHECK_UPDATE_ID:
				urlString = UPDATE_URL + "?url=" + urlForCheck;
				break;
			case Constants.CHECK_LOADING_ID:
				break;
		}
		Log.e("fujun", urlString);
		InputStreamReader reader = null;
		StringBuffer buffer = new StringBuffer();
		HttpURLConnection connection = null;
		try {
			URL url = new URL(urlString);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(Constants.CONNECT_TIME_OUT);
			reader = new InputStreamReader(connection.getInputStream());
			char[] chars = new char[256];
			int len = 0;
			while ((len = reader.read(chars)) > 0) {
				buffer.append(chars, 0, len);
			}
			return buffer.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				reader = null;
			}
			if (connection != null) {
				connection.disconnect();
				connection = null;
			}
		}
	}
}
