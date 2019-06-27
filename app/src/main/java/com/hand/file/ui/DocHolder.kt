package com.hand.file.ui

import android.view.View
import android.widget.CheckBox
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
    private var checkBox: CheckBox = itemView.findViewById(R.id.checkbox)

    override fun setData(data: DocumentInfo?) {
        this.title.text = data!!.displayName
        this.desc.text = data!!.summary
    }

    override fun setEditState(editing: Boolean) {
        checkBox.visibility = if (editing) View.VISIBLE else View.INVISIBLE
        if (!editing) checkBox.isChecked = false
    }

    override fun setItemChecked(checked: Boolean) {
        checkBox.isChecked = checked
    }

}