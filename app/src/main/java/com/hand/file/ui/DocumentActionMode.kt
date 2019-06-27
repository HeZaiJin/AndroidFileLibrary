package com.hand.file.ui

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.hand.document.core.DocumentInfo
import com.hand.document.core.RootInfo

class DocumentActionMode(root: RootInfo, doc: DocumentInfo): ActionMode.Callback{
    private final var rootInfo: RootInfo = root
    private final var documentInfo: DocumentInfo = doc

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}