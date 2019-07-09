package com.hand.document.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.IntDef;
import androidx.annotation.LongDef;
import androidx.annotation.StringDef;
import androidx.collection.ArrayMap;
import com.hand.document.BuildConfig;
import com.hand.document.R;
import com.hand.document.core.RootInfo;
import com.hand.document.io.IoUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class Providers {

    private static final Uri EXTERNAL_STORAGE_ROOT_URI = Uri.parse("content://" + BuildConfig.APPLICATION_ID + ".externalstorage.documents/root");
    private static final Uri DOWNLOAD_ROOT_URI = Uri.parse("content://" + BuildConfig.APPLICATION_ID + ".downloads.documents/root");

    private static final Uri MEDIA_ROOT_URI = Uri.parse("content://" + BuildConfig.APPLICATION_ID + ".media.documents/root");


    @StringDef(value = {
            ROOT_LOCAL_STORAGE,
            ROOT_IMAGE,
            ROOT_VIDEO,
            ROOT_AUDIO,
            ROOT_DOCUMENTS,
            ROOT_DOWNLOADS,
            ROOT_APPLICATION
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface RootType {
    }

    public static final String ROOT_LOCAL_STORAGE = "root_locals";
    public static final String ROOT_IMAGE = "root_images";
    public static final String ROOT_VIDEO = "root_videos";
    public static final String ROOT_AUDIO = "root_audios";
    public static final String ROOT_DOCUMENTS = "root_documents";
    public static final String ROOT_DOWNLOADS = "root_downloads";
    public static final String ROOT_APPLICATION = "root_application";

    public static ArrayMap<String, RootInfo> MEDIA_ROOTS = new ArrayMap<>();

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

    public static RootInfo getRoot(Context context, @RootType String type) {
        if (MEDIA_ROOTS.isEmpty()) {
            Cursor query = context.getContentResolver().query(MEDIA_ROOT_URI, null, null, null, null);
            if (null != query && !query.isClosed()) {
                while (query.moveToNext()) {
                    RootInfo info = new RootInfo();
                    info.setRootId(query.getString(query.getColumnIndex(DocumentsContract.Root.COLUMN_ROOT_ID)));
                    info.setFlags(query.getInt(query.getColumnIndex(DocumentsContract.Root.COLUMN_FLAGS)));
                    info.setTitle(query.getString(query.getColumnIndex(DocumentsContract.Root.COLUMN_TITLE)));
                    info.setDocId(query.getString(query.getColumnIndex(DocumentsContract.Root.COLUMN_DOCUMENT_ID)));
                    info.setAuthority(MediaDocumentsProvider.AUTHORITY);
                    MEDIA_ROOTS.put(info.getRootId(), info);
                }
                IoUtils.closeQuietly(query);
            }
        }

        return MEDIA_ROOTS.get(type);
    }
}
