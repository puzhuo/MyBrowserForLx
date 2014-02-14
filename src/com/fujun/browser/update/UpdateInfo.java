package com.fujun.browser.update;

import java.io.Serializable;

public class UpdateInfo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6197778003095271658L;
	
	public String remoteVersion;
	public String updateDescription;
	public String updateUrl;
	public long contentLength;
}
