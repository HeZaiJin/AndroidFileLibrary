package com.hand.document.core;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.SparseArray;

public class DocumentState implements Parcelable {

    public DocumentStack mStack = new DocumentStack();
    public RootInfo mRoot;

    public ArrayMap<String, SparseArray<Parcelable>> mDisplayState = new ArrayMap<>();

    public DocumentState(RootInfo info) {
        mRoot = info;
    }

    protected DocumentState(Parcel in) {
    }

    public static final Creator<DocumentState> CREATOR = new Creator<DocumentState>() {
        @Override
        public DocumentState createFromParcel(Parcel in) {
            return new DocumentState(in);
        }

        @Override
        public DocumentState[] newArray(int size) {
            return new DocumentState[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public void pushStack(DocumentInfo documentInfo) {
        mStack.push(documentInfo);
    }

    public DocumentInfo popStack() {
        return mStack.pop();
    }

    public DocumentInfo peekStack() {
        return mStack.peek();
    }

    public int getStackSize() {
        return mStack.size();
    }

    public void clearStack() {
        mStack.clear();
    }

    public void saveDisplayState(String key, SparseArray<Parcelable> parcelable) {
        mDisplayState.put(key, parcelable);
    }

    public SparseArray<Parcelable> getDisplayState(String key) {
        return mDisplayState.remove(key);
    }
}
