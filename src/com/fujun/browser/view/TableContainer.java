package com.fujun.browser.view;

import com.fujun.browser.model.entity.Table;
import com.fujun.browser.view.TableView.OnContentClickListener;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class TableContainer extends LinearLayout{

	public TableContainer(Context context){
		this(context, null);
	}
	
	public TableContainer(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}
	
	public TableContainer(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}
	
	public void setTables(Table[] tables, OnContentClickListener onContentClickListener){
		if(tables.length > 0){
			for(int i = 0; i < tables.length; i++){
				if(tables[i].getTitle().length() > 0){
					
				}
				TableView tableView = new TableView(getContext());
				tableView.setContents(tables[i].getContents(), tables[i].getUrls());
				tableView.setOnContentClickListener(onContentClickListener);
				addView(tableView);
			}
		}
	}
}
