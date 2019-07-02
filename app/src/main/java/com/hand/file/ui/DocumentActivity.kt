package com.hand.file.ui

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import com.hand.document.core.DocumentInfo
import com.hand.document.core.DocumentState
import com.hand.document.core.RootInfo
import com.hand.document.provider.Providers
import com.hand.document.util.BuildUtils
import com.hand.file.R
import com.hand.file.ui.widget.FolderNavigationBar


/**
 * TODO Document Stack
 */
class DocumentActivity : AppCompatActivity(), FolderNavigationBar.NavigateListener<DocumentInfo> {

    companion object {
        var TAG = "DocumentActivity"
        fun start(context: Context, rootInfo: RootInfo) {

        }
    }

    private var state: DocumentState? = null
    private var rootInfo: RootInfo? = null
    private var toolbar: Toolbar? = null
    private var appBarLayout: AppBarLayout? = null
//    private var navigate: FolderNavigationBar? = null
    private var currentDocumentInfo: DocumentInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)
        appBarLayout = findViewById(R.id.barLayout)
        toolbar = findViewById(R.id.toolbar)
//        navigate = findViewById(R.id.navigate)
        setSupportActionBar(toolbar)
        toolbar!!.setNavigationOnClickListener { onBackPressed() }
        this.rootInfo = Providers.getLocalRoots(applicationContext)!![0]
        state = DocumentState(rootInfo)
        title = rootInfo!!.title
        openRootDirectory()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun openDirectory(rootInfo: RootInfo, documentInfo: DocumentInfo, back: Boolean) {
        Log.d(TAG, "openDirectory rootInfo $rootInfo, documentInfo $documentInfo")
        if (!back && null != documentInfo) {
            state!!.pushStack(documentInfo)
            dumpStack()
        }
        DocumentFragment.start(supportFragmentManager, rootInfo, documentInfo)
        onDirectoryChanged(documentInfo!!)
    }

    fun saveDisplayState(key: String, view: View) {
        var container: SparseArray<Parcelable> = SparseArray()
        view.saveHierarchyState(container)
        state?.saveDisplayState(key, container)
    }

    fun restoreDisplayState(key: String, view: View) {
        val displayState = getDisplayState(key)
        if (null != displayState) {
            view.restoreHierarchyState(displayState)
        }
    }

    fun getState(): DocumentState? {
        return state
    }

    fun getDisplayState(key: String): SparseArray<Parcelable>? {
        return state!!.getDisplayState(key)
    }

    fun getCurrentDirectory(): DocumentInfo? {
        return state!!.peekStack()
    }

    fun openRootDirectory() {
        var documentInfo = getCurrentDirectory()
        if (null == documentInfo) {
            documentInfo = DocumentInfo(true)
        }
        openDirectory(rootInfo!!, documentInfo, false)
    }

    fun onDirectoryChanged(documentInfo: DocumentInfo) {
        var name = documentInfo.displayName
        if (TextUtils.isEmpty(name)) {
            name = rootInfo?.title
        }
//        navigate?.openFolder(documentInfo, name)
        currentDocumentInfo = documentInfo
        title = name
    }

    override fun onNavigate(info: DocumentInfo) {
        if (null != currentDocumentInfo) {
            if (currentDocumentInfo!! == info) {
                return
            }
        }
        if (info.isRootDirectory) {
            state?.clear()
            openRootDirectory()
        } else if (null != state){
            var documentInfo = state!!.peekStack()
            while (documentInfo != info) {
                state!!.popStack()
                documentInfo = state!!.peekStack()
            }
            openDirectory(rootInfo!!, info, false)
        }
    }

    fun updateStatusBarColor(color: Int, light: Boolean) {
        if (BuildUtils.hasLollipop()) {
            window.statusBarColor = color
            val systemUiVisibility = window.decorView.systemUiVisibility
            if (light) {
                window.decorView.systemUiVisibility = (systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            } else {
                window.decorView.systemUiVisibility = (systemUiVisibility xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            }
        }
//        toolbar?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    override fun onBackPressed() {
//        navigate?.onBackPressed()
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


    override fun onDestroy() {
        super.onDestroy()
        state?.clear()
    }

    private fun dumpStack() {
        Log.d(TAG, "Current stack: size = " + state!!.mStack.size)
        Log.d(TAG, " * " + state!!.mStack.root)
        for (doc in state!!.mStack) {
            Log.d(TAG, " +-- $doc")
        }
    }
}
