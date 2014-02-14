
package com.fujun.browser;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;

import com.fujun.browser.activity.HomeActivity;
import com.fujun.browser.constants.Constants;
import com.fujun.browser.view.SlidingWebView.OnWebViewMoveListener;
import com.fujun.browser.view.TitleBar;

public class TabManager {

	public interface onTabsChangedListener {
		public void onTabsCountChanged();
	}

	private static TabManager mInstance;
	private SettingManager mSettingManager;
	private ArrayList<Tab> mTabs;
	private int mPreActiveTabId = -1;
	private int mActiveTabId = -1;
	private ArrayList<onTabsChangedListener> mListeners;
	private ViewGroup mTabParent;
	private ViewGroup mViewRoot;
	private OnWebViewMoveListener mWebViewMoveListener;
	private Context mContext;

	private TabManager(Context context, ViewGroup tabParent, ViewGroup viewRoot) {
		if (tabParent == null || viewRoot == null) {
			throw new IllegalArgumentException();
		}
		mContext = context;
		mTabs = new ArrayList<Tab>();
		mListeners = new ArrayList<TabManager.onTabsChangedListener>();
		mTabParent = tabParent;
		mViewRoot = viewRoot;
		mSettingManager = SettingManager.getSettingManager();
	}

	public void setOnWebViewMoveListener(OnWebViewMoveListener listener) {
		mWebViewMoveListener = listener;
	}

	public static TabManager getInstance(Context context, ViewGroup tabParent, ViewGroup viewRoot) {
		if (mInstance == null) {
			mInstance = new TabManager(context, tabParent, viewRoot);
		}
		return mInstance;
	}

	public int getTabCount() {
		return mTabs == null ? 0 : mTabs.size();
	}

	public Tab getActiveTab() {
		return (mTabs == null && mActiveTabId != -1) ? null : mTabs.get(mActiveTabId);
	}

	private void notifyTabsChanged() {
		if (mListeners != null) {
			for (onTabsChangedListener listener : mListeners) {
				listener.onTabsCountChanged();
			}
		}
	}

	public void setOnTabsChangedListener(onTabsChangedListener listener) {
		mListeners.add(listener);
	}

	public Tab createNewTab(HomeActivity activity) {
		Tab tab = new Tab(activity, mTabParent, mWebViewMoveListener);
		mSettingManager.initWithSettings(tab);
		mTabs.add(tab);
		mPreActiveTabId = mActiveTabId;
		mActiveTabId = mTabs.indexOf(tab);
		switchTab();
		notifyTabsChanged();
		return tab;
	}

	public void setActiveTabId(int id) {
		if (id < 0 || id >= mTabs.size()) {
			throw new IllegalArgumentException();
		}
		mPreActiveTabId = mActiveTabId;
		mActiveTabId = id;
	}

	public int getActiveTabId() {
		return mActiveTabId;
	}

	public void deleteTab(int index) {
		if (index >= 0 && index < Constants.MAX_TAB_COUNT) {
			if (index < mActiveTabId) {
				mActiveTabId--;
				mPreActiveTabId = mActiveTabId;
			} else if (index == mActiveTabId) {
				mPreActiveTabId = index;
				mActiveTabId = index == 0 ? ++mActiveTabId : --mActiveTabId;
				switchTab();
			}
			Tab tab = mTabs.get(index);
			tab.destroy();
			mTabs.remove(index);
		}
		notifyTabsChanged();
	}

	public ArrayList<Tab> getTabs() {
		return mTabs;
	}

	public void release() {
		if (mTabs != null) {
			for (Tab tab : mTabs) {
				tab.destroy();
			}
			mTabs.clear();
		}
		if (mListeners != null) {
			mListeners.clear();
		}
		if (mInstance != null) {
			mInstance = null;
		}
	}

	public void switchTab() {
		hideTab(mPreActiveTabId);
		showTab(mActiveTabId);
		if (mWebViewMoveListener != null) {
			mWebViewMoveListener.onWebViewMove();
		}
	}

	public void showTab(int index) {
		if (index < 0 || index >= mTabs.size()) {
			return;
		}
		Tab tab = mTabs.get(index);
		TitleBar titleBar = tab.getTitleBar();
		mViewRoot.addView(titleBar, 0);
		titleBar.setVisibility(View.VISIBLE);
		WebView webView = tab.getWebView();
		tab.putInForeground();
		if (tab.getSlideFromHome()) {
			mTabParent.addView(webView);
		}
	}

	public void hideTab(int index) {
		if (index < 0 || index >= mTabs.size()) {
			return;
		}
		Tab tab = mTabs.get(index);
		TitleBar titleBar = tab.getTitleBar();
		titleBar.setVisibility(View.GONE);
		mViewRoot.removeView(titleBar);
		WebView webView = tab.getWebView();
		if (mTabParent.indexOfChild(webView) != -1) {
			tab.putInBackground();
			mTabParent.removeView(webView);
		}
	}

	public void clearCache() {
		for (Tab tab : mTabs) {
			tab.getWebView().clearCache(true);
		}
	}

	public void clearPasswd() {
		WebViewDatabase db = WebViewDatabase.getInstance(mContext);
		db.clearUsernamePassword();
		db.clearHttpAuthUsernamePassword();
	}

	public void clearForm() {
		WebViewDatabase db = WebViewDatabase.getInstance(mContext);
		db.clearFormData();
		mTabs.get(mActiveTabId).getWebView().clearFormData();
	}

	public void clearCookie() {
		CookieManager.getInstance().removeAllCookie();
	}

	public void clearLocationAccess(Context context) {
		GeolocationPermissions.getInstance().clearAll();
	}
}
