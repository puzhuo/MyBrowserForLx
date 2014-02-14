
package com.fujun.browser.view;

import android.content.Context;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;

import com.kukuai.daohang.R;

public class YesNoPreference extends DialogPreference {

	public interface OnDialogPreferencePositiveClickedListener {
		public void onDialogPreferencePositiveClick(Preference preference);
	}

	private OnDialogPreferencePositiveClickedListener mClickedListener;

	public YesNoPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public YesNoPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setPositiveButtonText(R.string.ok);
		setNegativeButtonText(R.string.cancel);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult && mClickedListener != null) {
			mClickedListener.onDialogPreferencePositiveClick(this);
		}
	}

	public void setOnDialogPreferencePositiveClickedListener(
			OnDialogPreferencePositiveClickedListener listener) {
		mClickedListener = listener;
	}
}
