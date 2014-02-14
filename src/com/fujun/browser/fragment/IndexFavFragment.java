
package com.fujun.browser.fragment;

import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fujun.browser.activity.AddNaviActivity;
import com.fujun.browser.constants.Constants;
import com.fujun.browser.loader.IndexFavLoader;
import com.fujun.browser.model.FavItem;
import com.fujun.browser.view.FavGridLayout;
import com.kukuai.daohang.R;

public class IndexFavFragment extends Fragment {

	public interface onFavClickListener {
		public void onFavClick(View v);
	}

	public interface onFavLongClickListener {
		public void onFavLongClick(View v);
	}

	private static final String TAG = "IndexNaviFragment";
	private static final int INDEX_NAVI_LOADER_ID = 0;

	private FavGridLayout mFavGridLayout;
	private onFavClickListener mListener;
	private onFavLongClickListener mLongClickListener;

	public void setOnFavClickListener(onFavClickListener listener) {
		mListener = listener;
	}

	public void setOnFavLongClickListener(onFavLongClickListener listener) {
		mLongClickListener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLoaderManager().initLoader(INDEX_NAVI_LOADER_ID, null,
				mIndexNaviLoaderCallbacks);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.index_fav_layout, container,
				false);
		mFavGridLayout = (FavGridLayout) view.findViewById(R.id.fav_grid);
		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getLoaderManager().destroyLoader(INDEX_NAVI_LOADER_ID);
	}

	private void setData(FavItem[] infos) {
		if (infos == null) {
			Log.e(TAG, "IndexNaviFragment data array is null");
			return;
		}
		mFavGridLayout.removeAllViews();
		FavItem info = null;
		View view = null;
		TextView textView = null;
		int length = infos.length;
		for (int i = 0; i < length; i++) {
			info = infos[i];
			view = getActivity().getLayoutInflater().inflate(
					R.layout.fav_item_layout_new, null);
			textView = (TextView) view.findViewById(R.id.index_fav_title);
			textView.setText(info.title);
			textView.setTextColor(Color.WHITE);
			if (info.icon != null) {
				Drawable drawable = new BitmapDrawable(info.icon);
				int size = getResources().getDimensionPixelSize(
						R.dimen.navi_item_icon_size);
				drawable.setBounds(0, 0, size, size);
				view.setBackgroundDrawable(drawable);
			} else {
				// no drawable?
				int pos = i % Constants.FAV_DEFAULT_NUM;
				view.setBackgroundResource(Constants.FAV_DEFAULT_BG[pos]);
			}
			view.setTag(info);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mListener != null) {
						mListener.onFavClick(v);
					}
				}
			});
			view.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					if (mLongClickListener != null) {
						mLongClickListener.onFavLongClick(v);
						return true;
					}
					return false;
				}
			});
			mFavGridLayout.addView(view);
		}
		// add add button
		view = getActivity().getLayoutInflater().inflate(
				R.layout.fav_item_layout_new, null);
		textView = (TextView) view.findViewById(R.id.index_fav_title);
		textView.setText(R.string.add_text);
		view.setBackgroundResource(R.drawable.navi_add);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), AddNaviActivity.class));
				// Utils.showNaviEditDialog(getActivity(), null);
			}
		});
		mFavGridLayout.addView(view);
	}

	private LoaderCallbacks<List<FavItem>> mIndexNaviLoaderCallbacks = new LoaderCallbacks<List<FavItem>>() {

		@Override
		public Loader<List<FavItem>> onCreateLoader(int arg0, Bundle arg1) {
			return new IndexFavLoader(getActivity());
		}

		@Override
		public void onLoadFinished(Loader<List<FavItem>> loader,
				List<FavItem> data) {
			if (data != null) {
				setData(data.toArray(new FavItem[data.size()]));
			}
		}

		@Override
		public void onLoaderReset(Loader<List<FavItem>> arg0) {
		}
	};

}
