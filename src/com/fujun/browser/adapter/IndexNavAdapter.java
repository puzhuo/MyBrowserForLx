package com.fujun.browser.adapter;

import java.io.File;

import com.kukuai.daohang.R;
import com.fujun.browser.constants.Constants;
import com.fujun.browser.model.NavJsonItem;
import com.fujun.browser.model.entity.Table;
import com.fujun.browser.view.FitWidthImageView;
import com.fujun.browser.view.TableContainer;
import com.fujun.browser.view.TableView;
import com.fujun.browser.view.TableView.OnContentClickListener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IndexNavAdapter extends BaseAdapter{
	
	private LayoutInflater inflater;
	private NavJsonItem item;
	private OnContentClickListener onContentClickListener;
	
	public IndexNavAdapter(LayoutInflater inflater, NavJsonItem item, OnContentClickListener onContentClickListener){
		this.inflater = inflater;
		this.item = item;
		this.onContentClickListener = onContentClickListener;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return item.getBottomList().size() + 2;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	@Override
	public int getViewTypeCount(){
		return 3;
	}
	
	@Override
	public int getItemViewType(int position){
		if(position < 2){
			return position;
		}else{
			return 2;
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		switch(getItemViewType(position)){
		case 0:
			if(convertView == null){
				convertView = inflater.inflate(R.layout.index_nav_item_toptable, null, false);
			}
			TableView tableView = (TableView) convertView.findViewById(R.id.table);
			tableView.setContents(item.getTopTable().getContents(), item.getTopTable().getUrls());
			tableView.setOnContentClickListener(onContentClickListener);
			
			break;
		case 1:
			if(convertView == null){
				convertView = inflater.inflate(R.layout.index_nav_item_ads, null ,false);
			}
			final FitWidthImageView adsImage = (FitWidthImageView) convertView.findViewById(R.id.image);
			new AsyncTask<Void, Void, Bitmap>(){
				@Override
				protected Bitmap doInBackground(Void... params){
					Bitmap bitmap = null;
					File image = new File(Environment.getExternalStorageDirectory(), Constants.SDCARD_HTML_FOLDER + "/" + item.getAdsImageUrl());
					if(image.exists()){
						try{
							bitmap = BitmapFactory.decodeFile(image.getPath());
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					
					return bitmap;
				}
				
				@Override
				protected void onPostExecute(Bitmap result){
					if(result != null){
						adsImage.setImageBitmap(result);
						adsImage.setClickable(true);
						adsImage.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v){
								if(onContentClickListener != null){
									onContentClickListener.onContentClick(item.getAdsUrl());
								}
							}
						});
					}
				}
			}.execute();
			
			break;
		case 2:
			if(convertView == null){
				convertView = inflater.inflate(R.layout.index_nav_item_bottomlist, null, false);
			}
			
			if(position == 2){
				convertView.setBackgroundResource(R.drawable.bottomlist_bg_head);
			}else if(position == getCount() - 1){
				convertView.setBackgroundResource(R.drawable.bottomlist_bg_foot);
			}else{
				convertView.setBackgroundResource(R.drawable.bottomlist_bg_middle);
			}
			
			final ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
			new AsyncTask<Void, Void, Bitmap>(){
				@Override
				protected Bitmap doInBackground(Void... params){
					Bitmap bitmap = null;
					File image = new File(Environment.getExternalStorageDirectory(), Constants.SDCARD_HTML_FOLDER + "/" + item.getBottomList().get(position - 2).getIconPath());
					if(image.exists()){
						try{
							bitmap = BitmapFactory.decodeFile(image.getPath());
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					
					return bitmap;
				}
				@Override
				protected void onPostExecute(Bitmap result){
					if(result != null){
						icon.setImageBitmap(result);
					}
				}
			}.execute();
			((TextView) convertView.findViewById(R.id.title)).setText(item.getBottomList().get(position - 2).getTitle());
			((TextView) convertView.findViewById(R.id.subtitle)).setText(item.getBottomList().get(position - 2).getSubTitle());
			
			final LinearLayout tableContainer = (LinearLayout) convertView.findViewById(R.id.table_container);
			if(tableContainer.getChildCount() > 0){
				tableContainer.removeAllViews();
			}
			final Table[] tables = item.getBottomList().get(position - 2).getTables();
			for(int i = 0; i < tables.length; i++){
				if(tables[i].getTitle() != null && tables[i].getTitle().length() > 0){
					View setionTitle = inflater.inflate(R.layout.index_nav_item_bottomlist_item_title, null, false);
					((TextView) setionTitle.findViewById(R.id.setion_title)).setText(tables[i].getTitle());
					
					final int ii = i;
					setionTitle.setClickable(true);
					setionTitle.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v){
							if(onContentClickListener != null && tables[ii].getTitleUrl() != null){
								onContentClickListener.onContentClick(tables[ii].getTitleUrl());
							}
						}
					});
					tableContainer.addView(setionTitle);
				}
				TableView tableViewItem = new TableView(tableContainer.getContext());
				tableViewItem.setContents(tables[i].getContents(), tables[i].getUrls());
				tableViewItem.setOnContentClickListener(onContentClickListener);
				tableContainer.addView(tableViewItem);
			}
			
			final View divider = convertView.findViewById(R.id.divider);
			
			convertView.findViewById(R.id.title_layout).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					if(tableContainer.getVisibility() == View.GONE){
						tableContainer.setVisibility(View.VISIBLE);
						if(position == getCount() - 1){
							divider.setVisibility(View.VISIBLE);
						}
					}else{
						tableContainer.setVisibility(View.GONE);
						if(position == getCount() - 1){
							divider.setVisibility(View.GONE);
						}
					}
				}
			});
			
			
			break;
		}
		return convertView;
	}

}
