
package com.fujun.browser.fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.fujun.browser.activity.DownloadManagerActivity;
import com.fujun.browser.constants.Constants;
import com.fujun.browser.model.NaviItem;
import com.fujun.browser.utils.Utils;
import com.fujun.browser.view.NaviItemLayout;
import com.fujun.browser.view.NaviItemLayout.onNaviItemClickListener;
import com.kukuai.daohang.R;

public class IndexNaviFragment extends Fragment implements OnClickListener {

	private JSONArray mJsonArray;
	private onNaviItemClickListener mNaviItemClickListener;

	private static final String JSON_FILE_NAME = "navigation_config.json";
	private static final String JSON_ARRAY_NAME = "sites";

	public void setNaviItemClickListener(onNaviItemClickListener listener) {
		mNaviItemClickListener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.index_navi_layout, null);

		new AsyncTask<Void, Void, JSONArray>() {

			@Override
			protected JSONArray doInBackground(Void... params) {
				StringBuffer buffer = new StringBuffer();
				InputStreamReader reader = null;
				JSONArray array = null;
				try {
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
						File file = new File(Environment.getExternalStorageDirectory(),
								Constants.SDCARD_HTML_FOLDER + "/" + JSON_FILE_NAME);
						if (file.exists()) {
							reader = new InputStreamReader(new FileInputStream(file));
						} else {
							reader = new InputStreamReader(getActivity().getAssets()
									.open(JSON_FILE_NAME));
						}
					} else {
						reader = new InputStreamReader(getActivity().getAssets()
								.open(JSON_FILE_NAME));
					}
					char[] chars = new char[1024];
					int len = -1;
					while ((len = reader.read(chars)) > 0) {
						buffer.append(chars, 0, len);
					}
					JSONObject object = new JSONObject(buffer.toString());
					array = object.getJSONArray(JSON_ARRAY_NAME);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return array;
			}

			protected void onPostExecute(JSONArray result) {
				mJsonArray = result;
				LinearLayout layout = (LinearLayout) view
						.findViewById(R.id.navi_layout);
				if (mJsonArray != null) {
					NaviItemLayout itemView = null;
					JSONArray array = null;
					int arrayLength = mJsonArray.length();
					for (int i = 0; i < arrayLength; i++) {
						try {
							array = mJsonArray.getJSONArray(i);
							int length = array.length();
							String[] str2 = null;
							String string = null;
							NaviItem item = null;
							ArrayList<NaviItem> list = new ArrayList<NaviItem>();
							for (int j = 0; j < length; j++) {
								string = array.getString(j);
								str2 = string.split("@");
								if (str2 != null) {
									item = new NaviItem();
									item.title = str2[0];
									if (str2.length > 1) {
										item.url = str2[1];
									}
									if (str2.length > 2) {
										item.color = Utils.getColor(str2[2]);
									} else {
										item.color = -1;
									}
									if (j == 0) {
										item.color = getResources().getColor(
												R.color.purple_blue);
									}
									list.add(item);
								}
							}
							if (list != null && list.size() > 0) {
								itemView = (NaviItemLayout) getActivity()
										.getLayoutInflater()
										.inflate(R.layout.navi_item_layout,
												null);
								itemView.setContent(list);
								itemView.setOnNaviItemClickListener(mNaviItemClickListener);
								layout.addView(itemView);
							}
						} catch (JSONException e) {
							e.printStackTrace();
							break;
						}
					}
				}
				if (layout.getChildCount() == 0) {
					layout.setVisibility(View.GONE);
				}
			};

		}.execute();

		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.navi_header_1:
				startActivity(new Intent(getActivity(),
						DownloadManagerActivity.class));
				break;
			case R.id.navi_header_2:
				break;
			case R.id.navi_header_3:
				break;
			case R.id.navi_header_4:
				break;
			case R.id.navi_header_5:
				break;
			case R.id.navi_header_6:
				break;
			case R.id.navi_header_7:
				break;
			case R.id.navi_header_8:
				break;
			default:
				break;
		}
	}

}
