package com.fujun.browser.fragment;

import java.util.ArrayList;
import java.util.List;

import com.fujun.browser.view.TableView;
import com.kukuai.daohang.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class IndexNaviFragmentEvenNew extends Fragment {
	
	private TableView tableView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		final View view = inflater.inflate(R.layout.index_nav_layout_new, null, false);
		
		tableView = (TableView) view.findViewById(R.id.table);
		List<String[]> contents = new ArrayList<String[]>();
		contents.add(new String[]{"", "123"});
		contents.add(new String[]{"", "456"});
		contents.add(new String[]{"", "789"});
		contents.add(new String[]{"", "123"});
		contents.add(new String[]{"", "456"});
		contents.add(new String[]{"", "789"});
		contents.add(new String[]{"", "123"});
		contents.add(new String[]{"", "456"});
		contents.add(new String[]{"", "789"});
		contents.add(new String[]{"", "123"});
		
		tableView.setContents(contents);
		
		return view;
	}
}