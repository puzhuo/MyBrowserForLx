
package com.fujun.browser;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.fujun.browser.activity.HomeActivity.MyWebChromeClient;
import com.fujun.browser.constants.Constants;
import com.fujun.browser.download.BrowserDownloadListener;
import com.fujun.browser.model.HisItem;
import com.fujun.browser.utils.MyWebViewClient;
import com.fujun.browser.utils.Utils;
import com.fujun.browser.utils.WebViewFactory;
import com.fujun.browser.view.SlidingWebView;
import com.fujun.browser.view.SlidingWebView.OnWebViewMoveListener;
import com.fujun.browser.view.TitleBar;
import com.kukuai.daohang.R;

public class Tab {

	private SlidingWebView mWebView;
	private MyWebViewClient mWebViewClient;
	private MyWebChromeClient mWebChromeClient;
	private String mCurrentUrl;
	private String mCurrentTitle;
	private String mDefaultTitle;
	private int mCurrentLoadProgress;
	private boolean mInForeground;
	private TitleBar mTitleBar;
	private boolean mWebViewVisible;
	private ViewGroup mTabParent;
	private OnWebViewMoveListener mWebViewMoveListener;
	private Context mContext;
	private boolean mClearHistory;
	private boolean mLoading;

	public void stopLoading() {
		setLoading(false);
		mWebView.stopLoading();
		onWebViewMove();
	}

	public void setLoading(boolean loading) {
		mLoading = loading;
		if (mWebViewMoveListener != null) {
			mWebViewMoveListener.onLoadingStateChanged(loading);
		}
	}

	public boolean getLoading() {
		return mLoading;
	}

	public void clearHistory() {
		mWebView.clearHistory();
	}

	public boolean getClearHistory() {
		return mClearHistory;
	}

	public void setClearHistory(boolean clearHistory) {
		mClearHistory = clearHistory;
	}

	public void setWebViewVisible(boolean visible) {
		mWebViewVisible = visible;
		if (mWebViewVisible) {
			mWebView.setVisibility(View.VISIBLE);
			mWebView.requestFocus();
			mTitleBar.setMode(TitleBar.WEBVIEW_MODE);
		} else {
			mWebView.setVisibility(View.GONE);
			mTitleBar.setProgressbar(View.GONE);
			mTitleBar.setMode(TitleBar.HOME_MODE);
		}
		onWebViewMove();
	}

	public void insertHistory() {
		HisItem item = new HisItem();
		item.url = mWebView.getUrl();
		item.title = mWebView.getTitle();
		if (TextUtils.isEmpty(item.title)) {
			item.title = item.url;
		}
		item.icon = mWebView.getFavicon();
		item.accessTime = System.currentTimeMillis();
		Utils.insertHistory(mTabParent.getContext(), item);
	}

	public boolean getWebViewVisible() {
		return mWebViewVisible;
	}

	public void clearFocus() {
		mWebView.clearFocus();
	}

	public void requestFocus() {
		if (mWebViewVisible) {
			mWebView.requestFocus();
		}
	}

	public Bitmap getCurrentFav() {
		return mWebView.getFavicon();
	}

	public int getCurrentLoadProgress() {
		return mCurrentLoadProgress;
	}

	public boolean isVideoOn() {
		return mWebChromeClient.isVideoOn();
	}

	public void hideVideoView() {
		mWebChromeClient.onHideCustomView();
	}

	public void loadUrl(String url, boolean showMessage) {
		if (showMessage && TextUtils.isEmpty(url)) {
			Utils.showMessage(mContext, mContext.getString(R.string.wrong_url));
			return;
		}
		if (!Constants.ASSETS_HTML_URL.equals(url) && !Constants.SDCARD_HTML_URL.equals(url)
				&& !Utils.isNetworkAvailable(mContext)) {
			Utils.showMessage(mContext, mContext.getString(R.string.network_disconnect));
			return;
		}
		mWebView.loadUrl(url);
	}

	public void setCurrentLoadProgress(int progress) {
		mCurrentLoadProgress = progress;
	}

	public void moveToWebView() {
		if (getSlideFromHome()) {
			show();
		}
	}

	public boolean canGoBack() {
		return mWebViewVisible;
	}

	public void goBack() {
		if (canGoBack()) {
			if (mWebView.canGoBack()) {
				mWebView.goBack();
			} else {
				setWebViewVisible(false);
			}
		}
		onWebViewMove();
	}

	public boolean canGoForward() {
		if (mWebViewVisible) {
			return mWebView.canGoForward();
		} else {
			return getSlideFromHome();
		}
	}

	public void goForward() {
		if (canGoForward()) {
			if (!mWebViewVisible) {
				setWebViewVisible(true);
			} else {
				mWebView.goForward();
			}
		}
		onWebViewMove();
	}

	public void onWebViewMove() {
		if (mWebViewMoveListener != null) {
			mWebViewMoveListener.onWebViewMove();
		}
	}

	public void clearCache() {
		mWebView.clearCache(true);
	}

	public String getCurrentUrl() {
		if (mInForeground) {
			return mWebViewVisible ? mCurrentUrl : null;
		} else {
			return mCurrentUrl;
		}

	}

	public void refresh() {
		mWebView.reload();
	}

	// indicates if this tab can show by sliding from home
	public boolean getSlideFromHome() {
		return mCurrentUrl != null;
	}

	public void setCurrentUrl(String mCurrentUrl) {
		this.mCurrentUrl = mCurrentUrl;
	}

	public String getCurrentTitle() {
		return mWebViewVisible ? mCurrentTitle : mDefaultTitle;
	}

	public void setCurrentTitle(String mCurrentTitle) {
		this.mCurrentTitle = mCurrentTitle;
	}

	public Tab(Context context, ViewGroup tabParent, OnWebViewMoveListener listener) {
		mWebView = (SlidingWebView) WebViewFactory.getInstance().getWebView(
				context, listener);
		mWebView.setTab(this);
		mWebViewClient = new MyWebViewClient(this);
		mWebChromeClient = new MyWebChromeClient(context, this);

		mWebView.setDownloadListener(new BrowserDownloadListener(context,
				this));
		mWebView.setWebViewClient(mWebViewClient);
		mWebView.setWebChromeClient(mWebChromeClient);
		mWebView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.requestFocus();
				return false;
			}
		});

		mDefaultTitle = context.getString(R.string.default_tab_title);

		mTitleBar = new TitleBar(context, this);
		mTitleBar.setMode(TitleBar.HOME_MODE);
		mWebViewClient.setTitleBar(mTitleBar);
		mWebChromeClient.setTitleBar(mTitleBar);

		mTabParent = tabParent;
		mWebViewMoveListener = listener;

		mContext = context;
	}

	public Context getContext() {
		return mContext;
	}

	public void show() {
		mWebChromeClient.setShown(true);
		if (mTabParent.indexOfChild(mWebView) == -1) {
			mTabParent.addView(mWebView);
		}
		setWebViewVisible(true);
	}

	public TitleBar getTitleBar() {
		return mTitleBar;
	}

	public boolean inForeground() {
		return mInForeground;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void putInBackground() {
		mInForeground = false;
		mWebView.onPause();
	}

	public void destroy() {
		mWebView.destroy();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void putInForeground() {
		mInForeground = true;
		mWebView.onResume();
	}

	public WebView getWebView() {
		return mWebView;
	}

	public MyWebChromeClient getWebChromeClient() {
		return mWebChromeClient;
	}

	public MyWebViewClient getWebViewClient() {
		return mWebViewClient;
	}

}
