package com.fujun.browser.fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fujun.browser.activity.HomeActivity;
import com.fujun.browser.adapter.IndexNavAdapter;
import com.fujun.browser.constants.Constants;
import com.fujun.browser.utils.NavJSONUtil;
import com.kukuai.daohang.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class IndexNaviFragmentEvenNew extends Fragment {
	
	private ListView listView;
	private IndexNavAdapter adapter;
	
	private final String JSON_FILE_NAME = "/nav.json";

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		final View view = inflater.inflate(R.layout.index_nav_layout_new, null, false);
		
		new AsyncTask<Void, Void, String>(){
			@Override
			protected String doInBackground(Void... params){
				StringBuffer buffer = new StringBuffer();
				InputStreamReader reader = null;
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
					return buffer.toString();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(String result){
				listView = (ListView) view.findViewById(R.id.list);
				adapter = new IndexNavAdapter(inflater, NavJSONUtil.getNavJsonItem(result), ((HomeActivity) IndexNaviFragmentEvenNew.this.getActivity()));
				listView.setAdapter(adapter);
			}
		}.execute();
		
		return view;
	}
}