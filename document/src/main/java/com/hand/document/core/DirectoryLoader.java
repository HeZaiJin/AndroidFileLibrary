package com.hand.document.core;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

public class DirectoryLoader extends AsyncTaskLoader<DirectoryResult> {

    public DirectoryLoader(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public DirectoryResult loadInBackground() {
        return null;
    }
}
