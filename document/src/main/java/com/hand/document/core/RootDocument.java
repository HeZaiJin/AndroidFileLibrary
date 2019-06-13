package com.hand.document.core;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RootDocument implements Parcelable, Comparable<RootDocument> {

    @IntDef(flag = false, value = {
            TYPE_IMAGES,
            TYPE_VIDEO,
            TYPE_AUDIO,
            TYPE_RECENT,
            TYPE_DOWNLOADS,
            TYPE_LOCAL,
            TYPE_SD,
            TYPE_USB,
            TYPE_OTHER,
            TYPE_APP
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface RootType {
    }

    public static final int TYPE_LOCAL = 1;
    public static final int TYPE_SD = 2;
    public static final int TYPE_USB = 3;

    public static final int TYPE_RECENT = 4;
    public static final int TYPE_IMAGES = 5;
    public static final int TYPE_VIDEO = 6;
    public static final int TYPE_AUDIO = 7;
    public static final int TYPE_DOWNLOADS = 8;
    public static final int TYPE_APP = 9;
    public static final int TYPE_OTHER = 10;


    public String authority;
    public String rootId;
    public int flags;
    public int icon;
    public String title;
    public long availableBytes;
    public String mimeTypes;


    protected RootDocument(Parcel in) {
    }

    public static final Creator<RootDocument> CREATOR = new Creator<RootDocument>() {
        @Override
        public RootDocument createFromParcel(Parcel in) {
            return new RootDocument(in);
        }

        @Override
        public RootDocument[] newArray(int size) {
            return new RootDocument[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int compareTo(RootDocument o) {
        return 0;
    }
}
