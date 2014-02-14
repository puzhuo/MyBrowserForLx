
package com.fujun.browser.provider;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class LocalHtmlProvider extends ContentProvider {

	public static final Uri CONTENT_URI = Uri
			.parse("content://com.fujun.browser.provider.localhtmlprovider");
	public static final String BASE_URI = "content://com.fujun.browser.provider.localhtmlprovider";
	public static final int BASE_URI_LEN = BASE_URI.length();

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getType(Uri uri) {
		String mimetype = uri.getQuery();
		return mimetype == null ? "" : mimetype;
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
		if (!"r".equals(mode)) {
			throw new FileNotFoundException("Bad mode for " + uri + ": " + mode);
		}
		String filename = uri.toString().substring(BASE_URI_LEN);
		return ParcelFileDescriptor.open(new File(filename),
				ParcelFileDescriptor.MODE_READ_ONLY);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}
}
