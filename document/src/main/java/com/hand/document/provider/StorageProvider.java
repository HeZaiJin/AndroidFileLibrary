package com.hand.document.provider;

import com.hand.document.io.Volume;

import java.util.List;

public abstract class StorageProvider extends DocumentsProvider implements StorageVolumeProvider.Observer {

    @Override
    public boolean onCreate() {
        StorageVolumeProvider.get().registerObserver(this);
        return false;
    }

    public List<Volume> getStorageVolumes() {
        return StorageVolumeProvider.get().getStorageVolumes();
    }
}
