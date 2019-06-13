package com.hand.document.provider;

import com.hand.document.io.Volume;

import java.util.List;

public abstract class StorageProvider extends DocumentsProvider implements StorageVolumeProvider.Observer {

    public List<Volume> getStorageVolumes() {
        return StorageVolumeProvider.get(getContext()).getStorageVolumes();
    }
}
