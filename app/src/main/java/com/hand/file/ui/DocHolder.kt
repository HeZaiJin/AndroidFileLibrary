package com.hand.file.ui

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.hand.document.adapter.BaseHolder
import com.hand.document.core.DocumentInfo
import com.hand.document.provider.IconProvider
import com.hand.document.util.GlideApp
import com.hand.document.util.LogUtil
import com.hand.file.R

/**
 * TODO MD UI
 */
class DocHolder(itemView: View) : BaseHolder<DocumentInfo>(itemView) {

    companion object {
        val TAG = "DocHolder"
    }

    private var image: ImageView? = itemView.findViewById(R.id.image)
    private var title: TextView? = itemView.findViewById(R.id.title)
    private var desc: TextView? = itemView.findViewById(R.id.desc)
    private var checkBox: CheckBox? = itemView.findViewById(R.id.checkbox)
    private var more: View? = itemView.findViewById(R.id.more)

    override fun setData(data: DocumentInfo?) {
        data?.let {
            this.title!!.text = data.displayName
            this.desc!!.text = data.summary
            this.image?.apply {
                LogUtil.d(TAG, "setImageDrawable with ${data.documentId} , mimeType ${data.mimeType}")
                var uri = data.icon ?: data.path
                GlideApp.with(this).load(uri).placeholder(IconProvider.getDefDrawable(context, data.mimeType)).into(this)
            }
        }
    }

    private fun setSummary(data: DocumentInfo, desc: TextView) {
        if (data.isDirectory) {

        } else {

        }
    }

    override fun setEditState(editing: Boolean) {
        checkBox?.let {
            it.visibility = if (editing) View.VISIBLE else View.INVISIBLE
            if (!editing) it.isChecked = false
        }
        more?.let {
            it.visibility = if (editing) View.INVISIBLE else View.VISIBLE
        }
    }

    override fun setItemChecked(checked: Boolean) {
        checkBox?.let {
            it.isChecked = checked
        }
    }

}