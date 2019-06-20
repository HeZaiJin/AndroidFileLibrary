package com.hand.document.adapter;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hand.document.R;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public abstract class BaseRecyclerAdapter<VH extends BaseHolder> extends RecyclerView.Adapter<VH> implements View.OnClickListener, View.OnLongClickListener {

    private static final int ITEM_TAG_ID = R.id.recycler_item_position;
    private onItemClickListener mItemClickListener;

    public onItemClickListener getItemClickListener() {
        return mItemClickListener;
    }

    public void setItemClickListener(onItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.itemView.setTag(ITEM_TAG_ID, position);
        holder.itemView.setOnClickListener(this);
        holder.itemView.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void onClick(View v) {
        if (null != mItemClickListener) {
            int position = getAdapterPosition(v);
            if (position != -1) {
                mItemClickListener.onItemClick(v, position);
            }
        }
    }

    public int getAdapterPosition(View view) {
        Object tag = view.getTag(ITEM_TAG_ID);
        if (null != tag && tag instanceof Integer) {
            return (int) tag;
        }
        return NO_POSITION;
    }

    public interface onItemClickListener {
        void onItemClick(View view, int position);
    }
}
