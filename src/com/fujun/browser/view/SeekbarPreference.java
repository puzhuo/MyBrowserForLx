
package com.fujun.browser.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.kukuai.daohang.R;

public class SeekbarPreference extends Preference implements OnSeekBarChangeListener {

	private TextView mTitleText;
	private TextView mSummaryText;
	private SeekBar mSeekBar;

	private String mTitle;
	private CharSequence mSummary;
	private int mDefaultValue;
	private int mMaxValue;
	private OnPreferenceChangeListener mChangeListener;

	private static final int DEFAULT_DEFAULT_VALUE = 0;
	private static final int DEFAULT_MAX_VALUE = 100;

	public SeekbarPreference(Context context) {
		super(context);
		setLayoutResource(R.layout.seekbar_preference_layout);
		init(context, null);
	}

	public SeekbarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.seekbar_preference_layout);
		init(context, attrs);
	}

	public SeekbarPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setLayoutResource(R.layout.seekbar_preference_layout);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekbarPreference);
			mTitle = a.getString(R.styleable.SeekbarPreference_title);
			mDefaultValue = a.getInteger(R.styleable.SeekbarPreference_defaultValue,
					DEFAULT_DEFAULT_VALUE);
			mMaxValue = a.getInteger(R.styleable.SeekbarPreference_max, DEFAULT_MAX_VALUE);
			a.recycle();
		} else {
			mDefaultValue = DEFAULT_DEFAULT_VALUE;
			mMaxValue = DEFAULT_MAX_VALUE;
		}
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mTitleText = (TextView) view.findViewById(R.id.preference_title);
		mSummaryText = (TextView) view.findViewById(R.id.preference_summary);
		mSeekBar = (SeekBar) view.findViewById(R.id.preference_seek);

		mTitleText.setText(mTitle);
		mSummaryText.setText(mSummary);
		mSeekBar.setMax(mMaxValue);
		mSeekBar.setProgress(mDefaultValue);
		mSeekBar.setOnSeekBarChangeListener(this);
	}

	@Override
	public void setSummary(CharSequence summary) {
		mSummary = summary;
		if (mSummaryText != null) {
			mSummaryText.setText(summary);
		}
	}

	@Override
	public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
		mChangeListener = onPreferenceChangeListener;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		persistInt(progress);
		if (mChangeListener != null) {
			mChangeListener.onPreferenceChange(this, progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		if (restorePersistedValue) {
			// Restore existing state
			mDefaultValue = this.getPersistedInt(DEFAULT_DEFAULT_VALUE);
		} else {
			// Set default state from the XML attribute
			mDefaultValue = (Integer) defaultValue;
			persistInt(mDefaultValue);
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInteger(index, DEFAULT_DEFAULT_VALUE);
	}

	private static class SavedState extends BaseSavedState {
		// Member that holds the setting's value
		// Change this data type to match the type saved by your Preference
		int value;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		public SavedState(Parcel source) {
			super(source);
			// Get the current preference's value
			value = source.readInt(); // Change this to read the appropriate
										// data type
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			// Write the preference's value
			dest.writeInt(value); // Change this to write the appropriate data
									// type
		}

		// Standard creator object using an instance of this class
		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>() {

					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}

					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		// Check whether this Preference is persistent (continually saved)
		if (isPersistent()) {
			// No need to save instance state since it's persistent, use
			// superclass state
			return superState;
		}

		// Create instance of custom BaseSavedState
		final SavedState myState = new SavedState(superState);
		// Set the state's value with the class member that holds current
		// setting value
		myState.value = mSeekBar.getProgress();
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		// Check whether we saved the state in onSaveInstanceState
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save the state, so call superclass
			super.onRestoreInstanceState(state);
			return;
		}

		// Cast state to custom BaseSavedState and pass to superclass
		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());

		// Set this Preference's widget to reflect the restored state
		mSeekBar.setProgress(myState.value);
	}

}
