
package com.fujun.browser.utils;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fujun.browser.Tab;
import com.fujun.browser.view.TitleBar;
import com.kukuai.daohang.R;

public class MyWebViewClient extends WebViewClient {

	private Tab mTab;
	private TitleBar mTitleBar;

	public MyWebViewClient(Tab tab) {
		mTab = tab;
	}

	public void setTitleBar(TitleBar titleBar) {
		mTitleBar = titleBar;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		return false;
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		view.clearMatches();
		mTab.setCurrentUrl(url);
		mTab.setLoading(true);
		if (view.getProgress() < Utils.getLimitedProgress(mTab.getContext())) {
			mTab.getWebChromeClient().setShown(false);
		}
		mTitleBar.setProgressbar(View.VISIBLE);
		mTitleBar.setHint(mTab.getContext().getString(R.string.page_loading));
		mTitleBar.setMode(TitleBar.WEBVIEW_MODE);
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		mTab.setCurrentLoadProgress(100);
		mTitleBar.setProgress(100);
		mTitleBar.setProgressbar(View.GONE);
		mTab.insertHistory();
		mTab.setLoading(false);
		mTab.onWebViewMove();
		view.requestFocus();
		if (TextUtils.isEmpty(mTab.getCurrentTitle())) {
			mTab.setCurrentTitle(url);
			mTitleBar.setTitle(url);
		}
	}
}
