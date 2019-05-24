package com.hand.document.util;

import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import com.hand.document.io.Volume;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class StorageUtils {

    public static List<Volume> getVolumes(Context context) {
        List<Volume> currentVolumes = new ArrayList<>();
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        List volumes = null;
        if (BuildUtils.hasPie()) {
            volumes = sm.getStorageVolumes();
        } else {
            try {
                Method getVolumeList = StorageManager.class.getDeclaredMethod("getVolumes");
                volumes = (List<Object>) getVolumeList.invoke(sm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (null != volumes) {
            for (Object volume : volumes) {
                Volume info = new Volume();
                if (BuildUtils.hasPie()) {
                    info.id = ((StorageVolume) volume).getId();
                    info.desc = ((StorageVolume) volume).getDescription(context);
                    info.primary = ((StorageVolume) volume).isPrimary();
                    info.removable = ((StorageVolume) volume).isRemovable();
                    info.path = ((StorageVolume) volume).getPath();
                    info.maxFileSize = ((StorageVolume) volume).getMaxFileSize();
                    info.state = ((StorageVolume) volume).getState();
                } else {

                }
                currentVolumes.add(info);
            }
        }
        return currentVolumes;
    }
}
