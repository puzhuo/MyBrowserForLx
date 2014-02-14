
package com.fujun.browser.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.Selection;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Toast;

import com.fujun.browser.activity.HomeActivity;
import com.fujun.browser.activity.WelcomeActivity;
import com.fujun.browser.constants.Constants;
import com.fujun.browser.model.FavItem;
import com.fujun.browser.model.HisItem;
import com.fujun.browser.provider.BrowserProvider;
import com.kukuai.daohang.R;

public class Utils {

	public static String removeParametersFromUrl(String urlString) {
		if (urlString != null && urlString.startsWith("http://m.90fan.cn/api/url.php")) {
			return urlString;
		}
		URL url;
		try {
			url = new URL(urlString);
			return url.getProtocol() + "://" + url.getHost() + url.getPath();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getProgress(long current, long all) {
		float value = (float) current / (float) all;
		int percent = (int) (value * 100);
		return percent;
	}

	public static void openFile(Context context, File file, String mimeType) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(file), mimeType);
		context.startActivity(intent);
	}

	public static int getLimitedProgress(Context context) {
		if (isWifiNetworkAvailable(context)) {
			return Constants.WIFI_LIMIT_PROGRESS;
		} else {
			return Constants.G_LIMIT_PROGRESS;
		}
	}

	/**
	 * Returns whether the network is roaming. 判断是否是手机网络 roam是漫游的意思
	 */
	public static boolean isNetworkRoaming(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			Log.v("SPRD_STORE", "couldn't get connectivity manager");
			return false;
		}

		NetworkInfo info = connectivity.getActiveNetworkInfo();
		if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telManager != null && telManager.isNetworkRoaming()) {
				Log.v("SPRD_STORE", "network is roaming");
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns whether the network is wifi connection.
	 */
	public static boolean isWifiNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			Log.v("SPRD_STORE", "couldn't get connectivity manager");
			return false;
		}

		NetworkInfo info = connectivity.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			return false;
		}

		if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}

		return false;
	}

	public static String getVersion(Context context) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
			if (Constants.DEBUG) {
				Log.e("fujun", "versionnName is " + info.versionName);
			}
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void createShortCut(Context context) {
		Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.app_name));
		intent.putExtra("duplicate", false);

		ShortcutIconResource resource = Intent.ShortcutIconResource.fromContext(context,
				R.drawable.ic_launcher);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, resource);

		Intent shortIntent = new Intent();
		shortIntent.setClass(context, WelcomeActivity.class);
		shortIntent.setAction(Intent.ACTION_MAIN);
		shortIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortIntent);

		context.sendBroadcast(intent);
	}

	public static void clearVisitHistory(Context context) {
		context.getContentResolver().delete(BrowserProvider.TABLE_HIS_URI,
				null, null);
	}

	public static void insertHistory(Context context, final HisItem item) {
		if (item == null || item.url == null) {
			return;
		}
		final ContentResolver resolver = context.getContentResolver();
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				Cursor cursor = null;
				try {
					cursor = resolver.query(BrowserProvider.TABLE_HIS_URI,
							null, BrowserProvider.TABLE_HIS_URL + "=?",
							new String[] {
								item.url
							}, null);
					ContentValues values = new ContentValues();
					if (cursor != null && cursor.getCount() > 0) {
						values.put(BrowserProvider.TABLE_HIS_ACCESS_TIME,
								item.accessTime);
						resolver.update(BrowserProvider.TABLE_HIS_URI, values,
								BrowserProvider.TABLE_HIS_URL + "=?",
								new String[] {
									item.url
								});
					} else {
						values.put(BrowserProvider.TABLE_HIS_ACCESS_TIME,
								item.accessTime);
						values.put(BrowserProvider.TABLE_HIS_TITLE, item.title);
						values.put(BrowserProvider.TABLE_HIS_URL, item.url);
						values.put(BrowserProvider.TABLE_HIS_REALURL,
								removeParametersFromUrl(item.url));
						if (item.icon != null) {
							ByteArrayOutputStream os = new ByteArrayOutputStream();
							item.icon.compress(CompressFormat.PNG, 100, os);
							values.put(BrowserProvider.TABLE_HIS_ICON,
									os.toByteArray());
						}
						resolver.insert(BrowserProvider.TABLE_HIS_URI, values);
					}
				} finally {
					if (cursor != null && !cursor.isClosed()) {
						cursor.close();
						cursor = null;
					}
				}
				return null;
			}
		}.execute();
	}

	public static Bitmap drawable2Bitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		} else if (drawable instanceof NinePatchDrawable) {
			Bitmap bitmap = Bitmap
					.createBitmap(
							drawable.getIntrinsicWidth(),
							drawable.getIntrinsicHeight(),
							drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
									: Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight());
			drawable.draw(canvas);
			return bitmap;
		}
		return null;
	}

	public static final int getColor(String color) {
		if ("blue".equals(color)) {
			return Color.BLUE;
		} else if ("red".equals(color)) {
			return Color.RED;
		}
		return -1;
	}

	static final Pattern ACCEPTED_URI_SCHEMA = Pattern.compile("(?i)"
			+ // switch on case insensitive matching
			"("
			+ // begin group for schema
			"(?:http|https|file):\\/\\/" + "|(?:inline|data|about|javascript):"
			+ ")" + "(.*)");

	// Google search
	private final static String QUICKSEARCH_G = "http://www.google.com/m?q=%s";
	private final static String QUERY_PLACE_HOLDER = "%s";

	/**
	 * Attempts to determine whether user input is a URL or search terms.
	 * Anything with a space is passed to search if canBeSearch is true.
	 * Converts to lowercase any mistakenly uppercased schema (i.e., "Http://"
	 * converts to "http://"
	 * 
	 * @param canBeSearch If true, will return a search url if it isn't a valid
	 *            URL. If false, invalid URLs will return null
	 * @return Original or modified URL
	 */
	@SuppressLint("DefaultLocale")
	public static String smartUrlFilter(String url, boolean canBeSearch) {
		if (url == null) {
			return null;
		}
		String inUrl = url.trim();
		boolean hasSpace = inUrl.indexOf(' ') != -1;

		Matcher matcher = ACCEPTED_URI_SCHEMA.matcher(inUrl);
		if (matcher.matches()) {
			// force scheme to lowercase
			String scheme = matcher.group(1);
			String lcScheme = scheme.toLowerCase();
			if (!lcScheme.equals(scheme)) {
				inUrl = lcScheme + matcher.group(2);
			}
			if (hasSpace && Patterns.WEB_URL.matcher(inUrl).matches()) {
				inUrl = inUrl.replace(" ", "%20");
			}
			return inUrl;
		}
		if (!hasSpace) {
			if (Patterns.WEB_URL.matcher(inUrl).matches()) {
				return URLUtil.guessUrl(inUrl);
			}
		}
		if (canBeSearch) {
			return URLUtil.composeSearchUrl(inUrl, QUICKSEARCH_G,
					QUERY_PLACE_HOLDER);
		}
		return null;
	}

	public static void addToFav(final Context context, final String title,
			final String url, final Bitmap bitmap) {
		final ContentResolver resolver = context.getContentResolver();
		new AsyncTask<Void, Void, Integer>() {

			@Override
			protected Integer doInBackground(Void... params) {
				Cursor cursor = null;
				try {
					cursor = resolver.query(BrowserProvider.TABLE_FAV_URI,
							null, BrowserProvider.TABLE_FAV_REALURL + "=?",
							new String[] {
								removeParametersFromUrl(url)
							}, null);
					if (cursor != null && cursor.getCount() > 0) {
						return Integer.valueOf(Constants.ALREADY_EXIST);
					} else {
						ContentValues values = new ContentValues();
						values.put(BrowserProvider.TABLE_FAV_TITLE, title);
						values.put(BrowserProvider.TABLE_FAV_URL, url);
						values.put(BrowserProvider.TABLE_FAV_REALURL, removeParametersFromUrl(url));
						if (bitmap != null) {
							ByteArrayOutputStream os = new ByteArrayOutputStream();
							bitmap.compress(CompressFormat.PNG, 100, os);
							values.put(BrowserProvider.TABLE_FAV_ICON,
									os.toByteArray());
						}
						Uri uri = resolver.insert(
								BrowserProvider.TABLE_FAV_URI, values);
						if (ContentUris.parseId(uri) == -1) {
							return Integer.valueOf(Constants.ADD_FAIL);
						} else {
							return Integer.valueOf(Constants.ADD_OK);
						}
					}
				} finally {
					if (cursor != null && !cursor.isClosed()) {
						cursor.close();
						cursor = null;
					}
				}
			}

			protected void onPostExecute(Integer result) {
				int id = result.intValue();
				switch (id) {
					case Constants.ADD_FAIL:
						Toast.makeText(context, R.string.add_fav_fail,
								Toast.LENGTH_SHORT).show();
						break;
					case Constants.ADD_OK:
						Toast.makeText(context, R.string.add_fav_ok,
								Toast.LENGTH_SHORT).show();
						break;
					case Constants.ALREADY_EXIST:
						Toast.makeText(context, R.string.add_fav_already_exist,
								Toast.LENGTH_SHORT).show();
						break;
				}
			};
		}.execute();
	}

	public static void addToNavi(final Context context, final String title,
			final String url, final Bitmap bitmap) {
		final ContentResolver resolver = context.getContentResolver();
		new AsyncTask<Void, Void, Integer>() {

			@Override
			protected Integer doInBackground(Void... params) {
				Cursor cursor = null;
				try {
					cursor = resolver.query(
							BrowserProvider.TABLE_INDEX_NAVI_URI, null,
							BrowserProvider.TABLE_NAVI_REALURL + "=?",
							new String[] {
								removeParametersFromUrl(url)
							}, null);
					if (cursor != null && cursor.getCount() > 0) {
						return Integer.valueOf(Constants.ALREADY_EXIST);
					} else {
						ContentValues values = new ContentValues();
						values.put(BrowserProvider.TABLE_NAVI_TITLE, title);
						values.put(BrowserProvider.TABLE_NAVI_URL, url);
						values.put(BrowserProvider.TABLE_NAVI_REALURL, removeParametersFromUrl(url));
						if (bitmap != null) {
							ByteArrayOutputStream os = new ByteArrayOutputStream();
							bitmap.compress(CompressFormat.PNG, 100, os);
							values.put(BrowserProvider.TABLE_NAVI_ICON,
									os.toByteArray());
						}
						Uri uri = resolver.insert(
								BrowserProvider.TABLE_INDEX_NAVI_URI, values);
						if (ContentUris.parseId(uri) == -1) {
							return Integer.valueOf(Constants.ADD_FAIL);
						} else {
							return Integer.valueOf(Constants.ADD_OK);
						}
					}
				} finally {
					if (cursor != null && !cursor.isClosed()) {
						cursor.close();
						cursor = null;
					}
				}
			}

			protected void onPostExecute(Integer result) {
				int id = result.intValue();
				switch (id) {
					case Constants.ADD_FAIL:
						Toast.makeText(context, R.string.add_navi_fail,
								Toast.LENGTH_SHORT).show();
						break;
					case Constants.ADD_OK:
						Toast.makeText(context, R.string.add_navi_ok,
								Toast.LENGTH_SHORT).show();
						break;
					case Constants.ALREADY_EXIST:
						Toast.makeText(context, R.string.add_navi_already_exist,
								Toast.LENGTH_SHORT).show();
						break;
					case Constants.NAVI_TO_LIMIT:
						Toast.makeText(context, R.string.navi_to_limit,
								Toast.LENGTH_SHORT).show();
						break;
				}
			};
		}.execute();
	}

	public static void createShortCut(Context context, String shortcutName,
			String url) {
		Intent intent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");

		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
		intent.putExtra("duplicate", false);

		ShortcutIconResource resource = Intent.ShortcutIconResource
				.fromContext(context, R.drawable.ic_launcher);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, resource);

		Intent shortIntent = new Intent();
		shortIntent.setClass(context, HomeActivity.class);
		shortIntent.setAction(Intent.ACTION_MAIN);
		shortIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		if (!TextUtils.isEmpty(url)) {
			shortIntent.setData(Uri.parse(url));
		}
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortIntent);

		context.sendBroadcast(intent);
	}

	private static final String[] UNITS = new String[] {
			"B", "KB", "MB", "GB",
			"TB", "PB"
	};
	private static final DecimalFormat sSizeFormat = new DecimalFormat(
			"###0.##");

	/**
	 * Formats a file size according to the binary size orders (KB, MB, ...),
	 * Better performance than the android.text.format.Formatter.formatFileSize
	 * 
	 * @param size file size
	 * @return formatted size value
	 */
	public static String formatFileSize(final long size) {
		if (size <= 0)
			return "0";
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		if (digitGroups >= UNITS.length) {
			digitGroups = UNITS.length - 1;
		}
		return sSizeFormat.format(size / Math.pow(1024, digitGroups))
				+ UNITS[digitGroups];
	}

	/* 判断文件MimeType的method */
	public static String getMIMEType(File f)
	{
		String type = "";
		String fName = f.getName();
		/* 取得扩展名 */
		String end = fName.substring(fName.lastIndexOf(".")
				+ 1, fName.length()).toLowerCase();

		/* 依扩展名的类型决定MimeType */
		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") ||
				end.equals("xmf") || end.equals("ogg") || end.equals("wav"))
		{
			type = "audio";
		}
		else if (end.equals("3gp") || end.equals("mp4"))
		{
			type = "video";
		}
		else if (end.equals("jpg") || end.equals("gif") || end.equals("png") ||
				end.equals("jpeg") || end.equals("bmp"))
		{
			type = "image";
		}
		else if (end.equals("apk"))
		{
			/* android.permission.INSTALL_PACKAGES */
			type = "application/vnd.android.package-archive";
		}
		else
		{
			type = "*";
		}
		/* 如果无法直接打开，就跳出软件列表给用户选择 */
		if (end.equals("apk"))
		{
		}
		else
		{
			type += "/*";
		}
		return type;
	}

	public static void showMessage(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static void showNaviEditDialog(final Context context, final FavItem item) {
		int titleRes = (item != null) ? R.string.edit_navi : R.string.add_navi;
		int okRes = (item != null) ? R.string.ok : R.string.add_navi;
		LayoutInflater factory = LayoutInflater.from(context);
		final View textEntryView = factory.inflate(R.layout.fav_add_edit_dialog_layout, null);
		final EditText titleEditText = (EditText) textEntryView.findViewById(R.id.title_edit);
		final EditText urlEditText = (EditText) textEntryView.findViewById(R.id.address_edit);
		if (item != null) {
			titleEditText.setText(item.title);
			urlEditText.setText(item.url);
			Selection.selectAll(titleEditText.getText());
			Selection.selectAll(urlEditText.getText());
		}
		new AlertDialog.Builder(context)
				.setTitle(titleRes)
				.setView(textEntryView)
				.setPositiveButton(okRes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String title = titleEditText.getText().toString().trim();
						String url = urlEditText.getText().toString().trim();
						if (TextUtils.isEmpty(title) || TextUtils.isEmpty(url)) {
							showMessage(context, context.getString(R.string.fill_all_first));
							return;
						}
						if (item == null) {
							addToNavi(context, title, url, null);
						} else {
							final ContentValues values = new ContentValues();
							values.put(BrowserProvider.TABLE_NAVI_TITLE, title);
							values.put(BrowserProvider.TABLE_NAVI_URL, url);
							new AsyncTask<Void, Void, Void>() {

								@Override
								protected Void doInBackground(Void... params) {
									context.getContentResolver().update(
											BrowserProvider.TABLE_INDEX_NAVI_URI, values,
											BrowserProvider.TABLE_NAVI_URL + "=?", new String[] {
												item.url
											});
									return null;
								}

							}.execute();
						}
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	public static void showFavEditDialog(final Context context, final FavItem item) {
		if (item == null) {
			return;
		}
		LayoutInflater factory = LayoutInflater.from(context);
		final View textEntryView = factory.inflate(R.layout.fav_add_edit_dialog_layout, null);
		final EditText titleEditText = (EditText) textEntryView.findViewById(R.id.title_edit);
		final EditText urlEditText = (EditText) textEntryView.findViewById(R.id.address_edit);
		if (item != null) {
			titleEditText.setText(item.title);
			urlEditText.setText(item.url);
			Selection.selectAll(titleEditText.getText());
			Selection.selectAll(urlEditText.getText());
		}
		new AlertDialog.Builder(context)
				.setTitle(R.string.edit_fav)
				.setView(textEntryView)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String title = titleEditText.getText().toString().trim();
						String url = urlEditText.getText().toString().trim();
						if (TextUtils.isEmpty(title) || TextUtils.isEmpty(url)) {
							showMessage(context, context.getString(R.string.fill_all_first));
							return;
						}
						final ContentValues values = new ContentValues();
						values.put(BrowserProvider.TABLE_FAV_TITLE, title);
						values.put(BrowserProvider.TABLE_FAV_URL, url);
						new AsyncTask<Void, Void, Void>() {

							@Override
							protected Void doInBackground(Void... params) {
								context.getContentResolver().update(
										BrowserProvider.TABLE_FAV_URI, values,
										BrowserProvider.TABLE_FAV_URL + "=?", new String[] {
											item.url
										});
								return null;
							}

						}.execute();
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	public static String getVersionName(Context context) {
		String versionName;
		try {
			versionName = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (Exception e) {
			versionName = null;
		}

		return versionName;
	}

	public static void updateHtml() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					return null;
				}
				File desFile = new File(Environment.getExternalStorageDirectory(),
						Constants.SDCARD_FOLDER + "/html.tar");
				if (!desFile.getParentFile().exists()) {
					desFile.getParentFile().mkdirs();
				}
				InputStream in = null;
				FileOutputStream fos = null;
				HttpURLConnection connection = null;
				try {
					URL dUrl = new URL(Constants.HTML_URL);
					connection = (HttpURLConnection) dUrl.openConnection();
					in = connection.getInputStream();
					fos = new FileOutputStream(desFile);
					int len = -1;
					byte[] buffer = new byte[1024];
					while ((len = in.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						in = null;
					}
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						fos = null;
					}
					if (connection != null) {
						connection.disconnect();
						connection = null;
					}
				}
				File file = new File(Environment.getExternalStorageDirectory(),
						Constants.SDCARD_HTML_FOLDER);
				if (file.exists() && file.isDirectory()) {
					RecursionDeleteFile(file);
				}
				unpackZip(desFile.getParentFile().getAbsolutePath() + "/", "html.tar");
				desFile.delete();
				return null;
			}

		}.execute();
	}

	public static void RecursionDeleteFile(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}
		if (file.isDirectory()) {
			File[] childFile = file.listFiles();
			if (childFile == null || childFile.length == 0) {
				file.delete();
				return;
			}
			for (File f : childFile) {
				RecursionDeleteFile(f);
			}
			file.delete();
		}
	}

	private static boolean unpackZip(String path, String zipName)
	{
		InputStream is;
		ZipInputStream zis;
		try
		{
			String filename;
			is = new FileInputStream(path + zipName);
			zis = new ZipInputStream(new BufferedInputStream(is));
			ZipEntry ze;
			byte[] buffer = new byte[1024];
			int count;

			while ((ze = zis.getNextEntry()) != null)
			{
				// zapis do souboru
				filename = ze.getName();
				// Need to create directories if not exists, or
				// it will generate an Exception...
				if (ze.isDirectory()) {
					File fmd = new File(path + filename);
					fmd.mkdirs();
					continue;
				}

				FileOutputStream fout = new FileOutputStream(path + filename);

				// cteni zipu a zapis
				while ((count = zis.read(buffer)) != -1)
				{
					fout.write(buffer, 0, count);
				}

				fout.close();
				zis.closeEntry();
			}

			zis.close();
		} catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		return info == null ? false : true;
	}
}
