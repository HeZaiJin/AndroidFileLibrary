package com.hand.document.adapter;

import android.database.Cursor;
import android.util.SparseBooleanArray;
import androidx.annotation.NonNull;

import java.util.ArrayList;

public abstract class RecyclerCursorAdapter<T, VH extends BaseHolder<T>> extends MultiChoiceAdapter<VH> {

    private Cursor mCursor;
    private int mCursorCount;
    private boolean isLoaded = false;

    public void swapResult(Cursor cursor) {
        isLoaded = true;
        mCursor = cursor;
        mCursorCount = mCursor != null ? mCursor.getCount() : 0;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mCursorCount;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.setData(getItem(position));
    }

    public T getItem(int position) {
        if (position < mCursorCount) {
            mCursor.moveToPosition(position);
            return obtainItem(mCursor);
        } else {
            return null;
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public abstract T obtainItem(Cursor cursor);

    @Override
    public boolean isEmpty() {
        return isLoaded && mCursorCount == 0;
    }

    public ArrayList<T> getCheckItems() {
        SparseBooleanArray checkedItemPositions = getCheckedItemPositions();
        int size = checkedItemPositions.size();
        ArrayList<T> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (checkedItemPositions.valueAt(i)) {
                list.add(getItem(checkedItemPositions.keyAt(i)));
            }
        }
        return list;
    }
}
