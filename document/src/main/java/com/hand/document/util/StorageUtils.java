package com.hand.document.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import com.hand.document.io.Volume;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class StorageUtils {

    public static final String PATH_EMULATED = "storage/emulated/0";

    @SuppressLint("NewApi")
    public static List<Volume> getVolumes(Context context) {
        List<Volume> currentVolumes = new ArrayList<>();
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> volumes = new ArrayList<>();
        if (BuildUtils.hasNougat()) {
            volumes = sm.getStorageVolumes();
        } else {
            try {
                Method getVolumeList = StorageManager.class.getDeclaredMethod("getVolumeList");
                Object result = getVolumeList.invoke(sm);
                if (result instanceof List) {
                    volumes.addAll((Collection<? extends StorageVolume>) result);
                } else if (result.getClass().isArray()) {
                    volumes.addAll(Arrays.asList((StorageVolume[]) result));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (null != volumes) {
            for (StorageVolume volume : volumes) {
                Volume info = new Volume();
                info.id = volume.getUuid();
                info.desc = volume.getDescription(context);
                info.isPrimary = volume.isPrimary();
                info.removable = volume.isRemovable();
                info.path = volume.getPath();
                info.maxFileSize = volume.getMaxFileSize();
                if (info.maxFileSize <= 0) {
                    File file = new File(info.path);
                    if (file.exists()) {
                        info.maxFileSize = file.getTotalSpace();
                    }
                }
                info.state = volume.getState();
                if (info.isPrimary && !info.removable && info.path.contains("emulated")) {
                    info.path = PATH_EMULATED;
                }
                currentVolumes.add(info);
            }
        }
        return currentVolumes;
    }
}
