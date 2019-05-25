package com.hand.file.ui.widget

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hand.file.R
import java.util.*

class FolderAdapter : RecyclerView.Adapter<FolderAdapter.Holder>(), View.OnClickListener {
    private val mInfoList = ArrayList<FolderNavigationBar.NavigationInfo>()
    private var mListener: FolderNavigationBar.NavigateListener? = null

    fun setNavigateListener(listener: FolderNavigationBar.NavigateListener) {
        this.mListener = listener
    }

    override fun onClick(v: View) {
        val tag = v.tag as FolderNavigationBar.NavigationInfo
        val index = tag.mIndex
        val end = itemCount - 1
        if (index == end) {
            return
        }
        val start = index + 1
        mListener!!.onNavigate(tag.mInfo)
        mInfoList.subList(start, itemCount).clear()
        notifyDataSetChanged()
    }

    fun addInfo(info: FolderNavigationBar.NavigationInfo) {
        mInfoList.add(info)
        notifyItemInserted(mInfoList.size - 1)
    }

    fun backPressed(): Boolean {
        val end = itemCount - 1
        if (end > 0) {
            val into = end - 1
            val info = mInfoList[into]
            if (null != mListener) {
                mInfoList.removeAt(end)
                notifyItemRemoved(into)
                mListener!!.onNavigate(info.mInfo)
                return true
            }
        }
        return false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val content = LayoutInflater.from(parent.context).inflate(R.layout.item_folder_navigation, parent, false)
        content.setOnClickListener(this)
        return Holder(content)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val title = mInfoList[position].mTitle
        holder.mTitle.text = title
        holder.itemView.tag = mInfoList[position]
        if (position >= itemCount - 1) {
            holder.mImageView.visibility = View.INVISIBLE
            holder.mTitle.setBackgroundColor(Color.TRANSPARENT)
        } else {
            holder.mImageView.visibility = View.VISIBLE
            holder.mTitle.setBackgroundResource(R.drawable.bg_navigation_item)
        }
    }

    override fun getItemCount(): Int {
        return mInfoList.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTitle: TextView = itemView.findViewById(R.id.title)
        var mImageView: ImageView = itemView.findViewById(R.id.image)
    }

}