package com.hand.document.core;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;

public class RootInfo implements Parcelable, Durable {

    private static final int VERSION_DROP_TYPE = 1;

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

    @Override
    public void reset() {
        rootId = null;
        authority = null;
        flags = -1;
        title = null;
        docId = null;
        path = null;
        available_bytes = -1;
        capacity_bytes = -1;
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        final int version = in.readInt();
        switch (version) {
            case VERSION_DROP_TYPE:
                rootId = DurableUtils.readNullableString(in);
                authority = DurableUtils.readNullableString(in);
                flags = in.readInt();
                title = DurableUtils.readNullableString(in);
                docId = DurableUtils.readNullableString(in);
                path = DurableUtils.readNullableString(in);
                available_bytes = in.readLong();
                available_bytes = in.readLong();
                capacity_bytes = in.readLong();
                break;
            default:
                throw new ProtocolException("Unknown version " + version);
        }
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(VERSION_DROP_TYPE);
        DurableUtils.writeNullableString(out, rootId);
        DurableUtils.writeNullableString(out, authority);
        out.writeInt(flags);
        DurableUtils.writeNullableString(out, title);
        DurableUtils.writeNullableString(out, docId);
        DurableUtils.writeNullableString(out, path);
        out.writeLong(available_bytes);
        out.writeLong(capacity_bytes);
    }
}
