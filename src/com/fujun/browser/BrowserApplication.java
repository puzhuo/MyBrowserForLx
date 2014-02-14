
package com.fujun.browser;

import android.app.Application;
import android.support.v4.app.FragmentManager;

public class BrowserApplication extends Application {

	private static FragmentManager mFragmentManager;

	public static void setFragmentManager(FragmentManager manager) {
		mFragmentManager = manager;
	}

	public static FragmentManager getFragmentManager() {
		return mFragmentManager;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		SettingManager.initiant(this);
	}
}
