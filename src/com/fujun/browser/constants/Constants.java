
package com.fujun.browser.constants;

import com.kukuai.daohang.R;

public class Constants {

	public static final int REQUEST_GET_QRCODE = 10010;
	public static final String EXTRA_QRCODE = "extra_qr_code";

	public static final int[] FAV_DEFAULT_BG = new int[] {
			R.drawable.navi_default_0, R.drawable.navi_default_4, R.drawable.navi_default_1,
			R.drawable.navi_default_3, R.drawable.navi_default_2
	};
	public static final int FAV_DEFAULT_NUM = 5;
	public static final String EXTRA_ADD_FAV_TITLE = "extra_add_fav_title";
	public static final String EXTRA_ADD_FAV_URL = "extra_add_fav_url";

	public static final boolean DEBUG = false;
	public static final boolean CHECK_UPDATE = true;
	public static final String SDCARD_FOLDER = "puzhuo";

	public static final int WIFI_LIMIT_PROGRESS = 50;
	public static final int G_LIMIT_PROGRESS = 10;

	public static final int CHECK_UPDATE_ID = 0;
	public static final int CHECK_LOADING_ID = 1;
	public static final int CHECK_HTML_UPDATE_ID = 2;

	public static final String PREF_NAME = "kukuai_pref";
	public static final String PREF_DEFAULT_FAV = "pref_default_fav";

	public static final long UPDATE_HTML_GAP = 604800000;
	public static final String SDCARD_HTML_FOLDER = SDCARD_FOLDER + "/html";
	public static final String PREF_UPDATE_HTML_LAST_TIME = "update_html_last_time";
	public static final String SHARED_CREATE_ICON = "create_icon";
	public static final String HTML_URL = "http://apkhtml.azwzdh.com/lxreal/html.tar";
	public static final String ASSETS_HTML_URL = "file:///android_asset/index.html";
	public static final String SDCARD_HTML_URL = "content://com.fujun.browser.provider.localhtmlprovider/sdcard/"
			+ SDCARD_HTML_FOLDER + "/index.html";

	public static final int MAX_TAB_COUNT = 8;

	public static final int FAV_ACTIVITY_REQUEST_CODE = 0;
	public static final String FAV_RESULT_EXTRA_URL = "fav_result_extra_url";

	public static final int ADD_OK = 0;
	public static final int ALREADY_EXIST = 1;
	public static final int NAVI_TO_LIMIT = 2;
	public static final int ADD_FAIL = -1;

	public static final String SEARCH_URL = "http://search.hao3608.com/?q=";
	public static final String DOWNLOAD_FOLDER = "KukuaiBrowser";

	public static final String EXTRA_START_DOWNLOAD = "start";
	public static final String EXTRA_PAUSE_DOWNLOAD = "pause";
	public static final String EXTRA_RESUME_DOWNLOAD = "resume";
	public static final String EXTRA_DELETE_DOWNLOAD = "delete";
	public static final String EXTRA_RESTART_DOWNLOAD = "restart";
	public static final String EXTRA_DOWNLOAD_URL = "url";
	public static final String EXTRA_DOWNLOAD_ITEM = "download_item";
	public static final int CONNECT_TIME_OUT = 8000;

	public static final String SHARED_LAST_CHECK_TIME = "last_time_check_update";
	public static final long ALL_DAY_IN_MILLIONS = 86400000;

	public static final String JSON_REMOTE_VERSION = "version";
	public static final String JSON_CONTENT_SIZE = "size";
	public static final String JSON_DESCRIPTION = "description";
	public static final String JSON_DOWNLOAD_URL = "download_url";
	public static final String JSON_RECHECK_URL = "url";
	public static final String JSON_IF_UPDATE = "update";
	public static final String JSON_ERROR_OCCUR = "error";
}
