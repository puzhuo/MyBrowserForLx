
package com.fujun.browser.model;

import java.io.Serializable;

import android.app.Notification;

public class DownloadItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int DOWNLOAD_STATUS_DOWNLOADING = 0;
	public static final int DOWNLOAD_STATUS_PAUSED = 1;
	public static final int DOWNLOAD_STATUS_DOWNLOADED = 2;
	public static final int DOWNLOAD_STATUS_ERROR = 3;
	public static final int DOWNLOAD_STATUS_WAIT = 4;

	public String url;
	public String fileName;
	public String mimeType;
	public long fileSize;
	public long currentSize;
	public int status;
	public Notification notification;

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[ ").append("url = ").append(url)
				.append("; fileName = ").append(fileName)
				.append("; mimeType = ").append(mimeType)
				.append("; notification = ").append(notification)
				.append("; fileSize = ").append(fileSize)
				.append("; currentSize = ").append(currentSize)
				.append("; status = ").append(status).append("] ");
		return buffer.toString();
	}
}
