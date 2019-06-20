package com.hand.document.adapter;

import android.util.SparseBooleanArray;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hand.document.util.LogUtil;


public class MultiChoiceHelper {

    private static final String TAG = "MultiChoiceHelper";

    public interface MultiChoiceListener {
        void onItemCheckedStateChanged(int position, boolean checked);

        void onEditingStateChanged(boolean edit);
    }

    private final RecyclerView.Adapter mAdapter;
    private SparseBooleanArray mCheckStates;
    private int mCheckedItemCount = 0;
    private MultiChoiceListener mListener;
    private boolean isEditing = false;

    public MultiChoiceHelper(@NonNull RecyclerView.Adapter adapter) {
        this.mAdapter = adapter;
        mAdapter.registerAdapterDataObserver(new AdapterDataSetObserver());
        mCheckStates = new SparseBooleanArray(0);
    }

    public void setListener(MultiChoiceListener listener) {
        this.mListener = listener;
    }

    public boolean isEditing() {
        return isEditing;
    }

    public int getCheckedItemCount() {
        return mCheckedItemCount;
    }

    public boolean isItemChecked(int position) {
        return mCheckStates.get(position);
    }

    public SparseBooleanArray getCheckedItemPositions() {
        return mCheckStates;
    }

    public void clearChoices() {
        if (mCheckedItemCount > 0) {
            final int start = mCheckStates.keyAt(0);
            final int end = mCheckStates.keyAt(mCheckStates.size() - 1);
            mCheckStates.clear();
            mCheckedItemCount = 0;
            mAdapter.notifyItemRangeChanged(start, end - start + 1);
        }
        if (isEditing) {
            isEditing = false;
            if (null != mListener) {
                mListener.onEditingStateChanged(false);
            }
        }
    }

    public void setItemChecked(int position, boolean value, boolean notifyChanged) {

        boolean oldValue = mCheckStates.get(position);
        mCheckStates.put(position, value);
        if (oldValue != value) {
            if (value) {
                mCheckedItemCount++;
            } else {
                mCheckedItemCount--;
            }

            if (mCheckedItemCount == 1 && !isEditing) {
                isEditing = true;
                LogUtil.d(TAG, "enter edit state");
                if (null != mListener) {
                    mListener.onEditingStateChanged(true);
                }
            } else if (mCheckedItemCount == 0 && isEditing) {
                isEditing = false;
                LogUtil.d(TAG, "exit edit state");
                if (null != mListener) {
                    mListener.onEditingStateChanged(false);
                }
            }

            if (notifyChanged) {
                mAdapter.notifyItemChanged(position);
            }
            if (null != mListener) {
                mListener.onItemCheckedStateChanged(position, value);
            }
        }
    }

    public void toggleItemChecked(int position, boolean notifyChanged) {
        setItemChecked(position, !isItemChecked(position), notifyChanged);
    }

    void confirmCheckedPositions() {
        if (mCheckedItemCount == 0) {
            return;
        }

        final int itemCount = mAdapter.getItemCount();

        if (itemCount == 0) {
            mCheckStates.clear();
            mCheckedItemCount = 0;
            if (isEditing) {
                isEditing = false;
                if (null != mListener) {
                    mListener.onEditingStateChanged(false);
                }
            }
        } else {
            for (int i = mCheckStates.size() - 1; (i >= 0) && (mCheckStates.keyAt(i) >= itemCount); i--) {
                if (mCheckStates.valueAt(i)) {
                    mCheckedItemCount--;
                }
                mCheckStates.delete(mCheckStates.keyAt(i));
            }
            if (mCheckedItemCount == 0 && isEditing) {
                isEditing = false;
                if (null != mListener) {
                    mListener.onEditingStateChanged(false);
                }
            }
        }
    }

    class AdapterDataSetObserver extends RecyclerView.AdapterDataObserver {

        @Override
        public void onChanged() {
            confirmCheckedPositions();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            confirmCheckedPositions();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            confirmCheckedPositions();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            confirmCheckedPositions();
        }
    }
}