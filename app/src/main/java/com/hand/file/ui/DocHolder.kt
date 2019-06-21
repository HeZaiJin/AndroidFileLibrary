package com.hand.file.ui

import android.view.View
import android.widget.TextView
import com.hand.document.adapter.BaseHolder
import com.hand.document.core.DocumentInfo
import com.hand.file.R

/**
 * TODO MD UI
 */
class DocHolder(itemView: View) : BaseHolder<DocumentInfo>(itemView) {

    private var title: TextView = itemView.findViewById(R.id.title)
    private var desc: TextView = itemView.findViewById(R.id.desc)

    override fun setData(data: DocumentInfo?) {
        this.title.text = data!!.displayName
        this.desc.text = data!!.summary
    }
}