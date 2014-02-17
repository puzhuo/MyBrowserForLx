package com.fujun.browser.fragment;

import com.fujun.browser.view.TableView;
import com.kukuai.daohang.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class IndexNaviFragmentEvenNew extends Fragment {
	
	private TableView tableView;
	
	private ImageView adsImage;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		final View view = inflater.inflate(R.layout.index_nav_layout_new, null, false);
		
		tableView = (TableView) view.findViewById(R.id.table);
		
		//tableView.setContents(contents);
		
		return view;
	}
}