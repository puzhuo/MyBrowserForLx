
package com.fujun.browser.fragment;

import java.io.File;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fujun.browser.constants.Constants;
import com.fujun.browser.utils.WebViewFactory;

public class IndexNaviFragmentNew extends Fragment {

	public interface NaviNewClickListener {
		public void onNewNaviClick(String url);
	}

	private WebView mWebView;
	private NaviNewClickListener mListener;
	private String mIndexUrl;

	private String judgeUrl() {
		String urlString = null;
		File file = new File(Environment.getExternalStorageDirectory(),
				Constants.SDCARD_HTML_FOLDER + "/index.html");
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				&& file.exists()) {
			urlString = Constants.SDCARD_HTML_URL;
		} else {
			urlString = Constants.ASSETS_HTML_URL;
		}
		if (Constants.DEBUG) {
			Log.e("fujun", "mIndexUrl = " + urlString);
		}
		return urlString;
	}

	public WebView getWebView() {
		return mWebView;
	}

	public void refresh() {
		mIndexUrl = judgeUrl();
		mWebView.loadUrl(mIndexUrl);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!judgeUrl().equals(mIndexUrl)) {
			mIndexUrl = judgeUrl();
			mWebView.loadUrl(mIndexUrl);
		}
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				if (!mIndexUrl.equals(url) && mListener != null) {
					mWebView.stopLoading();
					mListener.onNewNaviClick(url);
				}
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mWebView = WebViewFactory.getInstance().getWebView(getActivity(), null);
		return mWebView;
	}

	public void setNaviNewClickListener(NaviNewClickListener listener) {
		mListener = listener;
	}

}
