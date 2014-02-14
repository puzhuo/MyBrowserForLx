
package com.fujun.browser.activity;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fujun.browser.loader.FavLoader;
import com.fujun.browser.model.FavItem;
import com.fujun.browser.provider.BrowserProvider;
import com.fujun.browser.utils.Utils;
import com.kukuai.daohang.R;

public class FavManageActivity extends BaseFragmentActivity implements
		OnClickListener, LoaderCallbacks<ArrayList<FavItem>> {

	private class CheckItem {
		public boolean isChecked;
		public FavItem favItem;
	}

	private static final int LOADER_ID = 0;

	private TextView mDeleteTextView;
	private TextView mBackTextView;
	private int mCheckedCount;
	private CheckBox mCheckAllBox;
	private ListView mListView;
	private FavManageAdapter mAdapter = new FavManageAdapter();
	private ArrayList<CheckItem> mItems = new ArrayList<CheckItem>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fav_manage_layout);
		getSupportLoaderManager().initLoader(LOADER_ID, null, this);

		mListView = (ListView) findViewById(R.id.fav_manage_list);
		mListView.setAdapter(mAdapter);
		View emptyView = findViewById(R.id.empty);
		TextView textView = (TextView) emptyView.findViewById(R.id.empty_text);
		textView.setText(R.string.empty_fav_text);
		textView.setCompoundDrawablesWithIntrinsicBounds(0,
				R.drawable.empty_fav, 0, 0);
		mListView.setEmptyView(emptyView);

		mDeleteTextView = (TextView) findViewById(R.id.fav_manage_delete);
		mDeleteTextView.setOnClickListener(this);
		mBackTextView = (TextView) findViewById(R.id.fav_manage_back);
		mBackTextView.setOnClickListener(this);

		mCheckAllBox = (CheckBox) findViewById(R.id.fav_manage_checkbox);
		mCheckAllBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					setAllChecked(true);
				} else if (mAdapter.getCount() == mCheckedCount) {
					setAllChecked(false);
				}
			}
		});
		setDeleteNum();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	private void setDeleteNum() {
		mCheckedCount = 0;
		for (CheckItem item : mItems) {
			if (item.isChecked) {
				mCheckedCount++;
			}
		}
		String format = getString(R.string.fav_manage_delete);
		mDeleteTextView.setText(String.format(format, mCheckedCount));
		if (mCheckedCount == 0) {
			mDeleteTextView.setEnabled(false);
		} else {
			mDeleteTextView.setEnabled(true);
		}
	}

	private void setAllChecked(boolean checked) {
		if (mItems != null) {
			for (CheckItem item : mItems) {
				item.isChecked = checked;
			}
		}
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	private void deleteFav() {
		if (mItems != null) {
			ArrayList<String> urls = new ArrayList<String>();
			for (CheckItem item : mItems) {
				if (item.isChecked) {
					urls.add(item.favItem.url);
				}
			}
			ContentProviderOperation.Builder builder = null;
			ArrayList<ContentProviderOperation> list = new ArrayList<ContentProviderOperation>();
			for (String url : urls) {
				builder = ContentProviderOperation
						.newDelete(BrowserProvider.TABLE_FAV_URI);
				builder.withSelection(BrowserProvider.TABLE_FAV_URL + "=?",
						new String[] {
							url
						});
				list.add(builder.build());
			}
			try {
				getContentResolver()
						.applyBatch(BrowserProvider.AUTHORITY, list);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}
		}

		setDeleteNum();
		mCheckAllBox.setChecked(false);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.fav_manage_delete:
				deleteFav();
				break;
			case R.id.fav_manage_back:
				finish();
				break;
		}
	}

	private class ViewHolder {
		public TextView titleTextView;
		public TextView urlTextView;
		public ImageView editImageView;
		public CheckBox checkBox;
	}

	private class FavManageAdapter extends BaseAdapter {

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
				view = getLayoutInflater().inflate(
						R.layout.fav_manage_item_layout, null);
				holder = new ViewHolder();
				holder.titleTextView = (TextView) view
						.findViewById(R.id.fav_manage_item_title);
				holder.urlTextView = (TextView) view
						.findViewById(R.id.fav_manage_item_url);
				holder.checkBox = (CheckBox) view
						.findViewById(R.id.fav_manage_item_checkbox);
				holder.editImageView = (ImageView) view
						.findViewById(R.id.fav_manage_item_edit);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}

			final CheckItem item = mItems.get(position);
			holder.titleTextView.setText(item.favItem.title);
			holder.urlTextView.setText(item.favItem.url);
			holder.checkBox
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							item.isChecked = isChecked;
							setDeleteNum();
							if (!isChecked) {
								mCheckAllBox.setChecked(false);
							}
						}
					});
			holder.editImageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Utils.showFavEditDialog(FavManageActivity.this, item.favItem);
				}
			});
			holder.checkBox.setChecked(item.isChecked);
			return view;
		}

	}

	@Override
	public Loader<ArrayList<FavItem>> onCreateLoader(int arg0, Bundle arg1) {
		return new FavLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<ArrayList<FavItem>> arg0,
			ArrayList<FavItem> data) {
		mItems.clear();
		CheckItem item = null;
		for (FavItem favItem : data) {
			item = new CheckItem();
			item.favItem = favItem;
			mItems.add(item);
		}
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
		setDeleteNum();
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<FavItem>> arg0) {
	}

}
