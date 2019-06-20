package com.hand.document.adapter;

import android.view.View;

public interface OnItemClickListener {
    /**
     * Fires when recycler view receives a single tap event on any item
     *
     * @param view     tapped view
     * @param position item position in the list
     */
    void onItemClick(View view, int position);
}