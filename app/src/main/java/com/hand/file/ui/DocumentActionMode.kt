package com.hand.file.ui

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import com.hand.document.core.DocumentInfo
import com.hand.document.core.RootInfo
import com.hand.document.util.LogUtil
import com.hand.file.R

class DocumentActionMode(context: Context, adapter: DocAdapter, root: RootInfo, doc: DocumentInfo, callback: Callback) :
    ActionMode.Callback {

    companion object {
        val TAG = "DocumentActionMode"
    }

    private var context: Context = context
    private var adapter: DocAdapter = adapter
    private val rootInfo: RootInfo = root
    private var documentInfo: DocumentInfo = doc
    private var callback: Callback = callback

    private var shareAble: Boolean = true
    private var dirCount: Int = 0
    private var openAble: Boolean = true
    private var selectAll: Boolean = false

    fun updateSelectAll(selectAll: Boolean) {
        if (this.selectAll == selectAll) {
            return
        }
        LogUtil.d(TAG, "updateSelectAll $selectAll, current selectAll ${this.selectAll}")
        shareAble = false
        openAble = false
        this.selectAll = selectAll
    }


    fun update(actionMode: ActionMode?, position: Int, checked: Boolean) {
        val item = adapter.getItem(position)
        if (item.isDirectory) {
            if (checked) {
                dirCount++
            } else {
                dirCount--
            }
        }
        var requestInvalidate = false
        var canShare = dirCount == 0
        if (shareAble != canShare) {
            shareAble = canShare
            requestInvalidate = true
        }
        var open = dirCount == 0 && adapter.checkItemCount == 1
        if (openAble != open) {
            openAble = open
            requestInvalidate = true
        }
        actionMode?.apply {
            if (requestInvalidate) {
                invalidate()
            } else {
                title = context.resources.getString(R.string.mode_selected_count, adapter.checkItemCount)
            }
        }
    }

    fun dump() {
        LogUtil.d(
            TAG, "current checkedCount ${adapter.checkItemCount}, dirCount $dirCount" +
                    ", shareAble $shareAble , openAbel $openAble"
        )
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.let {
            it.menuInflater.inflate(R.menu.mode_document, menu)
            it.title = context.resources.getString(R.string.mode_selected_count, adapter.checkItemCount)
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        menu?.let {
            var count = adapter.checkItemCount
            mode?.apply {
                title = context.resources.getString(R.string.mode_selected_count, adapter.checkItemCount)
            }
            val share = it.findItem(R.id.menu_share)
            val open = it.findItem(R.id.menu_open_as)
            val info = it.findItem(R.id.menu_info)
            val rename = it.findItem(R.id.menu_rename)
            val selectAll = it.findItem(R.id.menu_select_all)
            rename.isVisible = count == 1
            info.isVisible = count == 1
            open.isVisible = openAble
            share.isVisible = shareAble
            selectAll.setTitle(if (this.selectAll) R.string.menu_select_un_all else R.string.menu_select_all)
            callback.onActionModeCreated()
        }

        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        if (null != item) {
            val handleMenu = callback.onActionMenuClick(item)
            if (handleMenu) {
                mode?.finish()
                return true
            }
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        callback.onActionModeDestroy()
        reset()
    }

    fun reset() {
        dirCount = 0
        selectAll = false
        shareAble = true
        openAble = true
    }

    interface Callback {
        fun onActionModeCreated()
        fun onActionModeDestroy()
        fun onActionMenuClick(item: MenuItem): Boolean
    }
}