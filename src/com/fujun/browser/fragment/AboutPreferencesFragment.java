
package com.fujun.browser.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fujun.browser.utils.Utils;
import com.kukuai.daohang.R;

public class AboutPreferencesFragment extends PreferenceFragment {

	private TextView mVersionTextView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.about_layout, null);
		mVersionTextView = (TextView) view.findViewById(R.id.about_version);
		if (Utils.getVersionName(getActivity()) != null) {
			mVersionTextView.setText(getString(R.string.version)
					+ Utils.getVersionName(getActivity()));
		} else {
			mVersionTextView.setText("unKnown");
		}
		return view;
	}
}
