
package com.fujun.browser.activity;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.ScaleAnimation;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fujun.browser.BrowserApplication;
import com.fujun.browser.SettingManager;
import com.fujun.browser.Tab;
import com.fujun.browser.TabManager;
import com.fujun.browser.TabManager.onTabsChangedListener;
import com.fujun.browser.constants.Constants;
import com.fujun.browser.fragment.IndexFavFragment;
import com.fujun.browser.fragment.IndexFavFragment.onFavClickListener;
import com.fujun.browser.fragment.IndexFavFragment.onFavLongClickListener;
import com.fujun.browser.fragment.IndexNaviFragmentNew;
import com.fujun.browser.fragment.IndexNaviFragmentNew.NaviNewClickListener;
import com.fujun.browser.model.FavItem;
import com.fujun.browser.provider.BrowserProvider;
import com.fujun.browser.update.CheckTask;
import com.fujun.browser.utils.Utils;
import com.fujun.browser.view.FindDialog;
import com.fujun.browser.view.MyViewPager;
import com.fujun.browser.view.MyViewPager.onMoveToWebViewListener;
import com.fujun.browser.view.SlidingWebView.OnWebViewMoveListener;
import com.fujun.browser.view.TitleBar;
import com.kukuai.daohang.R;

public class HomeActivity extends BaseFragmentActivity implements
		onMoveToWebViewListener, onFavClickListener, OnClickListener, OnWebViewMoveListener,
		NaviNewClickListener {

	private MyViewPager mViewPager;
	private BrowserPagerAdapter mAdapter;
	private LinearLayout mIndicatorLayout;
	private FrameLayout mHomeContent;
	private static FrameLayout mHomeRoot;
	private TabManager mTabManager;
	private Tab mCurrentTab;
	private int mCurrentTabId;

	private ArrayList<Tab> mTabs;
	private IndexFavFragment mIndexFavFragment;
	private IndexNaviFragmentNew mIndexNaviFragment;

	private LinearLayout mMenubarLayout;
	private TextView mTabTextView;
	private ImageView mMenubarBack;
	private ImageView mMenubarStop;
	private ImageView mMenubarForward;
	private ImageView mMenubarHome;

	private PopupWindow mTabPopupWindow;
	private ListView mTabCenterListView;
	private TabCenterAdapter mTabCenterAdapter;

	private PopupWindow mMenuPopupWindow;
	private TextView mMenuPopAddFav;
	private TextView mMenuPopOpenFav;
	private TextView mMenuPopRefresh;
	private TextView mMenuPopDownload;
	private TextView mMenuPopQuit;
	private TextView mMenuPopNoImage;
	private TextView mMenuPopFindOnPage;
	private TextView mMenuPopSetting;

	private FindDialog mFindDialog;

	private int mBackClickCount;
	private static final long BACK_GAP_TIME = 2500;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.home_layout);
		BrowserApplication.setFragmentManager(getSupportFragmentManager());

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			File file = new File(Environment.getExternalStorageDirectory(),
					Constants.DOWNLOAD_FOLDER);
			if (!file.exists()) {
				file.mkdirs();
			}
		}

		final SharedPreferences preferences = getSharedPreferences(Constants.PREF_NAME,
				MODE_PRIVATE);
		if (preferences.getBoolean(Constants.SHARED_CREATE_ICON + Utils.getVersionName(this), true)) {
			Utils.createShortCut(getApplicationContext());
			preferences.edit()
					.putBoolean(Constants.SHARED_CREATE_ICON + Utils.getVersionName(this), false)
					.commit();
		}
		long lastTime = preferences.getLong(Constants.PREF_UPDATE_HTML_LAST_TIME, 0);
		if (lastTime == 0 || (System.currentTimeMillis() - lastTime) > Constants.UPDATE_HTML_GAP) {
			Utils.updateHtml();
			preferences.edit().putLong(Constants.PREF_UPDATE_HTML_LAST_TIME,
					System.currentTimeMillis()).commit();
		}
		if (Constants.CHECK_UPDATE) {
			long lastTimeUpdate = preferences.getLong(
					Constants.SHARED_LAST_CHECK_TIME, 0);
			if (lastTimeUpdate + Constants.ALL_DAY_IN_MILLIONS <= System
					.currentTimeMillis()) {
				new Thread() {
					@Override
					public void run() {
						CheckTask
								.start(HomeActivity.this,
										Constants.CHECK_UPDATE_ID,
										getSupportFragmentManager());
					}
				}.start();
			}
		}

		WebIconDatabase.getInstance().open(
				getDir("icons", MODE_PRIVATE).getPath());
		init();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mCurrentTab != null) {
			mCurrentTab.clearCache();
		}
		if (mHomeContent != null) {
			mHomeContent.removeAllViews();
		}
		mTabManager.release();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void createNewTab(String url, boolean showMessage) {
		mCurrentTab = mTabManager.createNewTab(this);
		mCurrentTabId = mTabManager.getActiveTabId();
		mCurrentTab.loadUrl(Utils.smartUrlFilter(url, false), showMessage);
		onWebViewMove();
	}

	private void init() {

		mHomeRoot = (FrameLayout) findViewById(android.R.id.content);
		mHomeContent = (FrameLayout) findViewById(R.id.home_content);

		mViewPager = (MyViewPager) findViewById(R.id.pager);
		mAdapter = new BrowserPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnMoveToWebViewListener(this);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				if (mIndicatorLayout != null) {
					mIndicatorLayout.setVisibility(View.VISIBLE);
					ImageView child = null;
					int count = mIndicatorLayout.getChildCount();
					for (int i = 0; i < count; i++) {
						child = (ImageView) mIndicatorLayout.getChildAt(i);
						if (i == position) {
							child.setBackgroundColor(getResources()
									.getColor(R.color.blue));
							child.setImageResource(R.drawable.navigation_title_indicator_0
									+ i);
						} else {
							child.setBackgroundColor(Color.TRANSPARENT);
							child.setImageResource(R.drawable.navigation_slider_off);
						}
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		mIndicatorLayout = (LinearLayout) findViewById(R.id.navi_indicator);
		// webviewfragment does not need a indicator
		int count = mViewPager.getAdapter().getCount();
		if (count < 1) {
			mIndicatorLayout.setVisibility(View.GONE);
			return;
		}
		ImageView imageView = null;
		for (int i = 0; i < count; i++) {
			imageView = new ImageView(this);
			imageView.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			if (i == 0) {
				imageView.setBackgroundColor(getResources().getColor(
						R.color.blue));
				imageView
						.setImageResource(R.drawable.navigation_title_indicator_0);
			} else {
				imageView.setBackgroundColor(Color.TRANSPARENT);
				imageView.setImageResource(R.drawable.navigation_slider_off);
			}
			mIndicatorLayout.addView(imageView);
		}

		mMenubarLayout = (LinearLayout) findViewById(R.id.menubar);
		mTabTextView = (TextView) findViewById(R.id.menubar_tabcenter_text);
		mTabTextView.setOnClickListener(this);
		mMenubarBack = (ImageView) findViewById(R.id.menubar_back);
		mMenubarBack.setOnClickListener(this);
		mMenubarBack.setEnabled(false);
		mMenubarForward = (ImageView) findViewById(R.id.menubar_forward);
		mMenubarForward.setOnClickListener(this);
		mMenubarForward.setEnabled(false);
		mMenubarHome = (ImageView) findViewById(R.id.menubar_home);
		mMenubarHome.setOnClickListener(this);
		mMenubarStop = (ImageView) findViewById(R.id.menubar_stop);
		mMenubarStop.setOnClickListener(this);

		mTabManager = TabManager.getInstance(this, mHomeContent,
				(ViewGroup) findViewById(R.id.home_root));
		mTabManager.setOnTabsChangedListener(mTabsChangedListener);
		mTabManager.setOnWebViewMoveListener(this);
		String url = getIntent().getDataString();
		createNewTab(url, false);
		if (!TextUtils.isEmpty(url)) {
			mCurrentTab.show();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		String url = intent.getDataString();
		createNewTab(url, true);
		if (!TextUtils.isEmpty(url)) {
			mCurrentTab.show();
		}
	}

	@Override
	public void onBackPressed() {
		if (mCurrentTab.getTitleBar().getMode() == TitleBar.EDIT_MODE) {
			mCurrentTab.getTitleBar().setPreviousMode();
			return;
		}
		if (mCurrentTab != null && mCurrentTab.getWebViewVisible()) {
			if (mCurrentTab.isVideoOn()) {
				mCurrentTab.hideVideoView();
			} else {
				mCurrentTab.goBack();
			}
			return;
		}
		if (mBackClickCount == 0) {
			mBackClickCount++;
			mHandler.sendEmptyMessageDelayed(0, BACK_GAP_TIME);
			Toast.makeText(this, R.string.back_notice_text,
					Toast.LENGTH_SHORT).show();
		} else {
			finish();
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (mMenuPopupWindow == null || !mMenuPopupWindow.isShowing()) {
				showMenuPopWindow();
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mBackClickCount--;
		};
	};

	private onFavLongClickListener mFavLongClickListener = new onFavLongClickListener() {

		@Override
		public void onFavLongClick(View v) {
			final FavItem favItem = (FavItem) v.getTag();
			PopupMenu popup = new PopupMenu(HomeActivity.this, v);
			MenuInflater inflater = popup.getMenuInflater();
			inflater.inflate(R.menu.navi_menu, popup.getMenu());
			popup.show();
			popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

				@Override
				public boolean onMenuItemClick(MenuItem item) {
					if (item.getItemId() == R.id.action_delete) {
						new AsyncTask<Void, Void, Void>() {

							@Override
							protected Void doInBackground(Void... params) {
								getContentResolver().delete(BrowserProvider.TABLE_INDEX_NAVI_URI,
										BrowserProvider.TABLE_NAVI_URL + "=?", new String[] {
											favItem.url
										});

								return null;
							}
						}.execute();
						return true;
					} else if (item.getItemId() == R.id.action_edit) {
						Utils.showNaviEditDialog(HomeActivity.this, favItem);
					}
					return false;
				}
			});
		}
	};

	private class BrowserPagerAdapter extends FragmentPagerAdapter {

		// private static final String TAG = "BrowserPagerAdapter";
		private static final int INDEX_PAGE_COUNT = 2;

		public BrowserPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 1:
					mIndexFavFragment = new IndexFavFragment();
					mIndexFavFragment.setOnFavClickListener(HomeActivity.this);
					mIndexFavFragment.setOnFavLongClickListener(mFavLongClickListener);
					return mIndexFavFragment;
				case 0:
					mIndexNaviFragment = new IndexNaviFragmentNew();
					mIndexNaviFragment.setNaviNewClickListener(HomeActivity.this);
					return mIndexNaviFragment;
				default:
					throw new IllegalArgumentException(
							"BrowserPagerAdapter illegal position");
			}
		}

		@Override
		public int getCount() {
			return INDEX_PAGE_COUNT;
		}

	}

	@Override
	public void onMoveToWebView() {
		// if (mCurrentTab != null) {
		// mCurrentTab.moveToWebView();
		// }
	}

	@Override
	public void onFavClick(View v) {
		FavItem item = (FavItem) v.getTag();
		if (mCurrentTab != null) {
			mCurrentTab.setClearHistory(true);
			String url = Utils.smartUrlFilter(item.url, false);
			mCurrentTab.loadUrl(url, true);
		}
	}

	private void showMenuPopWindow() {
		View view = getLayoutInflater().inflate(R.layout.menu_pop_layout,
				null);
		mMenuPopupWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		mMenuPopupWindow.setFocusable(true);
		mMenuPopupWindow.setBackgroundDrawable(new ColorDrawable(
				Color.TRANSPARENT));
		mMenuPopupWindow.getContentView().setFocusableInTouchMode(true);
		mMenuPopupWindow.getContentView().setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
					dismissMenuPopWindow();
					return true;
				}
				return false;
			}
		});
		mMenuPopupWindow.showAtLocation(mHomeContent, Gravity.BOTTOM, 0,
				mMenubarLayout.getMeasuredHeight());

		mMenuPopAddFav = (TextView) view.findViewById(R.id.menu_pop_add_fav);
		mMenuPopOpenFav = (TextView) view.findViewById(R.id.menu_pop_open_fav);
		mMenuPopRefresh = (TextView) view.findViewById(R.id.menu_pop_refresh);
		mMenuPopDownload = (TextView) view.findViewById(R.id.menu_pop_download);
		mMenuPopQuit = (TextView) view.findViewById(R.id.menu_pop_quit);
		mMenuPopFindOnPage = (TextView) view.findViewById(R.id.menu_pop_find_on_page);
		mMenuPopSetting = (TextView) view.findViewById(R.id.menu_pop_setting);
		mMenuPopNoImage = (TextView) view.findViewById(R.id.menu_pop_no_image);
		int res = SettingManager.getSettingManager().getIfWithImage() ? R.string.menu_pop_with_image
				: R.string.menu_pop_without_image;
		mMenuPopNoImage.setText(res);
		mMenuPopAddFav.setOnClickListener(mMenuPopClickListener);
		mMenuPopOpenFav.setOnClickListener(mMenuPopClickListener);
		mMenuPopRefresh.setOnClickListener(mMenuPopClickListener);
		mMenuPopDownload.setOnClickListener(mMenuPopClickListener);
		mMenuPopQuit.setOnClickListener(mMenuPopClickListener);
		mMenuPopNoImage.setOnClickListener(mMenuPopClickListener);
		mMenuPopFindOnPage.setOnClickListener(mMenuPopClickListener);
		mMenuPopSetting.setOnClickListener(mMenuPopClickListener);
		if (!mCurrentTab.getWebViewVisible()) {
			mMenuPopAddFav.setEnabled(false);
		}
	}

	private OnClickListener mMenuPopClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.menu_pop_add_fav:
					Utils.addToFav(HomeActivity.this, mCurrentTab.getCurrentTitle(),
							mCurrentTab.getCurrentUrl(), mCurrentTab.getCurrentFav());
					break;
				case R.id.menu_pop_open_fav:
					startActivityForResult(new Intent(HomeActivity.this, FavHisActivity.class),
							Constants.FAV_ACTIVITY_REQUEST_CODE);
					break;
				case R.id.menu_pop_refresh:
					if (mCurrentTab.getWebViewVisible()) {
						mCurrentTab.refresh();
					} else if (mIndexNaviFragment != null) {
						mIndexNaviFragment.refresh();
					}
					break;
				case R.id.menu_pop_download:
					startActivity(new Intent(HomeActivity.this, DownloadManagerActivity.class));
					break;
				case R.id.menu_pop_quit:
					finish();
					break;
				case R.id.menu_pop_no_image:
					SettingManager settingManager = SettingManager
							.getSettingManager();
					boolean withImage = settingManager.getIfWithImage();
					settingManager.setIfWithImage(!withImage);
					int res = withImage ? R.string.menu_pop_without_image
							: R.string.menu_pop_with_image;
					mMenuPopNoImage.setText(res);
					res = withImage ? R.string.switched_to_with_image
							: R.string.switched_to_no_image;
					Utils.showMessage(getApplicationContext(), getString(res));
					break;
				case R.id.menu_pop_find_on_page:
					findOnPage();
					break;
				case R.id.menu_pop_setting:
					startActivity(new Intent(HomeActivity.this, SettingActivity.class));
					break;
				default:
					break;
			}
			dismissMenuPopWindow();
		}
	};

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void findOnPage() {
		WebView webView = null;
		if (mCurrentTab.getWebViewVisible()) {
			webView = mCurrentTab.getWebView();
		} else if (mIndexNaviFragment != null) {
			webView = mIndexNaviFragment.getWebView();
		}
		if (webView != null) {
			if (Build.VERSION.SDK_INT >= 21) {
				webView.showFindDialog(null, true);
			} else {
				// View view =
				// LayoutInflater.from(this).inflate(R.layout.find_on_page_view,
				// null);
				// final EditText editText = (EditText)
				// view.findViewById(R.id.findtext_edit);
				// new AlertDialog.Builder(this).setView(view)
				// .setPositiveButton(R.string.ok, new
				// DialogInterface.OnClickListener() {
				//
				// @Override
				// public void onClick(DialogInterface dialog, int which) {
				// int occurance = mCurrentTab.getWebView().findAll(
				// editText.getText().toString());
				// try {
				// Method m = WebView.class.getMethod("setFindIsUp",
				// Boolean.TYPE);
				// m.invoke(mCurrentTab.getWebView(), true);
				// } catch (Exception ignored) {
				// }
				// if (occurance == 0) {
				// Toast.makeText(HomeActivity.this,
				// R.string.find_nothing_match,
				// Toast.LENGTH_SHORT).show();
				// } else {
				// Toast.makeText(
				// HomeActivity.this,
				// String.format(getString(R.string.find_sth_match),
				// occurance),
				// Toast.LENGTH_SHORT).show();
				// }
				// }
				// }).setNegativeButton(R.string.cancel, null).show();
				if (mFindDialog == null) {
					mFindDialog = new FindDialog(this);
				}
				mFindDialog.setWebView(webView);
				mFindDialog.show();
			}
		}
	}

	private void showTabCenter() {
		View view = getLayoutInflater().inflate(R.layout.tab_center_layout,
				null);
		mTabPopupWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		mTabPopupWindow.setFocusable(true);
		mTabPopupWindow.setBackgroundDrawable(new ColorDrawable(
				Color.TRANSPARENT));
		mTabPopupWindow.showAtLocation(mHomeContent, Gravity.BOTTOM, 0,
				mMenubarLayout.getMeasuredHeight());
		mTabCenterListView = (ListView) view.findViewById(R.id.tab_center_list);
		mTabCenterListView.setItemsCanFocus(true);
		mTabCenterAdapter = new TabCenterAdapter();
		mTabCenterListView.setAdapter(mTabCenterAdapter);

		RelativeLayout newTab = (RelativeLayout) view
				.findViewById(R.id.new_tab);
		// create a new tab
		newTab.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// dismiss TabPopupWindow
				dismissTabPopupWindow();
				if (mTabManager.getTabCount() == Constants.MAX_TAB_COUNT) {
					Toast.makeText(HomeActivity.this,
							R.string.tab_center_has_reached_max_count,
							Toast.LENGTH_SHORT).show();
					return;
				}
				// add a new tab to tabs
				createNewTab(null, false);
			}
		});
	}

	private void dismissTabPopupWindow() {
		if (mTabPopupWindow != null && mTabPopupWindow.isShowing()) {
			mTabPopupWindow.dismiss();
			mTabPopupWindow = null;
			mTabCenterListView = null;
			mTabCenterAdapter = null;
		}
	}

	private void dismissMenuPopWindow() {
		if (mMenuPopupWindow != null && mMenuPopupWindow.isShowing()) {
			mMenuPopupWindow.dismiss();
			mMenuPopupWindow = null;
			mMenuPopAddFav = null;
			mMenuPopOpenFav = null;
			mMenuPopRefresh = null;
			mMenuPopDownload = null;
			mMenuPopQuit = null;
			mMenuPopNoImage = null;
			mMenuPopFindOnPage = null;
			mMenuPopSetting = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.menubar_tabcenter_text:
				if (mTabPopupWindow != null && mTabPopupWindow.isShowing()) {
					mTabPopupWindow.dismiss();
					mTabPopupWindow = null;
				} else {
					showTabCenter();
				}
				break;
			case R.id.menubar_back:
				if (mCurrentTab != null && mCurrentTab.canGoBack()) {
					mCurrentTab.goBack();
				}
				break;
			case R.id.menubar_forward:
				if (mCurrentTab != null && mCurrentTab.canGoForward()) {
					mCurrentTab.goForward();
				}
				break;
			case R.id.menubar_stop:
				if (mCurrentTab != null) {
					mCurrentTab.stopLoading();
				}
				break;
			case R.id.menubar_home:
				if (mCurrentTab != null && mCurrentTab.getWebViewVisible()) {
					mCurrentTab.setWebViewVisible(false);
					mViewPager.setCurrentItem(0);
				}
				break;
			case R.id.menubar_toggle:
				showMenuPopWindow();
				break;
		}
	}

	private class TabCenterHolder {
		public TextView titleTextView;
		public TextView urlTextView;
		public ImageView iconImageView;
		public ImageView closeImageView;
	}

	private class TabCenterAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mTabs == null ? 0 : mTabs.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			TabCenterHolder holder = null;
			final int pos = position;
			if (convertView == null) {
				view = getLayoutInflater().inflate(R.layout.tab_item_layout,
						null);
				holder = new TabCenterHolder();
				holder.titleTextView = (TextView) view
						.findViewById(R.id.tab_item_title);
				holder.urlTextView = (TextView) view
						.findViewById(R.id.tab_item_url);
				holder.iconImageView = (ImageView) view
						.findViewById(R.id.tab_item_icon);
				holder.closeImageView = (ImageView) view
						.findViewById(R.id.tab_item_close);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (TabCenterHolder) view.getTag();
			}

			Tab tab = mTabs.get(position);
			holder.titleTextView.setText((position + 1) + ". "
					+ tab.getCurrentTitle());

			String url = tab.getCurrentUrl();
			if (url != null) {
				holder.urlTextView.setText(url);
				holder.urlTextView.setVisibility(View.VISIBLE);
			} else {
				holder.urlTextView.setVisibility(View.GONE);
			}

			holder.iconImageView.setImageResource(R.drawable.ic_launcher);

			// delete tab
			holder.closeImageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mTabManager.deleteTab(pos);
					if (mTabManager.getTabCount() == 0) {
						// add a new tab to tabs
						createNewTab(null, false);
						// dismiss TabPopupWindow
						dismissTabPopupWindow();
					}
					onLoadingStateChanged(mCurrentTab.getLoading());
				}
			});

			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dismissTabPopupWindow();
					if (mCurrentTabId != pos) {
						mTabManager.setActiveTabId(pos);
						mCurrentTabId = pos;
						mCurrentTab = mTabManager.getActiveTab();
						mTabManager.switchTab();
					}
				}
			});
			return view;
		}
	}

	private onTabsChangedListener mTabsChangedListener = new onTabsChangedListener() {

		@Override
		public void onTabsCountChanged() {
			mTabs = mTabManager.getTabs();
			mCurrentTabId = mTabManager.getActiveTabId();
			if (mTabCenterAdapter != null) {
				mTabCenterAdapter.notifyDataSetChanged();
			}
			if (mTabTextView != null) {
				mTabTextView.setText("" + mTabManager.getTabCount());
				if (mTabManager.getTabCount() != 1) {
					ScaleAnimation animation = new ScaleAnimation(-1.0f, 1.0f,
							2.0f, 1.0f, 0.3f, 0.7f);
					animation.setDuration(500);
					mTabTextView.startAnimation(animation);
				}
			}
		}
	};

	public static class MyWebChromeClient extends WebChromeClient implements
			OnCompletionListener, OnErrorListener {

		static final FrameLayout.LayoutParams COVER_SCREEN_GRAVITY_CENTER = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);

		private Context mContext;
		private View mCustomView;
		private View mVideoProgressView;
		private FrameLayout mCustomViewContainer;
		private CustomViewCallback mCustomViewCallback;
		private Tab mTab;
		private static boolean mIsVideoOn;
		private TitleBar mTitleBar;
		private boolean mShown;

		public MyWebChromeClient(Context context, Tab tab) {
			mContext = context;
			mTab = tab;
		}

		public boolean isVideoOn() {
			return mIsVideoOn;
		}

		public void setTitleBar(TitleBar titleBar) {
			mTitleBar = titleBar;
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			mTab.setCurrentTitle(title);
			mTitleBar.setTitle(title);
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			mTab.setCurrentLoadProgress(newProgress);
			mTitleBar.setProgress(newProgress);
			if (!mShown && newProgress > Utils.getLimitedProgress(mContext)
					&& mTab.inForeground()
					&& !mTab.getWebViewVisible()) {
				mTab.show();
			}
			if (newProgress > 10 && mTab.getClearHistory()) {
				mTab.clearHistory();
				mTab.setClearHistory(false);
			}
			if (newProgress >= 100) {
				mShown = false;
			}
		}

		public boolean getShown() {
			return mShown;
		}

		public void setShown(boolean shown) {
			mShown = shown;
		}

		/********************************** things about video ******************************************/

		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			if (mCustomView != null) {
				callback.onCustomViewHidden();
				return;
			}
			if (mHomeRoot != null) {
				mCustomViewContainer = new FrameLayout(mContext);
				mCustomViewContainer.setBackgroundResource(R.color.black);
				mCustomView = view;
				mCustomViewContainer.addView(mCustomView,
						COVER_SCREEN_GRAVITY_CENTER);
				mCustomViewCallback = callback;

				mHomeRoot.addView(mCustomViewContainer,
						COVER_SCREEN_GRAVITY_CENTER);
				mCustomViewContainer.setVisibility(View.VISIBLE);
				mIsVideoOn = true;
			}
		}

		@Override
		public View getVideoLoadingProgressView() {
			if (mVideoProgressView == null) {
				LayoutInflater inflater = LayoutInflater.from(mContext);
				mVideoProgressView = inflater.inflate(
						R.layout.video_loading_progress, null);
			}
			return mVideoProgressView;
		}

		@Override
		public void onHideCustomView() {
			if (mCustomView != null && mHomeRoot != null) {
				mCustomView.setVisibility(View.GONE);
				mCustomViewContainer.removeView(mCustomView);
				mCustomViewContainer.setVisibility(View.GONE);
				mHomeRoot.removeView(mCustomViewContainer);
				mCustomView = null;
				mCustomViewCallback.onCustomViewHidden();
				mCustomViewContainer = null;
				mIsVideoOn = false;
			}
		}

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			onHideCustomView();
			Toast.makeText(mContext, mContext.getString(R.string.video_error),
					Toast.LENGTH_SHORT).show();
			return false;
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			mp.stop();
			onHideCustomView();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && data != null) {
			if (requestCode == Constants.FAV_ACTIVITY_REQUEST_CODE) {
				String url = data.getStringExtra(Constants.FAV_RESULT_EXTRA_URL);
				createNewTab(url, true);
			} else if (requestCode == Constants.REQUEST_GET_QRCODE) {
				String url = data == null ? "" : data
						.getStringExtra(Constants.EXTRA_QRCODE);
				Log.e("fujun", "url " + url);
				createNewTab(url, true);
			}
		}
	}

	@Override
	public void onWebViewMove() {
		if (mCurrentTab != null) {
			if (mCurrentTab.canGoBack()) {
				mMenubarBack.setEnabled(true);
			} else {
				mMenubarBack.setEnabled(false);
			}
			if (mCurrentTab.canGoForward()) {
				mMenubarForward.setEnabled(true);
			} else {
				mMenubarForward.setEnabled(false);
			}
		}
	}

	@Override
	public void onNewNaviClick(String url) {
		if (mCurrentTab != null) {
			mCurrentTab.setClearHistory(true);
			mCurrentTab.loadUrl(url, true);
		}
	}

	@Override
	public void onLoadingStateChanged(boolean loading) {
		if (loading) {
			mMenubarForward.setVisibility(View.GONE);
			mMenubarStop.setVisibility(View.VISIBLE);
		} else {
			mMenubarForward.setVisibility(View.VISIBLE);
			mMenubarStop.setVisibility(View.GONE);
		}
		onWebViewMove();
	}

}
