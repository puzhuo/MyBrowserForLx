
package com.fujun.browser.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.fujun.browser.fragment.FavFragment;
import com.fujun.browser.fragment.HisFragment;
import com.fujun.browser.utils.Utils;
import com.kukuai.daohang.R;

public class FavHisActivity extends BaseFragmentActivity implements OnClickListener {

	public interface FavHisEmptyListener {
		public void onFavHisEmpty(boolean isFav, boolean isEmpty);
	}

	private static final int FAV_PAGE_INDEX = 0;
	private static final int HIS_PAGE_INDEX = 1;

	private static final int CLEAR_HIS_DIALOG_ID = 0;

	private TextView mFavTitleTextView;
	private TextView mHisTitleTextView;
	private TextView mMenuLeftTextView;
	private TextView mMenuRightTextView;
	private ViewPager mViewPager;
	private FavPagerAdapter mAdapter;
	private FavFragment mFavFragment;
	private HisFragment mHisFragment;

	private int mCurrentItem;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fav_his_layout);

		mFavFragment = new FavFragment();
		mFavFragment.setOnFavHisEmptyListener(mFavHisEmptyListener);
		mHisFragment = new HisFragment();
		mHisFragment.setOnFavHisEmptyListener(mFavHisEmptyListener);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mAdapter = new FavPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				mCurrentItem = arg0;
				if (arg0 == FAV_PAGE_INDEX) {
					mFavTitleTextView.setSelected(true);
					mHisTitleTextView.setSelected(false);
					mMenuLeftTextView.setText(R.string.fav_his_manage);
					mMenuLeftTextView.setEnabled(!mFavFragment.isEmpty());
				} else if (arg0 == HIS_PAGE_INDEX) {
					mFavTitleTextView.setSelected(false);
					mHisTitleTextView.setSelected(true);
					mMenuLeftTextView.setText(R.string.clear_text);
					mMenuLeftTextView.setEnabled(!mHisFragment.isEmpty());
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		mFavTitleTextView = (TextView) findViewById(R.id.fav_text);
		mHisTitleTextView = (TextView) findViewById(R.id.his_text);
		mMenuLeftTextView = (TextView) findViewById(R.id.fav_his_manage);
		mMenuLeftTextView.setEnabled(!mFavFragment.isEmpty());
		mMenuRightTextView = (TextView) findViewById(R.id.fav_his_back);
		mFavTitleTextView.setSelected(true);

		mFavTitleTextView.setOnClickListener(this);
		mHisTitleTextView.setOnClickListener(this);
		mMenuLeftTextView.setOnClickListener(this);
		mMenuRightTextView.setOnClickListener(this);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
			case CLEAR_HIS_DIALOG_ID:
				builder.setTitle(R.string.clear_his_dialog_title)
						.setMessage(R.string.clear_his_dialog_message)
						.setPositiveButton(R.string.dialog_positive_queding,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Utils.clearVisitHistory(FavHisActivity.this);
									}
								})
						.setNegativeButton(R.string.dialog_negative_cancel, null);
				break;
		}
		return builder.create();
	}

	private class FavPagerAdapter extends FragmentPagerAdapter {

		public FavPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		private static final int PAGE_COUNT = 2;

		@Override
		public Fragment getItem(int arg0) {
			switch (arg0) {
				case FAV_PAGE_INDEX:
					return mFavFragment;
				case HIS_PAGE_INDEX:
					return mHisFragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			return PAGE_COUNT;
		}

	}

	private FavHisEmptyListener mFavHisEmptyListener = new FavHisEmptyListener() {

		@Override
		public void onFavHisEmpty(boolean isFav, boolean isEmpty) {
			if ((isFav && (mCurrentItem == FAV_PAGE_INDEX))
					|| (!isFav && (mCurrentItem == HIS_PAGE_INDEX))) {
				mMenuLeftTextView.setEnabled(!isEmpty);
			}
		}
	};

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.fav_text:
				if (mCurrentItem != FAV_PAGE_INDEX) {
					mViewPager.setCurrentItem(FAV_PAGE_INDEX);
					mCurrentItem = FAV_PAGE_INDEX;
				}
				break;
			case R.id.his_text:
				if (mCurrentItem != HIS_PAGE_INDEX) {
					mViewPager.setCurrentItem(HIS_PAGE_INDEX);
					mCurrentItem = HIS_PAGE_INDEX;
				}
				break;
			case R.id.fav_his_back:
				finish();
				break;
			case R.id.fav_his_manage:
				if (mCurrentItem == FAV_PAGE_INDEX) {
					startActivity(new Intent(this, FavManageActivity.class));
				} else if (mCurrentItem == HIS_PAGE_INDEX) {
					showDialog(CLEAR_HIS_DIALOG_ID);
				}
				break;
		}
	}
}
