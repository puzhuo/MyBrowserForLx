
package com.fujun.browser.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.fujun.browser.TabManager;
import com.fujun.browser.utils.Utils;
import com.fujun.browser.view.YesNoPreference;
import com.fujun.browser.view.YesNoPreference.OnDialogPreferencePositiveClickedListener;
import com.kukuai.daohang.R;

public class SecurityPreferencesFragment extends PreferenceFragment implements
		OnDialogPreferencePositiveClickedListener {

	private static final String CLEAR_CACHE_KEY = "clean_cache";
	private static final String CLEAR_COOKIE_KEY = "clean_cookie";
	private static final String CLEAR_PASSWD_KEY = "clear_passwd";
	private static final String CLEAR_FORM_KEY = "clear_form";
	private static final String UNENABLE_LOCATION_KEY = "unenable_location";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.privacy_security_preferences);
		((YesNoPreference) findPreference(CLEAR_CACHE_KEY))
				.setOnDialogPreferencePositiveClickedListener(this);
		((YesNoPreference) findPreference(CLEAR_COOKIE_KEY))
				.setOnDialogPreferencePositiveClickedListener(this);
		((YesNoPreference) findPreference(CLEAR_PASSWD_KEY))
				.setOnDialogPreferencePositiveClickedListener(this);
		((YesNoPreference) findPreference(CLEAR_FORM_KEY))
				.setOnDialogPreferencePositiveClickedListener(this);
		((YesNoPreference) findPreference(UNENABLE_LOCATION_KEY))
				.setOnDialogPreferencePositiveClickedListener(this);
	}

	@Override
	public void onDialogPreferencePositiveClick(Preference preference) {
		if (CLEAR_CACHE_KEY.equals(preference.getKey())) {
			TabManager.getInstance(getActivity(), null, null).clearCache();
			Utils.showMessage(getActivity(), getString(R.string.cache_cleaned));
		} else if (CLEAR_COOKIE_KEY.equals(preference.getKey())) {
			TabManager.getInstance(getActivity(), null, null).clearCookie();
			Utils.showMessage(getActivity(), getString(R.string.cookie_cleaned));
		} else if (CLEAR_PASSWD_KEY.equals(preference.getKey())) {
			TabManager.getInstance(getActivity(), null, null).clearPasswd();
			Utils.showMessage(getActivity(), getString(R.string.passwd_cleaned));
		} else if (CLEAR_FORM_KEY.equals(preference.getKey())) {
			TabManager.getInstance(getActivity(), null, null).clearForm();
			Utils.showMessage(getActivity(), getString(R.string.form_cleaned));
		} else if (UNENABLE_LOCATION_KEY.equals(preference.getKey())) {
			TabManager.getInstance(getActivity(), null, null).clearLocationAccess(getActivity());
			Utils.showMessage(getActivity(), getString(R.string.location_cleaned));
		}
	}
}
