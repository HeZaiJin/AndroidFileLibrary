package com.hand.document.provider;

import android.database.Cursor;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import androidx.annotation.Nullable;
import com.hand.document.io.Volume;

import java.io.FileNotFoundException;
import java.util.List;

public class MediaDocumentsProvider extends StorageProvider {

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        return null;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        return null;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        return null;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, @Nullable String[] projection, @Nullable Bundle queryArgs) throws FileNotFoundException {
        return null;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, @Nullable CancellationSignal signal) throws FileNotFoundException {
        return null;
    }

    @Override
    public void onStorageVolumeChanged(List<Volume> newVolumes) {

    }

    @Override
    public boolean onCreate() {
        return false;
    }
}
