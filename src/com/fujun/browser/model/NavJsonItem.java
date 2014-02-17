package com.fujun.browser.model;

import java.util.List;

import com.fujun.browser.model.entity.BottomListItem;
import com.fujun.browser.model.entity.Table;

public class NavJsonItem {
	private Table topTable;
	private String adsImageUrl;
	private String adsUrl;
	
	private List<BottomListItem> bottomList;

	public Table getTopTable() {
		return topTable;
	}

	public void setTopTable(Table topTable) {
		this.topTable = topTable;
	}

	public String getAdsImageUrl() {
		return adsImageUrl;
	}

	public void setAdsImageUrl(String adsImageUrl) {
		this.adsImageUrl = adsImageUrl;
	}

	public String getAdsUrl() {
		return adsUrl;
	}

	public void setAdsUrl(String adsUrl) {
		this.adsUrl = adsUrl;
	}

	public List<BottomListItem> getBottomList() {
		return bottomList;
	}

	public void setBottomList(List<BottomListItem> bottomList) {
		this.bottomList = bottomList;
	}
	
	
}
