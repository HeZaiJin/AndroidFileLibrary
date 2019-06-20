package com.hand.document.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.hand.document.BuildConfig;
import com.hand.document.core.RootInfo;
import com.hand.document.io.IoUtils;

import java.util.ArrayList;
import java.util.List;

public class Providers {

    private static final Uri EXTERNAL_STORAGE_ROOT_URI = Uri.parse("content://" + BuildConfig.APPLICATION_ID + ".externalstorage.documents/root");


    /**
     * @param context
     * @return
     */
    public static List<RootInfo> getLocalRoots(Context context) {
        List<RootInfo> roots = new ArrayList<>();
        Cursor query = context.getContentResolver().query(EXTERNAL_STORAGE_ROOT_URI, null, null, null, null);
        if (null != query && !query.isClosed()) {
            while (query.moveToNext()) {
                RootInfo info = new RootInfo();
                info.setRootId(query.getString(query.getColumnIndex(DocumentsContract.Root.COLUMN_ROOT_ID)));
                info.setFlags(query.getInt(query.getColumnIndex(DocumentsContract.Root.COLUMN_FLAGS)));
                info.setTitle(query.getString(query.getColumnIndex(DocumentsContract.Root.COLUMN_TITLE)));
                info.setDocId(query.getString(query.getColumnIndex(DocumentsContract.Root.COLUMN_DOCUMENT_ID)));
                info.setPath(query.getString(query.getColumnIndex(DocumentsContract.Root.COLUMN_PATH)));
                info.setAvailableBytes(query.getLong(query.getColumnIndex(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES)));
                info.setCapacityBytes(query.getLong(query.getColumnIndex(DocumentsContract.Root.COLUMN_CAPACITY_BYTES)));
                info.setAuthority(ExternalStorageProvider.AUTHORITY);
                roots.add(info);
            }
            IoUtils.closeQuietly(query);
        }
        return roots;
    }
}
