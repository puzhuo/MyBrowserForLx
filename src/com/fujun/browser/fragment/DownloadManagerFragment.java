
package com.fujun.browser.fragment;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fujun.browser.constants.Constants;
import com.fujun.browser.download.BrowserDownloadManager;
import com.fujun.browser.loader.DownloadLoader;
import com.fujun.browser.model.DownloadItem;
import com.fujun.browser.provider.BrowserProvider;
import com.fujun.browser.utils.Utils;
import com.kukuai.daohang.R;

public class DownloadManagerFragment extends Fragment {

	private static final int DOWNLOAD_LOADER_ID = 0;

	private static final int DOWNLOADING_MENU_DELETE = 0;
	private static final int DOWNLOADED_MENU_OPEN = 0;
	private static final int DOWNLOADED_MENU_DELETE = 1;
	private static final int DOWNLOADED_MENU_REDOWNLOAD = 2;

	private ExpandableListView mListView;
	private DownloadExpandableAdapter mAdapter;
	private ArrayList<DownloadItem> mDownloadingItems = new ArrayList<DownloadItem>();
	private ArrayList<DownloadItem> mDownloadedItems = new ArrayList<DownloadItem>();
	private TextView mClearTextView;
	private TextView mBackTextView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(
				R.layout.download_manage_fragment_layout, null);
		mClearTextView = (TextView) view.findViewById(R.id.download_clear_btn);
		mClearTextView.setOnClickListener(mOnClickListener);
		mClearTextView.setEnabled(false);
		mBackTextView = (TextView) view.findViewById(R.id.download_back_btn);
		mBackTextView.setOnClickListener(mOnClickListener);
		mListView = (ExpandableListView) view.findViewById(R.id.list);
		mAdapter = new DownloadExpandableAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
					int childPosition, long id) {
				if (groupPosition == DownloadExpandableAdapter.DOWNLOADED_GROUP_INDEX) {
					DownloadItem item = mDownloadedItems.get(childPosition);
					File file = new File(Environment.getExternalStorageDirectory(),
							Constants.DOWNLOAD_FOLDER + "/" + item.fileName);
					if (file.exists()) {
						Utils.openFile(getActivity(), file, item.mimeType);
					} else {
						Toast.makeText(getActivity(), R.string.file_not_exsit_text,
								Toast.LENGTH_SHORT).show();
					}
				} else if (groupPosition == DownloadExpandableAdapter.DOWNLOADING_GROUP_INDEX) {
					final DownloadItem item = mDownloadingItems.get(childPosition);
					new AsyncTask<Void, Void, Integer>() {

						@Override
						protected Integer doInBackground(Void... params) {
							Cursor cursor = null;
							try {
								cursor = getActivity().getContentResolver().query(
										BrowserProvider.TABLE_DOWNLOAD_URI, null,
										BrowserProvider.TABLE_DOWNLOAD_URL + "=?", new String[] {
											item.url
										}, null);
								if (cursor != null && cursor.getCount() > 0) {
									cursor.moveToFirst();
									int status = cursor.getInt(cursor
											.getColumnIndexOrThrow(BrowserProvider.TABLE_DOWNLOAD_STATUS));
									return Integer.valueOf(status);
								}
							} finally {
								if (cursor != null && !cursor.isClosed()) {
									cursor.close();
									cursor = null;
								}
							}
							return null;
						}

						protected void onPostExecute(Integer result) {
							switch (result.intValue()) {
								case DownloadItem.DOWNLOAD_STATUS_DOWNLOADING:
								{
									Intent intent = new Intent(getActivity(),
											BrowserDownloadManager.class);
									intent.putExtra(Constants.EXTRA_PAUSE_DOWNLOAD, true);
									intent.putExtra(Constants.EXTRA_DOWNLOAD_URL, item.url);
									getActivity().startService(intent);
								}
									break;
								case DownloadItem.DOWNLOAD_STATUS_PAUSED:
								{
									Intent intent = new Intent(getActivity(),
											BrowserDownloadManager.class);
									intent.putExtra(Constants.EXTRA_RESUME_DOWNLOAD, true);
									intent.putExtra(Constants.EXTRA_DOWNLOAD_ITEM, item);
									getActivity().startService(intent);
								}
									break;
								case DownloadItem.DOWNLOAD_STATUS_WAIT:
									Toast.makeText(getActivity(), R.string.file_is_waiting,
											Toast.LENGTH_SHORT).show();
									break;
								case DownloadItem.DOWNLOAD_STATUS_ERROR:
									Toast.makeText(getActivity(), R.string.file_downloads_error,
											Toast.LENGTH_SHORT).show();
									break;
							}
						};

					}.execute();
				}
				return false;
			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				ExpandableListView listView = (ExpandableListView) parent;
				final int currentPosition = ExpandableListView
						.getPackedPositionChild(listView
								.getExpandableListPosition(position));
				if (currentPosition == -1)
					return false;

				final int groupPosition = ExpandableListView
						.getPackedPositionGroup(listView
								.getExpandableListPosition(position));
				final int childPosition = ExpandableListView.getPackedPositionChild(listView
						.getExpandableListPosition(position));
				if (groupPosition == DownloadExpandableAdapter.DOWNLOADED_GROUP_INDEX) {
					final DownloadItem item = mDownloadedItems.get(childPosition);
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setItems(R.array.downloaded_menu, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
								case DOWNLOADED_MENU_OPEN:
								{
									File file = new File(Environment.getExternalStorageDirectory(),
											Constants.DOWNLOAD_FOLDER + "/" + item.fileName);
									Utils.openFile(getActivity(), file, item.mimeType);
								}
									break;
								case DOWNLOADED_MENU_DELETE:
								{
									View view = LayoutInflater.from(getActivity()).inflate(
											R.layout.delete_confirm_dialog_layout,
											null);
									final TextView textView = (TextView) view
											.findViewById(R.id.delete_confirm_dialog_title);
									final CheckBox checkBox = (CheckBox) view
											.findViewById(R.id.delete_confirm_dialog_checkbox);
									String format = getString(R.string.sure_to_delete_file);
									textView.setText(String.format(format, item.fileName));
									new AlertDialog.Builder(getActivity())
											.setTitle(R.string.delete_file)
											.setView(view)
											.setPositiveButton(R.string.ok, new OnClickListener() {

												@Override
												public void onClick(DialogInterface dialog,
														int which) {
													new AsyncTask<Void, Void, Void>() {

														@Override
														protected Void doInBackground(Void...
																params) {
															if (checkBox.isChecked()) {
																File file = new File(
																		Environment
																				.getExternalStorageDirectory(),
																		Constants.DOWNLOAD_FOLDER
																				+ "/"
																				+ item.fileName);
																if (file.exists()) {
																	file.delete();
																}
															}
															getActivity()
																	.getContentResolver()
																	.delete(
																			BrowserProvider.TABLE_DOWNLOAD_URI,
																			BrowserProvider.TABLE_DOWNLOAD_URL
																					+
																					"=?",
																			new String[] {
																				item.url
																			});
															return null;
														}
													}.execute();
												}
											}).setNegativeButton(R.string.cancel, null).show();
									dialog.dismiss();
								}
									break;
								case DOWNLOADED_MENU_REDOWNLOAD:
								{
									Intent intent = new Intent(getActivity(),
											BrowserDownloadManager.class);
									intent.putExtra(Constants.EXTRA_RESTART_DOWNLOAD, true);
									intent.putExtra(Constants.EXTRA_DOWNLOAD_ITEM, item);
									getActivity().startService(intent);
								}
									break;
							}
						}
					}).show();
				} else if (groupPosition == DownloadExpandableAdapter.DOWNLOADING_GROUP_INDEX) {
					final DownloadItem item = mDownloadingItems.get(childPosition);
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setItems(R.array.downloading_menu, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
								case DOWNLOADING_MENU_DELETE:
									View view = LayoutInflater.from(getActivity()).inflate(
											R.layout.delete_confirm_dialog_layout,
											null);
									final TextView textView = (TextView) view
											.findViewById(R.id.delete_confirm_dialog_title);
									final CheckBox checkBox = (CheckBox) view
											.findViewById(R.id.delete_confirm_dialog_checkbox);
									String format = getString(R.string.sure_to_delete_file);
									textView.setText(String.format(format, item.fileName));
									new AlertDialog.Builder(getActivity())
											.setTitle(R.string.delete_file)
											.setView(view)
											.setPositiveButton(R.string.ok, new OnClickListener() {

												@Override
												public void onClick(DialogInterface dialog,
														int which) {
													Intent intent = new Intent(getActivity(),
															BrowserDownloadManager.class);
													intent.putExtra(
															Constants.EXTRA_DELETE_DOWNLOAD, true);
													intent.putExtra(Constants.EXTRA_DOWNLOAD_URL,
															item.url);
													getActivity().startService(intent);
													if (checkBox.isChecked()) {
														File file = new File(
																Environment
																		.getExternalStorageDirectory(),
																Constants.DOWNLOAD_FOLDER
																		+ "/"
																		+ item.fileName);
														if (file.exists()) {
															file.delete();
														}
													}
												}
											}).setNegativeButton(R.string.cancel, null).show();
									dialog.dismiss();
									break;
							}
						}
					}).show();
				}
				return true;
			}
		});
		mListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

			@Override
			public void onGroupCollapse(int groupPosition) {
				setClearBtnEnabled();
			}
		});
		mListView.setOnGroupExpandListener(new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				switch (groupPosition) {
					case DownloadExpandableAdapter.DOWNLOADED_GROUP_INDEX:
						mListView.collapseGroup(DownloadExpandableAdapter.DOWNLOADING_GROUP_INDEX);
						break;
					case DownloadExpandableAdapter.DOWNLOADING_GROUP_INDEX:
						mListView.collapseGroup(DownloadExpandableAdapter.DOWNLOADED_GROUP_INDEX);
						break;
				}
				setClearBtnEnabled();
			}
		});
		getLoaderManager().initLoader(DOWNLOAD_LOADER_ID, null, mCallbacks);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		setClearBtnEnabled();
	}

	private void setClearBtnEnabled() {
		if ((mListView.isGroupExpanded(DownloadExpandableAdapter.DOWNLOADED_GROUP_INDEX) && mDownloadedItems
				.size() > 0)
				|| (mListView.isGroupExpanded(DownloadExpandableAdapter.DOWNLOADING_GROUP_INDEX) && mDownloadingItems
						.size() > 0)) {
			mClearTextView.setEnabled(true);
		} else {
			mClearTextView.setEnabled(false);
		}
	}

	private View.OnClickListener mOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.download_back_btn:
					getActivity().finish();
					break;
				case R.id.download_clear_btn:
					if (mListView.isGroupExpanded(DownloadExpandableAdapter.DOWNLOADED_GROUP_INDEX)) {
						View view = LayoutInflater.from(getActivity()).inflate(
								R.layout.delete_confirm_dialog_layout,
								null);
						final TextView textView = (TextView) view
								.findViewById(R.id.delete_confirm_dialog_title);
						final CheckBox checkBox = (CheckBox) view
								.findViewById(R.id.delete_confirm_dialog_checkbox);
						String format = getString(R.string.clear_all_downloaded_files);
						textView.setText(String.format(format, mDownloadedItems.size()));
						new AlertDialog.Builder(getActivity())
								.setTitle(R.string.clear_file)
								.setView(view)
								.setPositiveButton(R.string.ok, new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										new AsyncTask<Void, Void, Void>() {

											@Override
											protected Void doInBackground(Void...
													params) {
												if (checkBox.isChecked()) {
													File file = null;
													for (DownloadItem item : mDownloadedItems) {
														file = new File(Environment
																.getExternalStorageDirectory(),
																Constants.DOWNLOAD_FOLDER + "/"
																		+ item.fileName);
														if (file.exists()) {
															file.delete();
														}
													}
												}
												getActivity().getContentResolver().delete(
														BrowserProvider.TABLE_DOWNLOAD_URI,
														BrowserProvider.TABLE_DOWNLOAD_STATUS
																+ "=?",
														new String[] {
															DownloadItem.DOWNLOAD_STATUS_DOWNLOADED
																	+ ""
														});
												return null;
											}
										}.execute();
									}
								}).setNegativeButton(R.string.cancel, null).show();
					} else if (mListView
							.isGroupExpanded(DownloadExpandableAdapter.DOWNLOADING_GROUP_INDEX)) {
						View view = LayoutInflater.from(getActivity()).inflate(
								R.layout.delete_confirm_dialog_layout,
								null);
						final TextView textView = (TextView) view
								.findViewById(R.id.delete_confirm_dialog_title);
						final CheckBox checkBox = (CheckBox) view
								.findViewById(R.id.delete_confirm_dialog_checkbox);
						String format = getString(R.string.clear_all_downloading_files);
						textView.setText(String.format(format, mDownloadingItems.size()));
						new AlertDialog.Builder(getActivity())
								.setTitle(R.string.clear_file)
								.setView(view)
								.setPositiveButton(R.string.ok, new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										new AsyncTask<Void, Void, Void>() {

											@Override
											protected Void doInBackground(Void...
													params) {
												if (checkBox.isChecked()) {
													File file = null;
													for (DownloadItem item : mDownloadingItems) {
														file = new File(Environment
																.getExternalStorageDirectory(),
																Constants.DOWNLOAD_FOLDER + "/"
																		+ item.fileName);
														if (file.exists()) {
															file.delete();
														}
													}
												}
												getActivity().getContentResolver().delete(
														BrowserProvider.TABLE_DOWNLOAD_URI,
														BrowserProvider.TABLE_DOWNLOAD_STATUS
																+ "!=?",
														new String[] {
															DownloadItem.DOWNLOAD_STATUS_DOWNLOADED
																	+ ""
														});
												return null;
											}
										}.execute();
									}
								}).setNegativeButton(R.string.cancel, null).show();
					}
					break;
			}
		}
	};

	private LoaderCallbacks<ArrayList<DownloadItem>> mCallbacks = new LoaderCallbacks<ArrayList<DownloadItem>>() {

		@Override
		public Loader<ArrayList<DownloadItem>> onCreateLoader(int arg0,
				Bundle arg1) {
			return new DownloadLoader(getActivity());
		}

		@Override
		public void onLoadFinished(Loader<ArrayList<DownloadItem>> arg0,
				ArrayList<DownloadItem> data) {
			mDownloadedItems.clear();
			mDownloadingItems.clear();
			if (data != null) {
				for (DownloadItem item : data) {
					switch (item.status) {
						case DownloadItem.DOWNLOAD_STATUS_DOWNLOADING:
						case DownloadItem.DOWNLOAD_STATUS_PAUSED:
						case DownloadItem.DOWNLOAD_STATUS_WAIT:
						case DownloadItem.DOWNLOAD_STATUS_ERROR:
							mDownloadingItems.add(item);
							break;
						case DownloadItem.DOWNLOAD_STATUS_DOWNLOADED:
							mDownloadedItems.add(item);
							break;
					}
				}
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
			}
			setClearBtnEnabled();
		}

		@Override
		public void onLoaderReset(Loader<ArrayList<DownloadItem>> arg0) {
		}
	};

	private class DownloadingItemHolder {
		public ImageView statusImage;
		public TextView title;
		public ProgressBar progressBar;
		public TextView statusText;
		public TextView sizeText;
		public TextView progressText;
	}

	private class DownloadedItemHolder {
		public TextView title;
		public TextView size;
	}

	private class DownloadExpandableAdapter extends BaseExpandableListAdapter {

		private static final int GROUP_COUNT = 2;
		public static final int DOWNLOADING_GROUP_INDEX = 0;
		public static final int DOWNLOADED_GROUP_INDEX = 1;
		private static final int CHILD_VIEW_TYPE_COUNT = 2;
		private static final int CHILD_VIEW_TYPE_DOWNLOADING = 0;
		private static final int CHILD_VIEW_TYPE_DOWNLOADED = 1;

		@Override
		public int getGroupCount() {
			return GROUP_COUNT;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			int count = 0;
			switch (groupPosition) {
				case DOWNLOADING_GROUP_INDEX:
					count = mDownloadingItems == null ? 0 : mDownloadingItems
							.size();
					break;
				case DOWNLOADED_GROUP_INDEX:
					count = mDownloadedItems == null ? 0 : mDownloadedItems.size();
					break;
			}
			return count;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public int getChildType(int groupPosition, int childPosition) {
			switch (groupPosition) {
				case DOWNLOADED_GROUP_INDEX:
					return CHILD_VIEW_TYPE_DOWNLOADED;
				case DOWNLOADING_GROUP_INDEX:
					return CHILD_VIEW_TYPE_DOWNLOADING;
			}
			return super.getChildType(groupPosition, childPosition);
		}

		@Override
		public int getChildTypeCount() {
			return CHILD_VIEW_TYPE_COUNT;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			View view = convertView != null ? convertView : LayoutInflater
					.from(getActivity()).inflate(
							R.layout.download_group_header, null);
			TextView textView = (TextView) view
					.findViewById(R.id.download_header_text);
			ImageView imageView = (ImageView) view
					.findViewById(R.id.download_group_indicator);
			String textFormat = (groupPosition == DOWNLOADING_GROUP_INDEX) ? getString(R.string.downloading_text)
					: getString(R.string.downloaded_text);
			textView.setText(String.format(textFormat, ""
					+ getChildrenCount(groupPosition)));
			imageView.setImageResource(isExpanded ? R.drawable.indicator_open
					: R.drawable.indicator_close);
			return view;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			View view = null;
			switch (getChildType(groupPosition, childPosition)) {
				case CHILD_VIEW_TYPE_DOWNLOADED:
					view = getDownloadedView(childPosition, convertView);
					break;
				case CHILD_VIEW_TYPE_DOWNLOADING:
					view = getDownloadingView(childPosition, convertView);
					break;
			}
			return view;
		}

		private View getDownloadedView(int position, View convertView) {
			View view = null;
			DownloadedItemHolder holder = null;
			if (convertView == null) {
				view = LayoutInflater.from(getActivity()).inflate(
						R.layout.downloaded_child_item, null);
				holder = new DownloadedItemHolder();
				holder.title = (TextView) view
						.findViewById(R.id.downloaded_title);
				holder.size = (TextView) view
						.findViewById(R.id.downloaded_size);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (DownloadedItemHolder) view.getTag();
			}
			DownloadItem item = mDownloadedItems.get(position);
			holder.title.setText(item.fileName);
			holder.size.setText(Utils.formatFileSize(item.fileSize));
			return view;
		}

		private View getDownloadingView(int position, View convertView) {
			View view = null;
			DownloadingItemHolder holder = null;
			if (convertView == null) {
				view = LayoutInflater.from(getActivity()).inflate(
						R.layout.downloading_child_item, null);
				holder = new DownloadingItemHolder();
				holder.progressBar = (ProgressBar) view
						.findViewById(R.id.download_progressbar);
				holder.progressText = (TextView) view
						.findViewById(R.id.download_persent);
				holder.sizeText = (TextView) view
						.findViewById(R.id.download_speed);
				holder.statusImage = (ImageView) view
						.findViewById(R.id.download_item_icon);
				holder.statusText = (TextView) view
						.findViewById(R.id.download_text);
				holder.title = (TextView) view
						.findViewById(R.id.download_title);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (DownloadingItemHolder) view.getTag();
			}

			DownloadItem item = mDownloadingItems.get(position);
			holder.title.setText(item.fileName);
			switch (item.status) {
				case DownloadItem.DOWNLOAD_STATUS_DOWNLOADING:
					holder.statusText.setText(R.string.download_downloading);
					holder.statusImage
							.setImageResource(R.drawable.ic_pause_download);
					break;
				case DownloadItem.DOWNLOAD_STATUS_PAUSED:
					holder.statusText.setText(R.string.download_pause);
					holder.statusImage
							.setImageResource(R.drawable.ic_start_download);
					break;
				case DownloadItem.DOWNLOAD_STATUS_WAIT:
					holder.statusText.setText(R.string.download_wait);
					holder.statusImage
							.setImageResource(R.drawable.ic_queued_download);
					break;
				case DownloadItem.DOWNLOAD_STATUS_ERROR:
					holder.statusText.setText(R.string.download_error);
					holder.statusImage
							.setImageResource(R.drawable.ic_start_download);
					break;
			}

			String sizeFormat = getString(R.string.download_size_text);
			String size = String.format(sizeFormat,
					Utils.formatFileSize(item.currentSize),
					Utils.formatFileSize(item.fileSize));
			holder.sizeText.setText(size);

			int percent = Utils.getProgress(item.currentSize, item.fileSize);
			holder.progressBar.setProgress(percent);
			holder.progressText.setText(percent + "%");
			return view;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}
}
