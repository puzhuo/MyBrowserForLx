
package com.fujun.browser.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Window;

import com.fujun.browser.constants.Constants;
import com.fujun.browser.provider.BrowserProvider;
import com.fujun.browser.utils.Utils;
import com.kukuai.daohang.R;
import com.umeng.analytics.MobclickAgent;

public class WelcomeActivity extends Activity {

	private static final long GAP_TIME = 3000;
	private boolean mTimeEnd;
	private boolean mDbEnd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.welcome_layout);
		mHandler.sendEmptyMessageDelayed(0, GAP_TIME);

		final SharedPreferences preferences = getSharedPreferences(Constants.PREF_NAME,
				MODE_PRIVATE);
		if (!preferences.getBoolean(Constants.PREF_DEFAULT_FAV + Utils.getVersionName(this), false)) {
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					ContentResolver resolver = getContentResolver();
					Bitmap bitmap = null;
					ContentValues values = new ContentValues();
					values.put(BrowserProvider.TABLE_NAVI_URL,
							"http://m.90fan.cn/api/url.php?url=baidu");
					values.put(BrowserProvider.TABLE_NAVI_TITLE, getString(R.string.navi_baidu));
					values.put(BrowserProvider.TABLE_NAVI_REALURL, Utils
							.removeParametersFromUrl("http://m.90fan.cn/api/url.php?url=baidu"));
					bitmap = Utils.drawable2Bitmap(getResources().getDrawable(
							R.drawable.navi_baidu));
					if (bitmap != null) {
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						bitmap.compress(CompressFormat.PNG, 100, os);
						values.put(BrowserProvider.TABLE_NAVI_ICON,
								os.toByteArray());
					}
					resolver.insert(BrowserProvider.TABLE_INDEX_NAVI_URI, values);

					values.clear();
					values.put(BrowserProvider.TABLE_NAVI_URL,
							"http://m.90fan.cn/api/url.php?url=gouwu");
					values.put(BrowserProvider.TABLE_NAVI_REALURL, Utils
							.removeParametersFromUrl("http://m.90fan.cn/api/url.php?url=gouwu"));
					values.put(BrowserProvider.TABLE_NAVI_TITLE, getString(R.string.navi_shop));
					bitmap = Utils.drawable2Bitmap(getResources().getDrawable(
							R.drawable.navi_shop));
					if (bitmap != null) {
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						bitmap.compress(CompressFormat.PNG, 100, os);
						values.put(BrowserProvider.TABLE_NAVI_ICON,
								os.toByteArray());
					}
					resolver.insert(BrowserProvider.TABLE_INDEX_NAVI_URI, values);

					values.clear();
					values.put(BrowserProvider.TABLE_NAVI_URL,
							"http://m.90fan.cn/api/url.php?url=xiaoshuo");
					values.put(BrowserProvider.TABLE_NAVI_REALURL, Utils
							.removeParametersFromUrl("http://m.90fan.cn/api/url.php?url=xiaoshuo"));
					values.put(BrowserProvider.TABLE_NAVI_TITLE, getString(R.string.navi_novel));
					bitmap = Utils.drawable2Bitmap(getResources().getDrawable(
							R.drawable.navi_novel));
					if (bitmap != null) {
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						bitmap.compress(CompressFormat.PNG, 100, os);
						values.put(BrowserProvider.TABLE_NAVI_ICON,
								os.toByteArray());
					}
					resolver.insert(BrowserProvider.TABLE_INDEX_NAVI_URI, values);

					values.clear();
					values.put(BrowserProvider.TABLE_NAVI_URL,
							"http://m.90fan.cn/api/url.php?url=yingyong");
					values.put(BrowserProvider.TABLE_NAVI_REALURL, Utils
							.removeParametersFromUrl("http://m.90fan.cn/api/url.php?url=yingyong"));
					values.put(BrowserProvider.TABLE_NAVI_TITLE, getString(R.string.navi_app));
					bitmap = Utils.drawable2Bitmap(getResources().getDrawable(
							R.drawable.navi_app));
					if (bitmap != null) {
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						bitmap.compress(CompressFormat.PNG, 100, os);
						values.put(BrowserProvider.TABLE_NAVI_ICON,
								os.toByteArray());
					}
					resolver.insert(BrowserProvider.TABLE_INDEX_NAVI_URI, values);

					values.clear();
					values.put(BrowserProvider.TABLE_NAVI_URL,
							"http://m.idea123.cn/");
					values.put(BrowserProvider.TABLE_NAVI_REALURL, Utils
							.removeParametersFromUrl("http://m.idea123.cn/"));
					values.put(BrowserProvider.TABLE_NAVI_TITLE, getString(R.string.navi_daohang));
					bitmap = Utils.drawable2Bitmap(getResources().getDrawable(
							R.drawable.navi_daohang));
					if (bitmap != null) {
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						bitmap.compress(CompressFormat.PNG, 100, os);
						values.put(BrowserProvider.TABLE_NAVI_ICON,
								os.toByteArray());
					}
					resolver.insert(BrowserProvider.TABLE_INDEX_NAVI_URI, values);

					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
						File file = new File(Environment.getExternalStorageDirectory(),
								Constants.SDCARD_HTML_FOLDER);
						if (file.exists() && file.isDirectory()) {
							Utils.RecursionDeleteFile(file);
						}
					}
					return null;
				}

				protected void onPostExecute(Void result) {
					preferences
							.edit()
							.putBoolean(
									Constants.PREF_DEFAULT_FAV
											+ Utils.getVersionName(WelcomeActivity.this),
									true).commit();
					mDbEnd = true;
					finish();
				};
			}.execute();
		} else {
			mDbEnd = true;
			finish();
		}
	}

	@Override
	public void finish() {
		if (mDbEnd && mTimeEnd) {
			startActivity(new Intent(this, HomeActivity.class));
			super.finish();
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			mTimeEnd = true;
			finish();
		};
	};

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
