package com.hand.document.core;

import android.database.Cursor;
import com.hand.document.io.IoUtils;

public class DirectoryResult implements AutoCloseable {
    public Cursor cursor;
    public Exception exception;


    @Override
    public void close() {
        IoUtils.closeQuietly(cursor);
        cursor = null;
    }
}