
package com.fujun.browser.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fujun.browser.activity.AddNaviActivity.AddNaviItemClickListener;
import com.fujun.browser.activity.FavHisActivity.FavHisEmptyListener;
import com.fujun.browser.constants.Constants;
import com.fujun.browser.loader.FavLoader;
import com.fujun.browser.model.FavItem;
import com.kukuai.daohang.R;

public class FavFragment extends Fragment implements
		LoaderCallbacks<ArrayList<FavItem>> {

	private static final String TAG = "FavFragment";
	private static final int FAV_LOADER_ID = 0;

	private ListView mListView;
	private FavAdapter mAdapter;
	private ArrayList<FavItem> mItems = new ArrayList<FavItem>();
	private AddNaviItemClickListener mOnAddNaviClickListener;
	private FavHisEmptyListener mFavHisEmptyListener;

	public void setOnFavHisEmptyListener(FavHisEmptyListener listener) {
		mFavHisEmptyListener = listener;
	}

	public void setOnFavItemClickListener(AddNaviItemClickListener listener) {
		mOnAddNaviClickListener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLoaderManager().initLoader(FAV_LOADER_ID, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fav_his_fragment_layout, null);
		mListView = (ListView) view.findViewById(R.id.list);
		mAdapter = new FavAdapter(getActivity());
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mItems != null) {
					try {
						FavItem item = mItems.get(position);
						if (mOnAddNaviClickListener != null) {
							mOnAddNaviClickListener.onAddNaviItemClick(item);
							return;
						}
						Intent intent = new Intent();
						intent.putExtra(Constants.FAV_RESULT_EXTRA_URL,
								item.url);
						getActivity().setResult(Activity.RESULT_OK, intent);
						getActivity().finish();
					} catch (Exception e) {
						e.printStackTrace();
						Log.e(TAG, "array outofbounds");
					}
				}
			}

		});

		View emptyView = inflater.inflate(R.layout.fav_his_empty_view, null);
		TextView textView = (TextView) emptyView.findViewById(R.id.empty_text);
		textView.setText(R.string.empty_fav_text);
		textView.setCompoundDrawablesWithIntrinsicBounds(0,
				R.drawable.empty_fav, 0, 0);

		((ViewGroup) mListView.getParent()).addView(emptyView,
				new LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));
		mListView.setEmptyView(emptyView);
		return view;
	}

	public class FavAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView titleTextView;
			public TextView urlTextView;
			public ImageView iconImageView;
		}

		private LayoutInflater mInflater;

		public FavAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mItems == null ? 0 : mItems.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			ViewHolder holder = null;
			if (convertView == null) {
				view = mInflater.inflate(R.layout.fav_his_item_layout, null);
				holder = new ViewHolder();
				holder.titleTextView = (TextView) view
						.findViewById(R.id.tab_item_title);
				holder.urlTextView = (TextView) view
						.findViewById(R.id.tab_item_url);
				holder.iconImageView = (ImageView) view
						.findViewById(R.id.tab_item_icon);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}

			FavItem item = mItems.get(position);
			holder.titleTextView.setText(item.title);
			holder.urlTextView.setText(item.url);
			if (item.icon != null) {
				holder.iconImageView.setImageBitmap(item.icon);
			} else {
				holder.iconImageView.setImageResource(R.drawable.ic_launcher);
			}

			return view;
		}

	}

	public boolean isEmpty() {
		return mItems.size() == 0;
	}

	@Override
	public Loader<ArrayList<FavItem>> onCreateLoader(int arg0, Bundle arg1) {
		return new FavLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<ArrayList<FavItem>> arg0,
			ArrayList<FavItem> data) {
		mItems = data;
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
		if (mFavHisEmptyListener != null) {
			mFavHisEmptyListener.onFavHisEmpty(true, mItems.size() == 0);
		}
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<FavItem>> arg0) {
	}
}
