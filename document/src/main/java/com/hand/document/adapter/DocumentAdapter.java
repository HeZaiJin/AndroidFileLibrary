package com.hand.document.adapter;

import android.database.Cursor;
import com.hand.document.core.DocumentInfo;

public abstract class DocumentAdapter<VH extends BaseHolder<DocumentInfo>> extends RecyclerCursorAdapter<DocumentInfo, VH> {

    @Override
    public DocumentInfo obtainItem(Cursor cursor) {
        return DocumentInfo.fromDirectoryCursor(cursor);
    }
}
