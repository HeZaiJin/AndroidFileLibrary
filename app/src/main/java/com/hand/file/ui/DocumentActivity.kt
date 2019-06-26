package com.hand.file.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.hand.document.core.DocumentInfo
import com.hand.document.core.DocumentState
import com.hand.document.core.RootInfo
import com.hand.document.provider.Providers
import com.hand.file.R

/**
 * TODO Document Stack
 */
class DocumentActivity : AppCompatActivity() {

    companion object {
        var TAG = "DocumentActivity"
        fun start(context: Context, rootInfo: RootInfo) {

        }
    }

    private var state = DocumentState()
    private var rootInfo: RootInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        this.rootInfo = Providers.getLocalRoots(applicationContext)!![0]
        onCurrentDirecotryChanged()
    }

    fun openDirectory(rootInfo: RootInfo, documentInfo: DocumentInfo?, back: Boolean) {
        Log.d(TAG, "openDirectory rootInfo $rootInfo, documentInfo $documentInfo")

        if (!back && null != documentInfo) {
            state.pushStack(documentInfo)
            dumpStack()
        }
        DocumentFragment.start(supportFragmentManager, rootInfo, documentInfo)
    }

    fun getCurrentDirectory(): DocumentInfo? {
        return state.peekStack()
    }

    fun onCurrentDirecotryChanged() {
        var documentInfo = getCurrentDirectory()
        if (null == documentInfo) {
            documentInfo = DocumentInfo(true)
        }
        openDirectory(rootInfo!!, documentInfo, false)
    }


    override fun onBackPressed() {
        if (state.stackSize > 0) {
            state.popStack()
            dumpStack()
            var documentInfo = state.peekStack()
            if (null == documentInfo) {
                super.onBackPressed()
            } else {
                openDirectory(rootInfo!!, documentInfo, true)
            }
            return
        }
        dumpStack()
        super.onBackPressed()
    }

    private fun dumpStack() {
        Log.d(TAG, "Current stack: size = " + state.mStack.size)
        Log.d(TAG, " * " + state.mStack.root)
        for (doc in state.mStack) {
            Log.d(TAG, " +-- $doc")
        }
    }
}
