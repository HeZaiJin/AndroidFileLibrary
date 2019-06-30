package com.hand.document.adapter;

import android.util.SparseBooleanArray;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;
import com.hand.document.util.LogUtil;


public class MultiChoiceHelper {

    private static final String TAG = "MultiChoiceHelper";

    private static final int SELECT_NORMAL = 0;
    private static final int SELECT_ALL = 1;
    private static final int SELECT_NONE = 2;

    public interface MultiChoiceListener {
        /**
         * Called when an item is checked or unchecked during selection mode.
         *
         * @param mode     The {@link ActionMode} providing the selection startSupportActionModemode
         * @param position Adapter position of the item that was checked or unchecked
         * @param id       Adapter ID of the item that was checked or unchecked
         * @param checked  <code>true</code> if the item is now checked, <code>false</code>
         *                 if the item is now unchecked.
         */
        //void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked);

        void onItemCheckedStateChanged(int position, boolean checked);

        void onEditingStateChanged(boolean edit);

        void onSelectAll(boolean selectAll);
    }

    private final RecyclerView.Adapter mAdapter;
    private SparseBooleanArray mCheckStates;
    private int mCheckedItemCount = 0;
    private MultiChoiceListener mListener;
    private boolean isEditing = false;
    private int mSelectMode = SELECT_NORMAL;

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
        mSelectMode = SELECT_NORMAL;
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

    public void setSelectAll(boolean selectAll) {
        int mode = selectAll ? SELECT_ALL : SELECT_NONE;
        if (mode != mSelectMode) {
            mSelectMode = mode;
            if (mode == SELECT_NONE) {
                clearChoices();
            } else if (mode == SELECT_ALL) {
                mCheckedItemCount = mAdapter.getItemCount();
                //need approve
                for (int i = 0; i < mCheckedItemCount; i++) {
                    mCheckStates.put(i, true);
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public boolean isSelectAll() {
        return mSelectMode == SELECT_ALL;
    }

    public void setItemChecked(int position, boolean value, boolean notifyChanged) {
        if (isSelectAll() && !value) {
            mSelectMode = SELECT_NORMAL;
            if (null != mListener) {
                mListener.onSelectAll(false);
            }
        }
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
            if (mCheckedItemCount > 0) {
                if (notifyChanged) {
                    mAdapter.notifyItemChanged(position);
                }
                if (null != mListener) {
                    mListener.onItemCheckedStateChanged(position, value);
                }
            }
            if (mCheckedItemCount >= mAdapter.getItemCount()) {
                mSelectMode = SELECT_ALL;
                if (null != mListener) {
                    mListener.onSelectAll(true);
                }
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