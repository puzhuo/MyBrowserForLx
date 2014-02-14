
package com.fujun.browser.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fujun.browser.fragment.FavFragment;
import com.fujun.browser.fragment.HisFragment;
import com.fujun.browser.fragment.IndexNaviFragment;
import com.fujun.browser.model.Items;
import com.fujun.browser.model.NaviItem;
import com.fujun.browser.utils.Utils;
import com.fujun.browser.view.NaviItemLayout.onNaviItemClickListener;
import com.kukuai.daohang.R;

public class AddNaviActivity extends BaseFragmentActivity {

	public interface AddNaviItemClickListener {
		public void onAddNaviItemClick(Items items);
	}

	private ImageView mBackImageView;
	private TextView mSubmiTextView;
	private EditText mTitleEditText;
	private EditText mUrlEditText;
	private TextView mSwitchRecom;
	private TextView mSwitchFav;
	private TextView mSwitchHis;

	private ViewPager mViewPager;
	private AddNaviPagerAdapter mAdapter;
	private IndexNaviFragment mIndexNaviFragment;
	private FavFragment mFavFragment;
	private HisFragment mHisFragment;

	private static final int INDEX_SWITCH_RECOM = 0;
	private static final int INDEX_SWITCH_FAV = 1;
	private static final int INDEX_SWITCH_HIS = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_navi_layout);

		mBackImageView = (ImageView) findViewById(R.id.add_navi_back);
		mBackImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mSubmiTextView = (TextView) findViewById(R.id.add_navi_right);
		mSubmiTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String title = mTitleEditText.getText().toString().trim();
				if (TextUtils.isEmpty(title)) {
					Utils.showMessage(AddNaviActivity.this, getString(R.string.enter_name));
					return;
				}
				String url = mUrlEditText.getText().toString().trim();
				if (TextUtils.isEmpty(url)) {
					Utils.showMessage(AddNaviActivity.this, getString(R.string.enter_url));
					return;
				} else if (Utils.smartUrlFilter(url, false) == null) {
					Utils.showMessage(AddNaviActivity.this, getString(R.string.illegal_url));
					return;
				}
				Utils.addToNavi(AddNaviActivity.this, title, url, null);
				finish();
			}
		});
		mTitleEditText = (EditText) findViewById(R.id.add_navi_name);
		mUrlEditText = (EditText) findViewById(R.id.add_navi_url);

		mViewPager = (ViewPager) findViewById(R.id.add_navi_pager);
		mAdapter = new AddNaviPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				switch (arg0) {
					case INDEX_SWITCH_RECOM:
						mSwitchRecom.setSelected(true);
						mSwitchFav.setSelected(false);
						mSwitchHis.setSelected(false);
						break;
					case INDEX_SWITCH_FAV:
						mSwitchRecom.setSelected(false);
						mSwitchFav.setSelected(true);
						mSwitchHis.setSelected(false);
						break;
					case INDEX_SWITCH_HIS:
						mSwitchRecom.setSelected(false);
						mSwitchFav.setSelected(false);
						mSwitchHis.setSelected(true);
						break;
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		mSwitchRecom = (TextView) findViewById(R.id.switch_recommended_website);
		mSwitchRecom.setSelected(true);
		mSwitchRecom.setOnClickListener(mOnSwitchClickListener);
		mSwitchFav = (TextView) findViewById(R.id.switch_favorite);
		mSwitchFav.setSelected(false);
		mSwitchFav.setOnClickListener(mOnSwitchClickListener);
		mSwitchHis = (TextView) findViewById(R.id.switch_history);
		mSwitchHis.setSelected(false);
		mSwitchHis.setOnClickListener(mOnSwitchClickListener);
	}

	private OnClickListener mOnSwitchClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.switch_recommended_website:
					mViewPager.setCurrentItem(INDEX_SWITCH_RECOM);
					break;
				case R.id.switch_favorite:
					mViewPager.setCurrentItem(INDEX_SWITCH_FAV);
					break;
				case R.id.switch_history:
					mViewPager.setCurrentItem(INDEX_SWITCH_HIS);
					break;
			}
		}
	};

	private class AddNaviPagerAdapter extends FragmentPagerAdapter {

		// private static final String TAG = "BrowserPagerAdapter";
		private static final int PAGE_COUNT = 3;

		public AddNaviPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					if (mIndexNaviFragment == null) {
						mIndexNaviFragment = new IndexNaviFragment();
						mIndexNaviFragment.setNaviItemClickListener(mNaviItemClickListener);
					}
					return mIndexNaviFragment;
				case 1:
					if (mFavFragment == null) {
						mFavFragment = new FavFragment();
						mFavFragment.setOnFavItemClickListener(mItemClickListener);
					}
					return mFavFragment;
				case 2:
					if (mHisFragment == null) {
						mHisFragment = new HisFragment();
						mHisFragment.setOnHisItemClickListener(mItemClickListener);
					}
					return mHisFragment;
				default:
					throw new IllegalArgumentException(
							"AddNaviPagerAdapter illegal position");
			}
		}

		@Override
		public int getCount() {
			return PAGE_COUNT;
		}

	}

	private onNaviItemClickListener mNaviItemClickListener = new onNaviItemClickListener() {

		@Override
		public void onNaviItemClick(NaviItem item) {
			mTitleEditText.setText(item.title);
			mUrlEditText.setText(item.url);
		}
	};

	private AddNaviItemClickListener mItemClickListener = new AddNaviItemClickListener() {

		@Override
		public void onAddNaviItemClick(Items item) {
			mTitleEditText.setText(item.title);
			mUrlEditText.setText(item.url);
		}
	};
}
