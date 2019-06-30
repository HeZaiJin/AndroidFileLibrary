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

    var actionMode: ActionMode? = null

    fun update(position: Int, checked: Boolean) {
        actionMode?.title = context.resources.getString(R.string.mode_selected_count, adapter.checkItemCount)
        //
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

        if (requestInvalidate) {
            actionMode?.invalidate()
        }
    }

    fun dump() {
        LogUtil.d(
            TAG, "current checkedCount ${adapter.checkItemCount}, dirCount $dirCount" +
                    ", shareAble $shareAble , openAbel $openAble"
        )
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode!!.menuInflater.inflate(R.menu.mode_document, menu)
        mode.title = context.resources.getString(R.string.mode_selected_count, adapter.checkItemCount)
        callback.onActionModeCreated()
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        var count = adapter.checkItemCount
        val share = menu!!.findItem(R.id.menu_share)
        val open = menu!!.findItem(R.id.menu_open_as)
        val info = menu!!.findItem(R.id.menu_info)
        val rename = menu!!.findItem(R.id.menu_rename)
        rename!!.isVisible = count == 1
        info!!.isVisible = count == 1
        open!!.isVisible = openAble
        share!!.isVisible = shareAble

        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        if (null != item) {
            val handleMenu = handleMenu(item)
            if (handleMenu) {
                mode?.finish()
                return true
            }
        }
        return false
    }

    private fun handleMenu(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_select_all -> {
                return true
            }
            R.id.menu_delete -> {
                return true
            }
            R.id.menu_copy -> {
                return true
            }
            R.id.menu_cut -> {
                return true
            }
            R.id.menu_info -> {
                return true
            }
            R.id.men -> {
                return true
            }
        }

        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        callback.onActionModeDestroy()
    }

    interface Callback {
        fun onActionModeCreated()
        fun onActionModeDestroy()
    }
}