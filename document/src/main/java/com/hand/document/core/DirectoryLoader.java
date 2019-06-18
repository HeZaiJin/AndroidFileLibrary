package com.hand.document.core;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.OperationCanceledException;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
import com.hand.document.core.cursor.RootCursorWrapper;
import com.hand.document.io.IoUtils;
import com.hand.document.provider.ContentProviderClientCompat;
import com.hand.document.provider.DocumentsContract;
import com.hand.document.util.LogUtil;

import java.io.FileNotFoundException;

public class DirectoryLoader extends AsyncTaskLoader<DirectoryResult> {

    private static final String TAG = "DirectoryLoader";

    private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

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
        final DirectoryResult result = new DirectoryResult();
        final ContentResolver resolver = getContext().getContentResolver();
        if (null == mDoc) {
            final Uri docUri = DocumentsContract.buildDocumentUri(
                    mRootInfo.getAuthority(), mRootInfo.getDocId());
            try {
                mDoc = DocumentInfo.fromUri(resolver, docUri);
            } catch (FileNotFoundException e) {
                LogUtil.e(TAG, "Failed to query", e);
                result.exception = e;
                return result;
            }
        }
        Uri uri = DocumentsContract.buildChildDocumentsUri(mDoc.authority, mDoc.documentId);
        Cursor cursor = null;
        ContentProviderClient client = null;
        try {
            client = ContentProviderClientCompat.acquireUnstableProviderOrThrow(resolver, mDoc.authority);

            cursor = client.query(
                    uri, null, null, null, /*getQuerySortOrder(result.sortOrder)*/null);
            cursor.registerContentObserver(mObserver);

            cursor = new RootCursorWrapper(uri.getAuthority(), mRootInfo.getRootId(), cursor, -1);

//            cursor = new SortingCursorWrapper(cursor, result.sortOrder);
//
//            if (mType == DirectoryFragment.TYPE_SEARCH) {
//                cursor = new SortingCursorWrapper(cursor, result.sortOrder);
//                // Filter directories out of search results, for now
//                cursor = new FilteringCursorWrapper(cursor, null, SEARCH_REJECT_MIMES);
//            } else {
//                // Normal directories should have sorting applied
//                cursor = new SortingCursorWrapper(cursor, result.sortOrder);
//            }

            result.cursor = cursor;
        } catch (Exception e) {
            Log.w(TAG, "Failed to query", e);
            result.exception = e;
        } finally {
            synchronized (this) {
                mSignal = null;
            }
            ContentProviderClientCompat.releaseQuietly(client);
        }


        return result;
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
    protected void onStartLoading() {
        if (mResult != null) {
            deliverResult(mResult);
        }
        if (takeContentChanged() || mResult == null) {
            forceLoad();
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

    public final class ForceLoadContentObserver extends ContentObserver {
        public ForceLoadContentObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            onContentChanged();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            final String path = null != uri ? uri.getPath() : "";
            if (!TextUtils.isEmpty(path)) {
                return;
            }
            super.onChange(selfChange, uri);
        }
    }
}
