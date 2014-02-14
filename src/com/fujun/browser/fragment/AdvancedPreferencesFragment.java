
package com.fujun.browser.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.fujun.browser.SettingManager;
import com.fujun.browser.utils.Utils;
import com.fujun.browser.view.YesNoPreference;
import com.fujun.browser.view.YesNoPreference.OnDialogPreferencePositiveClickedListener;
import com.kukuai.daohang.R;

public class AdvancedPreferencesFragment extends PreferenceFragment implements
		OnPreferenceChangeListener, OnDialogPreferencePositiveClickedListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.advanced_preferences);

		Preference e = findPreference(SettingManager.PREF_DEFAULT_ZOOM);
		e.setOnPreferenceChangeListener(this);
		e.setSummary(getVisualDefaultZoomName(
				getPreferenceScreen().getSharedPreferences()
						.getString(SettingManager.PREF_DEFAULT_ZOOM, null)));

		e = findPreference(SettingManager.PREF_DEFAULT_TEXT_ENCODING);
		e.setOnPreferenceChangeListener(this);

		e = findPreference(SettingManager.PREF_PLUGIN_STATE);
		e.setOnPreferenceChangeListener(this);
		updateListPreferenceSummary((ListPreference) e);

		((YesNoPreference) findPreference(SettingManager.PREF_RESET_DEFAULT))
				.setOnDialogPreferencePositiveClickedListener(this);
	}

	private void updateListPreferenceSummary(ListPreference e) {
		e.setSummary(e.getEntry());
	}

	private CharSequence getVisualDefaultZoomName(String enumName) {
		Resources res = getActivity().getResources();
		CharSequence[] visualNames = res.getTextArray(R.array.pref_default_zoom_choices);
		CharSequence[] enumNames = res.getTextArray(R.array.pref_default_zoom_values);

		// Sanity check
		if (visualNames.length != enumNames.length) {
			return "";
		}

		int length = enumNames.length;
		for (int i = 0; i < length; i++) {
			if (enumNames[i].equals(enumName)) {
				return visualNames[i];
			}
		}

		return "";
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object objValue) {
		if (getActivity() == null) {
			// We aren't attached, so don't accept preferences changes from the
			// invisible UI.
			Log.w("PageContentPreferencesFragment",
					"onPreferenceChange called from detached fragment!");
			return false;
		}

		if (pref.getKey().equals(SettingManager.PREF_DEFAULT_ZOOM)) {
			pref.setSummary(getVisualDefaultZoomName((String) objValue));
			return true;
		} else if (pref.getKey().equals(SettingManager.PREF_DEFAULT_TEXT_ENCODING)) {
			pref.setSummary((String) objValue);
			return true;
		} else if (pref.getKey().equals(SettingManager.PREF_PLUGIN_STATE)) {
			ListPreference lp = (ListPreference) pref;
			lp.setValue((String) objValue);
			updateListPreferenceSummary(lp);
			return false;
		}
		return false;
	}

	@Override
	public void onDialogPreferencePositiveClick(Preference preference) {
		if (SettingManager.PREF_RESET_DEFAULT.equals(preference.getKey())) {
			SettingManager.getSettingManager().resetDefault();
			Utils.showMessage(getActivity(), getString(R.string.pref_default_reset));
		}
	}
}
