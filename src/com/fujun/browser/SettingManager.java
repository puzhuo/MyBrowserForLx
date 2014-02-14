
package com.fujun.browser;

import java.util.ArrayList;

import com.kukuai.daohang.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;

public class SettingManager implements OnSharedPreferenceChangeListener {

	public static final String SETTING_PREFERENCE_SCREEN = "setting_preference_screen";
	public static final String SETTING_IF_WITH_IMAGE = "if_with_image";
	public static final String SETTING_ACCEPT_COOKIES = "accept_cookies";
	public static final String SETTING_SAVE_PASSWD = "save_passwd";
	public static final String SETTING_SAVE_FORM = "save_form";
	public static final String SETTING_ENABLE_LOCATION = "enable_location";
	public static final String SETTING_SHOW_SECURITY = "show_security";

	public static final String PREF_MIN_FONT_SIZE = "min_font_size";
	public static final String PREF_TEXT_SIZE = "text_size";
	public static final String PREF_TEXT_ZOOM = "text_zoom";
	public static final String PREF_DOUBLE_TAP_ZOOM = "double_tap_zoom";
	public static final String PREF_FORCE_USERSCALABLE = "force_userscalable";
	public static final String PREF_INVERTED_CONTRAST = "inverted_contrast";

	public static final String PREF_DEFAULT_TEXT_ENCODING = "default_text_encoding";
	public static final String PREF_DEFAULT_ZOOM = "default_zoom";
	public static final String PREF_PLUGIN_STATE = "plugin_state";
	public static final String PREF_BLOCK_POPUP_WINDOWS = "block_popup_windows";
	public static final String PREF_LOAD_PAGE = "load_page";
	public static final String PREF_RESET_DEFAULT = "reset_default_preferences";

	// The minimum min font size
	// Aka, the lower bounds for the min font size range
	// which is 1:5..24
	private static final int MIN_FONT_SIZE_OFFSET = 5;
	// The initial value in the text zoom range
	// This is what represents 100% in the SeekBarPreference range
	private static final int TEXT_ZOOM_START_VAL = 10;
	// The size of a single step in the text zoom range, in percent
	private static final int TEXT_ZOOM_STEP = 5;
	// The initial value in the double tap zoom range
	// This is what represents 100% in the SeekBarPreference range
	private static final int DOUBLE_TAP_ZOOM_START_VAL = 5;
	// The size of a single step in the double tap zoom range, in percent
	private static final int DOUBLE_TAP_ZOOM_STEP = 5;

	public static SettingManager mInstance;
	private SharedPreferences mPreferences;
	private float mFontSizeMult = 1.0f;
	private Context mContext;

	public SettingManager(Context context) {
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		mPreferences.registerOnSharedPreferenceChangeListener(this);

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		mFontSizeMult = metrics.scaledDensity / metrics.density;

		mContext = context;
	}

	public static void initiant(Context context) {
		mInstance = new SettingManager(context);
	}

	public static SettingManager getSettingManager() {
		return mInstance;
	}

	public void setIfWithImage(boolean ifWith) {
		mPreferences.edit().putBoolean(SETTING_IF_WITH_IMAGE, ifWith).commit();
	}

	public boolean showSecurityWarning() {
		return mPreferences.getBoolean(SETTING_SHOW_SECURITY, true);
	}

	public boolean getIfWithImage() {
		return mPreferences.getBoolean(SETTING_IF_WITH_IMAGE, false);
	}

	public boolean getAcceptCookies() {
		return mPreferences.getBoolean(SETTING_ACCEPT_COOKIES, true);
	}

	public boolean getSavePasswd() {
		return mPreferences.getBoolean(SETTING_SAVE_PASSWD, true);
	}

	public boolean getSaveForm() {
		return mPreferences.getBoolean(SETTING_SAVE_FORM, true);
	}

	public boolean getEnableLocation() {
		return mPreferences.getBoolean(SETTING_ENABLE_LOCATION, true);
	}

	public static int getAdjustedMinimumFontSize(int rawValue) {
		rawValue++; // Preference starts at 0, min font at 1
		if (rawValue > 1) {
			rawValue += (MIN_FONT_SIZE_OFFSET - 2);
		}
		return rawValue;
	}

	public int getAdjustedTextZoom(int rawValue) {
		rawValue = (rawValue - TEXT_ZOOM_START_VAL) * TEXT_ZOOM_STEP;
		return (int) ((rawValue + 100) * mFontSizeMult);
	}

	static int getRawTextZoom(int percent) {
		return (percent - 100) / TEXT_ZOOM_STEP + TEXT_ZOOM_START_VAL;
	}

	public int getAdjustedDoubleTapZoom(int rawValue) {
		rawValue = (rawValue - DOUBLE_TAP_ZOOM_START_VAL) * DOUBLE_TAP_ZOOM_STEP;
		return (int) ((rawValue + 100) * mFontSizeMult);
	}

	static int getRawDoubleTapZoom(int percent) {
		return (percent - 100) / DOUBLE_TAP_ZOOM_STEP + DOUBLE_TAP_ZOOM_START_VAL;
	}

	public int getMinimumFontSize() {
		int minFont = mPreferences.getInt(PREF_MIN_FONT_SIZE, 0);
		return getAdjustedMinimumFontSize(minFont);
	}

	public boolean forceEnableUserScalable() {
		return mPreferences.getBoolean(PREF_FORCE_USERSCALABLE, false);
	}

	public int getTextZoom() {
		int textZoom = mPreferences.getInt(PREF_TEXT_ZOOM, 10);
		return getAdjustedTextZoom(textZoom);
	}

	public void setTextZoom(int percent) {
		mPreferences.edit().putInt(PREF_TEXT_ZOOM, getRawTextZoom(percent)).apply();
	}

	public int getDoubleTapZoom() {
		int doubleTapZoom = mPreferences.getInt(PREF_DOUBLE_TAP_ZOOM, 5);
		return getAdjustedDoubleTapZoom(doubleTapZoom);
	}

	public void setDoubleTapZoom(int percent) {
		mPreferences.edit().putInt(PREF_DOUBLE_TAP_ZOOM, getRawDoubleTapZoom(percent)).apply();
	}

	public String getDefaultTextEncoding() {
		return mPreferences.getString(PREF_DEFAULT_TEXT_ENCODING, null);
	}

	public ZoomDensity getDefaultZoom() {
		String zoom = mPreferences.getString(PREF_DEFAULT_ZOOM, "MEDIUM");
		return ZoomDensity.valueOf(zoom);
	}

	public PluginState getPluginState() {
		String state = mPreferences.getString(PREF_PLUGIN_STATE, "ON");
		return PluginState.valueOf(state);
	}

	public boolean blockPopupWindows() {
		return mPreferences.getBoolean(PREF_BLOCK_POPUP_WINDOWS, true);
	}

	public boolean loadPageInOverviewMode() {
		return mPreferences.getBoolean(PREF_LOAD_PAGE, true);
	}

	public void initWithSettings(Tab tab) {
		if (tab != null) {
			WebView webView = tab.getWebView();
			WebSettings settings = webView.getSettings();
			settings.setBlockNetworkImage(getIfWithImage());
			settings.setSavePassword(getSavePasswd());
			settings.setSaveFormData(getSaveForm());
			settings.setGeolocationEnabled(getEnableLocation());
			settings.setMinimumFontSize(getMinimumFontSize());
			settings.setTextZoom(getTextZoom());
			settings.setDefaultTextEncodingName(getDefaultTextEncoding());
			settings.setDefaultZoom(getDefaultZoom());
			settings.setPluginState(getPluginState());
			settings.setJavaScriptCanOpenWindowsAutomatically(!blockPopupWindows());
			settings.setLoadWithOverviewMode(loadPageInOverviewMode());

			CookieManager.getInstance().setAcceptCookie(getAcceptCookies());
		}
	}

	public void resetDefault() {
		Editor editor = mPreferences.edit();
		editor.putBoolean(SETTING_SHOW_SECURITY, true);
		editor.putBoolean(SETTING_ACCEPT_COOKIES, true);
		editor.putBoolean(SETTING_SAVE_FORM, true);
		editor.putBoolean(SETTING_SAVE_PASSWD, true);
		editor.putBoolean(SETTING_ENABLE_LOCATION, true);

		editor.putBoolean(PREF_FORCE_USERSCALABLE, false);
		editor.putBoolean(PREF_LOAD_PAGE, true);
		editor.putBoolean(PREF_BLOCK_POPUP_WINDOWS, true);

		String[] array = mContext.getResources().getStringArray(
				R.array.pref_default_text_encoding_values);
		editor.putString(PREF_DEFAULT_TEXT_ENCODING, array[0]);

		array = mContext.getResources().getStringArray(R.array.pref_content_plugins_values);
		editor.putString(PREF_PLUGIN_STATE, array[0]);

		array = mContext.getResources().getStringArray(R.array.pref_default_zoom_values);
		editor.putString(PREF_DEFAULT_ZOOM, array[0]);

		editor.commit();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		TabManager manager = TabManager.getInstance(null, null, null);
		ArrayList<Tab> tabs = manager.getTabs();
		for (Tab tab : tabs) {
			initWithSettings(tab);
		}
	}
}
