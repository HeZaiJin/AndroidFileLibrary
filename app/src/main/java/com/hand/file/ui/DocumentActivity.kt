package com.hand.file.ui

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.util.SparseArray
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.hand.document.core.DocumentInfo
import com.hand.document.core.DocumentState
import com.hand.document.core.RootInfo
import com.hand.document.provider.Providers
import com.hand.document.util.LogUtil
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

    private var state: DocumentState? = null
    private var rootInfo: RootInfo? = null
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar!!.setNavigationOnClickListener { onBackPressed() }
        this.rootInfo = Providers.getLocalRoots(applicationContext)!![0]
        state = DocumentState(rootInfo)
        onCurrentDirecotryChanged()
    }

    fun openDirectory(rootInfo: RootInfo, documentInfo: DocumentInfo?, back: Boolean) {
        Log.d(TAG, "openDirectory rootInfo $rootInfo, documentInfo $documentInfo")
        if (!back && null != documentInfo) {
            state!!.pushStack(documentInfo)
            dumpStack()
        }
        DocumentFragment.start(supportFragmentManager, rootInfo, documentInfo)
    }

    fun saveDisplayState(key: String, view: View) {
        var container: SparseArray<Parcelable> = SparseArray()
        view.saveHierarchyState(container)
        state!!.saveDisplayState(key, container)
        //TODO save toolbar state
    }

    fun restoreDisplayState(key: String, view: View) {
        val displayState = getDisplayState(key)
        if (null != displayState) {
            view.restoreHierarchyState(displayState)
            //TODO restore toolbar state
        }
    }

    fun getDisplayState(key: String): SparseArray<Parcelable>? {
        return state!!.getDisplayState(key)
    }

    fun getCurrentDirectory(): DocumentInfo? {
        return state!!.peekStack()
    }

    fun onCurrentDirecotryChanged() {
        var documentInfo = getCurrentDirectory()
        if (null == documentInfo) {
            documentInfo = DocumentInfo(true)
        }
        openDirectory(rootInfo!!, documentInfo, false)
    }


    override fun onBackPressed() {
        if (state!!.stackSize > 0) {
            state!!.popStack()
            dumpStack()
            var documentInfo = state!!.peekStack()
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
        Log.d(TAG, "Current stack: size = " + state!!.mStack.size)
        Log.d(TAG, " * " + state!!.mStack.root)
        for (doc in state!!.mStack) {
            Log.d(TAG, " +-- $doc")
        }
    }
}
