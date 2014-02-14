
package com.fujun.browser.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;

import com.fujun.browser.fragment.DownloadManagerFragment;
import com.kukuai.daohang.R;

public class DownloadManagerActivity extends BaseFragmentActivity {

	private ViewPager mViewPager;
	private DownloadPagerAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.download_manager_layout);

		mViewPager = (ViewPager) findViewById(R.id.download_pager);
		mAdapter = new DownloadPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mAdapter);
	}

	private class DownloadPagerAdapter extends FragmentPagerAdapter {

		public DownloadPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {
			return new DownloadManagerFragment();
		}

		@Override
		public int getCount() {
			return 1;
		}

	}
}
