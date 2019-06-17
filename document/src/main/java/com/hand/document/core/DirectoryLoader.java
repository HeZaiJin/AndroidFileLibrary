package com.hand.document.core;

import android.content.Context;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
import com.hand.document.io.IoUtils;

public class DirectoryLoader extends AsyncTaskLoader<DirectoryResult> {

    private final RootInfo mRootInfo;
    private DocumentInfo mDoc;

    private DirectoryResult mResult;
    private CancellationSignal mSignal;

    public DirectoryLoader(@NonNull Context context, RootInfo root, DocumentInfo documentInfo) {
        super(context);
        mRootInfo = root;
        mDoc = documentInfo;

    }

    @Nullable
    @Override
    public DirectoryResult loadInBackground() {
        synchronized (this) {
            if (isLoadInBackgroundCanceled()) {
                throw new OperationCanceledException();
            }
            mSignal = new CancellationSignal();
        }


        return null;
    }

    @Override
    public void deliverResult(DirectoryResult result) {
        if (isReset()) {
            IoUtils.closeQuietly(result);
            return;
        }
        DirectoryResult oldResult = mResult;
        mResult = result;

        if (isStarted()) {
            super.deliverResult(result);
        }

        if (oldResult != null && oldResult != result) {
            IoUtils.closeQuietly(oldResult);
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(DirectoryResult result) {
        IoUtils.closeQuietly(result);
    }
}
