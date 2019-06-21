package com.hand.document.core;

import android.os.Parcel;
import android.os.Parcelable;

public class DocumentState implements Parcelable {

    public DocumentStack mStack = new DocumentStack();

    public DocumentState() {
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
}
