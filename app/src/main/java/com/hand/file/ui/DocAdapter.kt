package com.hand.file.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hand.document.adapter.DocumentAdapter

class DocAdapter(layout: Int) : DocumentAdapter<DocHolder>() {

    private var mLayout: Int = layout

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocHolder {
        var view: View = LayoutInflater.from(parent.context).inflate(mLayout, parent, false)
        return DocHolder(view)

    }

    override fun onBindViewHolder(holder: DocHolder, position: Int) {
        super.onBindViewHolder(holder, position)
    }

}