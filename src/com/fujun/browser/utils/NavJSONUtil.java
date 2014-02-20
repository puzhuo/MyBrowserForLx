package com.fujun.browser.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fujun.browser.model.NavJsonItem;
import com.fujun.browser.model.entity.BottomListItem;
import com.fujun.browser.model.entity.Table;

public class NavJSONUtil {
	public static NavJsonItem getNavJsonItem(String json){
		NavJsonItem item = null;
		
		try{
			JSONObject jsonObject = new JSONObject(json);
			item = new NavJsonItem();
			
			//toptable
			item.setTopTable(getTableFromJSON(jsonObject.getJSONArray("toptable")));
			
			//ads
			item.setAdsImageUrl(jsonObject.getJSONObject("ads").getString("image_url"));
			item.setAdsUrl(jsonObject.getJSONObject("ads").getString("url"));
			
			//bottomlist
			List<BottomListItem> bottomListItems = new ArrayList<BottomListItem>();
			JSONArray bottomListJSON = jsonObject.getJSONArray("bottomList");
			int length = bottomListJSON.length();
			for(int i = 0; i < length; i++){
				BottomListItem bottomListItem = new BottomListItem();
				bottomListItem.setTitle(bottomListJSON.getJSONObject(i).getString("title"));
				bottomListItem.setSubTitle(bottomListJSON.getJSONObject(i).getString("subtitle"));
				bottomListItem.setIconPath(bottomListJSON.getJSONObject(i).getString("icon"));
				JSONArray contentJSON = bottomListJSON.getJSONObject(i).getJSONArray("content");
				int contentLength = contentJSON.length();
				Table[] tables = new Table[contentLength];
				for(int j = 0; j < contentLength; j++){
					tables[j] = getTableFromJSON(contentJSON.getJSONObject(j).getJSONArray("table"));
					tables[j].setTitle(contentJSON.getJSONObject(j).optString("title"));
					tables[j].setTitleUrl(contentJSON.getJSONObject(j).optString("title_url"));
				}
				
				bottomListItem.setTables(tables);
				
				bottomListItems.add(bottomListItem);
			}
			
			item.setBottomList(bottomListItems);
			
		}catch(JSONException e){
			e.printStackTrace();
		}
		
		return item;
	}
	
	private static Table getTableFromJSON(JSONArray object){
		Table result = null;
		
		try{
			result = new Table();
			int length = object.length();
			String[] contents = new String[length];
			String[] urls = new String[length];
			for(int i = 0; i < length; i++){
				contents[i] = object.getJSONObject(i).getString("title");
				urls[i] = object.getJSONObject(i).getString("url");
			}
			result.setContents(contents);
			result.setUrls(urls);
			
		}catch(JSONException e){
			e.printStackTrace();
		}
		
		return result;
	}
}
