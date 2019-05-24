package android.os.storage;

import android.content.Context;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;

import java.io.File;

public final class StorageVolume implements Parcelable {

    protected StorageVolume(Parcel in) {
    }

    public static final Creator<StorageVolume> CREATOR = new Creator<StorageVolume>() {
        @Override
        public StorageVolume createFromParcel(Parcel in) {
            return new StorageVolume(in);
        }

        @Override
        public StorageVolume[] newArray(int size) {
            return new StorageVolume[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }


    /**
     *
     */
    public String getId() {
        return null;
    }

    /**
     * Returns the mount path for the volume.
     *
     * @return the mount path
     */
    public String getPath() {
        return null;
    }

    /**
     * Returns the path of the underlying filesystem.
     *
     * @return the internal path
     */
    public String getInternalPath() {
        return null;
    }

    public File getPathFile() {
        return null;
    }

    /**
     * Returns a user-visible description of the volume.
     *
     * @return the volume description
     */
    public String getDescription(Context context) {
        return null;
    }

    /**
     * Returns true if the volume is the primary shared/external storage, which is the volume
     * backed by {@link Environment#getExternalStorageDirectory()}.
     */
    public boolean isPrimary() {
        return false;
    }

    /**
     * Returns true if the volume is removable.
     *
     * @return is removable
     */
    public boolean isRemovable() {
        return false;
    }

    /**
     * Returns true if the volume is emulated.
     *
     * @return is removable
     */
    public boolean isEmulated() {
        return false;
    }

    /**
     * Returns true if this volume can be shared via USB mass storage.
     *
     * @return whether mass storage is allowed
     */
    public boolean allowMassStorage() {
        return false;
    }

    /**
     * Returns maximum file size for the volume, or zero if it is unbounded.
     *
     * @return maximum file size
     */
    public long getMaxFileSize() {
        return 0;
    }

    public UserHandle getOwner() {
        return null;
    }

    /**
     * Gets the volume UUID, if any.
     */
    public String getUuid() {
        return null;
    }

    /**
     * Parse and return volume UUID as FAT volume ID, or return -1 if unable to
     * parse or UUID is unknown.
     */
    public int getFatVolumeId() {
        return 0;
    }

    public String getUserLabel() {
        return null;
    }

    /**
     * Returns the current state of the volume.
     *
     * @return one of {@link Environment#MEDIA_UNKNOWN}, {@link Environment#MEDIA_REMOVED},
     * {@link Environment#MEDIA_UNMOUNTED}, {@link Environment#MEDIA_CHECKING},
     * {@link Environment#MEDIA_NOFS}, {@link Environment#MEDIA_MOUNTED},
     * {@link Environment#MEDIA_MOUNTED_READ_ONLY}, {@link Environment#MEDIA_SHARED},
     * {@link Environment#MEDIA_BAD_REMOVAL}, or {@link Environment#MEDIA_UNMOUNTABLE}.
     */
    public String getState() {
        return null;
    }
}
