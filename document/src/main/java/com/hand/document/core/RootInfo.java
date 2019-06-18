package com.hand.document.core;

import android.os.Parcel;
import android.os.Parcelable;

public class RootInfo implements Parcelable {

    private String rootId;
    private String authority;
    private int flags;
    private String title;
    private String docId;
    private String path;
    private long available_bytes;
    private long capacity_bytes;

    public RootInfo() {
    }

    protected RootInfo(Parcel in) {
        rootId = in.readString();
        authority = in.readString();
        flags = in.readInt();
        title = in.readString();
        docId = in.readString();
        path = in.readString();
        available_bytes = in.readLong();
        capacity_bytes = in.readLong();
    }

    public static final Creator<RootInfo> CREATOR = new Creator<RootInfo>() {
        @Override
        public RootInfo createFromParcel(Parcel in) {
            return new RootInfo(in);
        }

        @Override
        public RootInfo[] newArray(int size) {
            return new RootInfo[size];
        }
    };

    public String getRootId() {
        return rootId;
    }

    public void setRootId(String rootId) {
        this.rootId = rootId;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public long getAvailableBytes() {
        return available_bytes;
    }

    public void setAvailableBytes(long available_bytes) {
        this.available_bytes = available_bytes;
    }

    public long getCapacityBytes() {
        return capacity_bytes;
    }

    public void setCapacityBytes(long capacity_bytes) {
        this.capacity_bytes = capacity_bytes;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.rootId);
        dest.writeString(this.authority);
        dest.writeInt(this.flags);
        dest.writeString(this.title);
        dest.writeString(this.docId);
        dest.writeString(this.path);
        dest.writeLong(this.available_bytes);
        dest.writeLong(this.capacity_bytes);
    }
}
