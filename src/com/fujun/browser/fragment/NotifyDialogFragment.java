
package com.fujun.browser.fragment;

import java.io.File;
import java.io.Serializable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.text.format.Formatter;

import com.fujun.browser.constants.Constants;
import com.fujun.browser.download.BrowserDownloadManager;
import com.fujun.browser.model.DownloadItem;
import com.fujun.browser.provider.BrowserProvider;
import com.fujun.browser.update.UpdateInfo;
import com.fujun.browser.utils.Utils;
import com.kukuai.daohang.R;

public abstract class NotifyDialogFragment extends DialogFragment {

	public abstract void positiveButtonClicked(Serializable info);

	public static final String ARGUMENT_DOWNLOAD_ITEM = "argument_item";
	public static final String ARGUMENT_NOTIFY_ID = "notify_id";

	public static final int DOWNLOAD_OVERLAP_ID = 0;
	public static final int DOWNLOADING_LIST_MENU_ID = 1;
	public static final int DOWNLOADED_LIST_MENU_ID = 2;
	public static final int DOWNLOADED_UPDATE_ID = 3;

	private static final int DOWNLOADING_MENU_DELETE = 0;

	private static final int DOWNLOADED_MENU_OPEN = 0;
	private static final int DOWNLOADED_MENU_DELETE = 1;
	private static final int DOWNLOADED_MENU_REDOWNLOAD = 2;

	private UpdateInfo mUpdateInfo;
	private DownloadItem mDownloadItem;
	private Bundle mArgument;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mArgument = getArguments();
		Serializable serializable = mArgument.getSerializable(
				ARGUMENT_DOWNLOAD_ITEM);
		if (serializable instanceof DownloadItem) {
			mDownloadItem = (DownloadItem) serializable;
		} else if (serializable instanceof UpdateInfo) {
			mUpdateInfo = (UpdateInfo) serializable;
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		switch (mArgument.getInt(ARGUMENT_NOTIFY_ID)) {
			case DOWNLOAD_OVERLAP_ID:
				builder.setMessage(R.string.download_overlap_message);
				builder.setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						positiveButtonClicked(mDownloadItem);
					}
				});
				builder.setNegativeButton(R.string.cancel, null);
				break;
			case DOWNLOADED_LIST_MENU_ID:
				if (mDownloadItem == null) {
					throw new IllegalArgumentException();
				}
				builder.setItems(R.array.downloaded_menu, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case DOWNLOADED_MENU_OPEN:
							{
								File file = new File(Environment.getExternalStorageDirectory(),
										Constants.DOWNLOAD_FOLDER + "/" + mDownloadItem.fileName);
								Utils.openFile(getActivity(), file, mDownloadItem.mimeType);
							}
								break;
							case DOWNLOADED_MENU_DELETE:
							{
								new AsyncTask<Void, Void, Void>() {

									@Override
									protected Void doInBackground(Void... params) {
										File file = new File(Environment
												.getExternalStorageDirectory(),
												Constants.DOWNLOAD_FOLDER + "/"
														+ mDownloadItem.fileName);
										if (file.exists()) {
											file.delete();
										}
										getActivity().getContentResolver().delete(
												BrowserProvider.TABLE_DOWNLOAD_URI,
												BrowserProvider.TABLE_DOWNLOAD_URL + "=?",
												new String[] {
													mDownloadItem.url
												});
										return null;
									}
								}.execute();
							}
								break;
							case DOWNLOADED_MENU_REDOWNLOAD:
							{
								Intent intent = new Intent(getActivity(),
										BrowserDownloadManager.class);
								intent.putExtra(Constants.EXTRA_RESTART_DOWNLOAD, true);
								intent.putExtra(Constants.EXTRA_DOWNLOAD_ITEM, mDownloadItem);
								getActivity().startService(intent);
							}
								break;
						}
					}
				});
				break;
			case DOWNLOADING_LIST_MENU_ID:
				builder.setItems(R.array.downloading_menu, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case DOWNLOADING_MENU_DELETE:
								Intent intent = new Intent(getActivity(),
										BrowserDownloadManager.class);
								intent.putExtra(Constants.EXTRA_DELETE_DOWNLOAD, true);
								intent.putExtra(Constants.EXTRA_DOWNLOAD_URL, mDownloadItem.url);
								getActivity().startService(intent);
								break;
						}
					}
				});
				break;
			case DOWNLOADED_UPDATE_ID:
				if (mUpdateInfo == null) {
					return null;
				}
				builder.setTitle(getActivity().getString(R.string.update_dialog_title)
						+ mUpdateInfo.remoteVersion);
				builder.setMessage(mUpdateInfo.updateDescription + "\n"
						+ getActivity().getString(R.string.update_dialog_message_size)
						+ Formatter.formatFileSize(getActivity(), mUpdateInfo.contentLength));
				builder.setPositiveButton(
						getActivity().getString(R.string.update_dialog_positive_button),
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								positiveButtonClicked(mUpdateInfo);
							}
						});
				builder.setNegativeButton(
						getActivity().getString(R.string.update_dialog_negative_button),
						null);
				break;
			default:
				throw new IllegalArgumentException("NotifyDialogFragment: dialog id is illegal!!!");
		}
		return builder.create();
	}
}
