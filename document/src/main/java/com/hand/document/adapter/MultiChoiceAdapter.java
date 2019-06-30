package com.hand.document.adapter;

import android.os.Build;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Checkable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public abstract class MultiChoiceAdapter<VH extends BaseHolder> extends BaseRecyclerAdapter<VH> {

    private final MultiChoiceHelper mMultiChoiceHelper;

    public MultiChoiceAdapter() {
        mMultiChoiceHelper = new MultiChoiceHelper(this);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        super.onBindViewHolder(holder, position);
        updateCheckedState(holder.itemView, position);
        holder.setEditState(isEditing());
        holder.setItemChecked(mMultiChoiceHelper.isItemChecked(position));
    }

    public void setMultiChoiceListener(MultiChoiceHelper.MultiChoiceListener listener) {
        mMultiChoiceHelper.setListener(listener);
    }

    public boolean isEditing() {
        return mMultiChoiceHelper.isEditing();
    }

    /*public void toggleItemChecked(int position) {
        mMultiChoiceHelper.toggleItemChecked(position, true);
    }*/

    public void exitEditing() {
        mMultiChoiceHelper.clearChoices();
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (isEditing()) {
            int position = getAdapterPosition(v);
            if (position != RecyclerView.NO_POSITION) {
                mMultiChoiceHelper.toggleItemChecked(position, true);
                if (!isEditing()) {
                    notifyDataSetChanged();
                }
            }
        } else {
            super.onClick(v);
        }
    }

    public void selectAll(boolean selectAll) {
        mMultiChoiceHelper.setSelectAll(selectAll);
    }

    public boolean isSelectAll() {
        return mMultiChoiceHelper.isSelectAll();
    }

    @Override
    public boolean onLongClick(View v) {
        if (isEditing()) {
            return super.onLongClick(v);
        }
        int position = getAdapterPosition(v);
        if (position != NO_POSITION) {
            mMultiChoiceHelper.setItemChecked(position, true, true);
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    public void updateCheckedState(View view, int position) {
        final boolean isChecked = mMultiChoiceHelper.isItemChecked(position);
        if (view instanceof Checkable) {
            ((Checkable) view).setChecked(isChecked);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            view.setActivated(isChecked);
        }
    }

    public int getCheckItemCount() {
        return mMultiChoiceHelper.getCheckedItemCount();
    }

    public SparseBooleanArray getCheckedItemPositions() {
        return mMultiChoiceHelper.getCheckedItemPositions();
    }

//    public ArrayList<T> getCheckItems() {
//        SparseBooleanArray checkedItemPositions = getCheckedItemPositions();
//        ArrayList<T> checkedItems = new ArrayList();
//        int size = checkedItemPositions.size();
//        for (int i = 0; i < size; i++) {
//            if (checkedItemPositions.valueAt(i)) {
//                T item = getItem(checkedItemPositions.keyAt(i));
//                if (null != item) {
//                    checkedItems.add(item);
//                }
//            }
//        }
//        if (checkedItems.isEmpty()) {
//            return null;
//        }
//        return checkedItems;
//    }

}
