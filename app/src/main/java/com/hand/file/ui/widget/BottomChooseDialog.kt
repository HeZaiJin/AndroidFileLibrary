package com.hand.file.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hand.file.R

class BottomChooseDialog<T>(context: Context) : BottomSheetDialog(context), View.OnClickListener {

    companion object {
        val TAG = "BottomChooseDialog"
    }

    private lateinit var titleView: TextView
    private lateinit var contentView: LinearLayout
    private lateinit var listData: ArrayList<Data<T>>
    var itemClickListener: ItemClickListener<Any>? = null

    fun bindData(list: ArrayList<Data<T>>) {
        listData = list
        setContentView(R.layout.dialog_choose_layout)
        titleView = findViewById<TextView>(R.id.title)!!
        contentView = findViewById<LinearLayout>(R.id.content)!!
        handleData()
    }

    private fun handleData() {
        if (listData.isNullOrEmpty()) {
            return
        }
        var itemLayout = LayoutInflater.from(context).inflate(R.layout.dialog_choose_item_layout, null)
        var index = 0
        for (obj in listData) {
            Holder<T>(itemLayout).apply {
                bindItem(obj.data, obj.title, obj.drawable, this@BottomChooseDialog)
                contentView.addView(itemView)
                index++
            }
        }
    }

    open class Holder<T>(view: View) {
        val itemView = view
        private var icon: ImageView
        private var title: TextView

        init {
            title = itemView.findViewById(R.id.title)
            icon = itemView.findViewById(R.id.image)
        }

        fun bindItem(data: T, title: String, drawable: Drawable?, clickListener: View.OnClickListener) {
            this.title.text = title
            this.icon.setImageDrawable(drawable)
            this.itemView.tag = data
            this.itemView.setOnClickListener(clickListener)
        }
    }

    override fun onClick(v: View?) {
        v?.run {
            tag?.apply {
                itemClickListener?.onItemClick(this)
                dismiss()
            }
        }
    }

    interface ItemClickListener<T> {
        fun onItemClick(data: T)
    }

    class Data<T>(title: String, drawable: Drawable?, data: T) {
        val title = title
        val drawable = drawable
        val data = data
    }

}