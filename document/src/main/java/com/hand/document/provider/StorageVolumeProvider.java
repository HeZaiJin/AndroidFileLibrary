package com.hand.document.provider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.hand.document.io.Volume;
import com.hand.document.util.LogUtil;
import com.hand.document.util.StorageUtils;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.*;

public class StorageVolumeProvider extends BroadcastReceiver {

    private static final String TAG = "StorageVolumeProvider";

    private static StorageVolumeProvider sInstance;

    private Context mContext;
    private List<Volume> mStorageVolume;
    private List<Observer> mObservers;

    private StorageVolumeProvider(Context context) {
        init(context);
    }

    public static StorageVolumeProvider get(Context context) {
        if (null == sInstance) {
            synchronized (StorageVolumeProvider.class) {
                if (null == sInstance) {
                    sInstance = new StorageVolumeProvider(context);
                }
            }
        }
        return sInstance;
    }

    public void init(Context context) {
        mContext = context;
        mObservers = new ArrayList<>();
        mStorageVolume = new ArrayList<>(StorageUtils.getVolumes(mContext));
        registerBroadcast();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d(TAG, String.format("receive intent: %s", intent));
    }

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_MEDIA_MOUNTED);
        filter.addAction(ACTION_MEDIA_UNMOUNTED);
        filter.addAction(ACTION_MEDIA_REMOVED);
        filter.addAction(ACTION_MEDIA_EJECT);
        mContext.registerReceiver(this, filter);
    }

    public List<Volume> getStorageVolumes() {
        if (null == mStorageVolume) {
            mStorageVolume = new ArrayList<>(StorageUtils.getVolumes(mContext));
        }
        return new ArrayList<>(mStorageVolume);
    }

    private void notifyObserver() {
        LogUtil.d(TAG, "notifyObserver");
        List<Volume> volumes = getStorageVolumes();
        for (Observer observer : mObservers) {
            observer.onStorageVolumeChanged(volumes);
        }
    }

    public boolean registerObserver(Observer observer) {
        if (!mObservers.contains(observer)) {
            return mObservers.add(observer);
        }
        return false;
    }

    public boolean unRegisterObserver(Observer observer) {
        return mObservers.remove(observer);
    }

    public interface Observer {
        void onStorageVolumeChanged(List<Volume> newVolumes);
    }
}
