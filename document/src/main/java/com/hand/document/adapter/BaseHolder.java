package com.hand.document.adapter;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseHolder<T> extends RecyclerView.ViewHolder {
    public BaseHolder(@NonNull View itemView) {
        super(itemView);
    }

    public abstract void setData(T data);

}
