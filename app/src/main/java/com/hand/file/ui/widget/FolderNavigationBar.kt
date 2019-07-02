package com.hand.file.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * @author HaoZhang.
 * @date 2018/8/19.
 * @email handhaozhang@gmail.com
 */
class FolderNavigationBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    RecyclerView(context, attrs, defStyle) {
    private var mAdapter: FolderAdapter? = null

    init {
        initial()
    }

    private fun initial() {
        mAdapter = FolderAdapter()
        this.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        this.adapter = mAdapter
    }

    fun setNavigateListener(listener: NavigateListener<Any>): FolderNavigationBar {
        this.mAdapter!!.setNavigateListener(listener)
        return this
    }

    fun openFolder(info: Any, title: String) {
        val navigationInfo = NavigationInfo()
        navigationInfo.mInfo = info
        navigationInfo.mTitle = title
        navigationInfo.mIndex = this.mAdapter!!.itemCount
        this.mAdapter!!.addInfo(navigationInfo)
        scrollToPosition(mAdapter!!.itemCount - 1)
    }

    class NavigationInfo {
        var mIndex: Int = 0
        var mInfo: Any? = null
        var mTitle: String? = null
    }

    interface NavigateListener<T> {
        fun onNavigate(info: T)
    }

    fun onBackPressed(): Boolean {
        return mAdapter!!.backPressed()
    }
}
