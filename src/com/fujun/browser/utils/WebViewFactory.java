
package com.fujun.browser.utils;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ZoomButtonsController;

import com.fujun.browser.view.SlidingWebView;
import com.fujun.browser.view.SlidingWebView.OnWebViewMoveListener;

public class WebViewFactory {

	private static WebViewFactory mInstance;

	public static WebViewFactory getInstance() {
		if (mInstance == null) {
			mInstance = new WebViewFactory();
		}
		return mInstance;
	}

	@SuppressLint("SetJavaScriptEnabled")
	public SlidingWebView getWebView(Context context, OnWebViewMoveListener listener) {
		SlidingWebView webView = new SlidingWebView(context);
		webView.setOnWebViewMoveListener(listener);
		webView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setPluginState(PluginState.ON);
		settings.setDomStorageEnabled(true);
		settings.setLoadsImagesAutomatically(true);
		settings.setNeedInitialFocus(false);

		settings.setUseWideViewPort(true);
		settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);

		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			// 用于判断是否为Android 3.0系统, 然后隐藏缩放控件
			settings.setDisplayZoomControls(false);
		} else {
			setZoomControlGone(webView); // Android 3.0(11) 以下使用以下方法
		}

		// cache settings
		settings.setAppCacheEnabled(true);
		settings.setAppCacheMaxSize(1024 * 1024 * 8);
		settings.setAppCachePath(context.getCacheDir().getAbsolutePath());
		settings.setDatabaseEnabled(true);
		settings.setDatabasePath(context.getDir("databases", 0).getPath());

		webView.setScrollbarFadingEnabled(true);
		webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

		return webView;
	}

	public void setZoomControlGone(WebView view) {
		Class<WebView> classType;
		Field field;
		try {
			classType = WebView.class;
			field = classType.getDeclaredField("mZoomButtonsController");
			field.setAccessible(true);
			ZoomButtonsController mZoomButtonsController = new ZoomButtonsController(view);
			mZoomButtonsController.getZoomControls().setVisibility(View.GONE);
			try {
				field.set(view, mZoomButtonsController);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
}
