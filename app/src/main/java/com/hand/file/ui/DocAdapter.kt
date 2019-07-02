package com.hand.file.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hand.document.adapter.DocumentAdapter
import com.hand.file.R

class DocAdapter(layout: Int) : DocumentAdapter<DocHolder>() {

    companion object {
        const val EMPTY_TYPE = 1001
    }

    private var mLayout: Int = layout

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocHolder {
        if (viewType == EMPTY_TYPE) {
            var view: View = LayoutInflater.from(parent.context).inflate(R.layout.document_empty, parent, false)
            return DocHolder(view)
        }
        var view: View = LayoutInflater.from(parent.context).inflate(mLayout, parent, false)
        return DocHolder(view)

    }

    override fun onBindViewHolder(holder: DocHolder, position: Int) {
        if (!isEmpty) {
            super.onBindViewHolder(holder, position)
        }
    }

    override fun getItemCount(): Int {
        return if (isEmpty) 1 else super.getItemCount()
    }

    override fun getItemViewType(position: Int): Int {
        if (isEmpty) {
            return EMPTY_TYPE
        }
        return super.getItemViewType(position)
    }

}