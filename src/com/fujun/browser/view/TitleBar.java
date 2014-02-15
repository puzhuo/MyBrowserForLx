package com.fujun.browser.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Selection;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fujun.browser.Tab;
import com.fujun.browser.activity.FavHisActivity;
import com.fujun.browser.activity.HomeActivity;
import com.fujun.browser.constants.Constants;
import com.fujun.browser.utils.Utils;
import com.fujun.zxing.CaptureActivity;
import com.kukuai.daohang.R;

public class TitleBar extends LinearLayout implements OnClickListener {

	private ProgressBar mProgressBar;
	private ImageButton mFavButton;
	private TextView mFakeUrlText;
	private RelativeLayout mRealUrlText;
	private ImageButton mRefreshButton;
	private TextView mTitleText;
	private TextView mSearchBtn;
	private ImageButton mMoreButton;
	private Tab mTab;
	private PopupWindow mUrlPopupWindow;
	private PopupWindow mMorePopupWindow;
	private EditText mUrlEditText;
	private InputMethodManager mInputMethodManager;

	private int mMode;
	private int mPreviousMode;
	public static final int EDIT_MODE = 2;
	public static final int WEBVIEW_MODE = 1;
	public static final int HOME_MODE = 0;

	public TitleBar(Context context, Tab tab) {
		super(context);
		mTab = tab;
		mInputMethodManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		init();
	}

	public TitleBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public TitleBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void setPreviousMode() {
		setMode(mPreviousMode);
	}

	public void setMode(int mode) {
		switch (mode) {
		case WEBVIEW_MODE:
			mUrlEditText.clearFocus();
			mTab.requestFocus();
			mUrlEditText.setVisibility(View.GONE);
			mFakeUrlText.setVisibility(View.GONE);
			mRealUrlText.setVisibility(View.VISIBLE);
			break;
		case HOME_MODE:
			mUrlEditText.clearFocus();
			mTab.requestFocus();
			mUrlEditText.setVisibility(View.GONE);
			mFakeUrlText.setVisibility(View.VISIBLE);
			mRealUrlText.setVisibility(View.GONE);
			break;
		case EDIT_MODE:
			mUrlEditText.setVisibility(View.VISIBLE);
			mFakeUrlText.setVisibility(View.GONE);
			mRealUrlText.setVisibility(View.GONE);
			mTab.clearFocus();
			mUrlEditText.requestFocus();
			mInputMethodManager.showSoftInput(mUrlEditText,
					InputMethodManager.SHOW_FORCED);
			String url = mTab.getCurrentUrl();
			if (mTab.getWebViewVisible() && url != null) {
				mUrlEditText.setText(url);
				Selection.selectAll(mUrlEditText.getText());
			} else {
				mUrlEditText.setText("");
			}
			break;
		}
		mPreviousMode = mMode;
		mMode = mode;
	}

	public int getMode() {
		return mMode;
	}

	public int getPreviousMode() {
		return mPreviousMode;
	}

	private void init() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.titlebar_new, this);
		setBackgroundResource(R.drawable.tabhost_background);

		mProgressBar = (ProgressBar) findViewById(R.id.progress);
		mFavButton = (ImageButton) findViewById(R.id.titlebar_fav_btn);
		mFakeUrlText = (TextView) findViewById(R.id.titlebar_url_fake);
		mRealUrlText = (RelativeLayout) findViewById(R.id.titlebar_url_real);
		mRefreshButton = (ImageButton) findViewById(R.id.titlebar_refresh);
		mTitleText = (TextView) findViewById(R.id.titlebar_url);
		mSearchBtn = (TextView) findViewById(R.id.titlebar_search);
		mMoreButton = (ImageButton) findViewById(R.id.titlebar_more_btn);

		mFavButton.setOnClickListener(this);
		mFakeUrlText.setOnClickListener(this);
		mRealUrlText.setOnClickListener(this);
		mRefreshButton.setOnClickListener(this);
		mSearchBtn.setOnClickListener(this);
		mMoreButton.setOnClickListener(this);

		mUrlEditText = (EditText) findViewById(R.id.titlebar_edit);
	}

	public void setProgressbar(int visibility) {
		mProgressBar.setVisibility(visibility);
	}

	public void setHint(String hint) {
		mTitleText.setHint(hint);
	}

	public void setTitle(String title) {
		mTitleText.setHint(title);
		// mTitleText.setText(title);
	}

	public void setFavBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
			mTitleText.setCompoundDrawables(drawable, null, null, null);
		}
	}

	public void setProgress(int progress) {
		if (progress < 0) {
			throw new IllegalArgumentException("illegal progress");
		}
		if (progress < 100) {
			mProgressBar.setProgress(progress);
		} else {
			mProgressBar.setProgress(100);
		}
	}

	private void showUrlPopup() {
		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.url_popup_menu_layout, null);
		mUrlPopupWindow = new PopupWindow(view,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		mUrlPopupWindow.setFocusable(true);
		mUrlPopupWindow.setBackgroundDrawable(new ColorDrawable(
				Color.TRANSPARENT));
		mUrlPopupWindow.showAsDropDown(mFavButton);

		view.findViewById(R.id.url_menu_add_fav).setOnClickListener(this);
		view.findViewById(R.id.url_menu_open_fav_his).setOnClickListener(this);
		view.findViewById(R.id.url_menu_add_navi).setOnClickListener(this);
		view.findViewById(R.id.url_menu_send_desk).setOnClickListener(this);
	}

	private void dismissUrlPopMenu() {
		if (mUrlPopupWindow != null) {
			mUrlPopupWindow.dismiss();
			mUrlPopupWindow = null;
		}
	}

	private void showMorePopup() {
		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.url_more_popup_layout, null);
		mMorePopupWindow = new PopupWindow(view,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		mMorePopupWindow.setFocusable(true);
		mMorePopupWindow.setBackgroundDrawable(new ColorDrawable(
				Color.TRANSPARENT));
		mMorePopupWindow.showAsDropDown(mMoreButton);

		view.findViewById(R.id.url_menu_barcode).setOnClickListener(this);
	}

	private void dismissMorePopMenu() {
		if (mMorePopupWindow != null) {
			mMorePopupWindow.dismiss();
			mMorePopupWindow = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.url_menu_barcode:
			((HomeActivity) getContext()).startActivityForResult(new Intent(
					getContext(), CaptureActivity.class),
					Constants.REQUEST_GET_QRCODE);
			dismissMorePopMenu();
			break;
		case R.id.titlebar_more_btn:
			showMorePopup();
			break;
		case R.id.titlebar_refresh:
			mTab.refresh();
			break;
		case R.id.titlebar_fav_btn:
			if (!mTab.getWebViewVisible()) {
				((HomeActivity) getContext()).startActivityForResult(
						new Intent(getContext(), FavHisActivity.class),
						Constants.FAV_ACTIVITY_REQUEST_CODE);
				dismissUrlPopMenu();
			} else if (mUrlPopupWindow != null && mUrlPopupWindow.isShowing()) {
				dismissUrlPopMenu();
			} else {
				showUrlPopup();
			}
			break;
		case R.id.titlebar_url_fake:
		case R.id.titlebar_url_real:
			setMode(EDIT_MODE);
			break;
		case R.id.url_menu_add_fav:
			Utils.addToFav(getContext(), mTab.getCurrentTitle(),
					mTab.getCurrentUrl(), mTab.getCurrentFav());
			dismissUrlPopMenu();
			break;
		case R.id.titlebar_search: {
			if (mMode == EDIT_MODE) {
				String content = mUrlEditText.getText().toString();
				if (!TextUtils.isEmpty(content)) {
					String url = Utils.smartUrlFilter(content, false);
					if (url == null) {
						mTab.loadUrl(Constants.SEARCH_URL + content, true);
					} else {
						mTab.loadUrl(url, true);
					}
				} else {
					Utils.showMessage(
							getContext(),
							getContext().getString(
									R.string.titlebar_url_edit_hint));
				}
			} else {
				Utils.showMessage(getContext(),
						getContext().getString(R.string.titlebar_url_edit_hint));
			}

			mInputMethodManager.hideSoftInputFromWindow(
					mUrlEditText.getWindowToken(), 0);
		}
			break;
		case R.id.url_menu_open_fav_his:
			((Activity) getContext()).startActivityForResult(new Intent(
					getContext(), FavHisActivity.class),
					Constants.FAV_ACTIVITY_REQUEST_CODE);
			dismissUrlPopMenu();
			break;
		case R.id.url_menu_add_navi:
			Utils.addToNavi(getContext(), mTab.getCurrentTitle(),
					mTab.getCurrentUrl(), null);
			dismissUrlPopMenu();
			break;
		case R.id.url_menu_send_desk:
			Utils.createShortCut(getContext(), mTab.getCurrentTitle(),
					mTab.getCurrentUrl());
			dismissUrlPopMenu();
			Toast.makeText(getContext(), R.string.send_desk_done,
					Toast.LENGTH_SHORT).show();
			break;
		}
	}
}
