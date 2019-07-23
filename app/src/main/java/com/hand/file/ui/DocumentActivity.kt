package com.hand.file.ui

import android.content.Context
import android.content.Intent
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
import com.hand.document.operation.BaseTask
import com.hand.document.operation.DeleteTask
import com.hand.document.operation.TaskProgress
import com.hand.document.operation.TaskResult
import com.hand.document.provider.Providers
import com.hand.document.util.BuildUtils
import com.hand.document.util.LogUtil
import com.hand.file.R
import com.hand.file.setupActionBar


/**
 * TODO Document Stack
 */
class DocumentActivity : AppCompatActivity(),
    BaseTask.Observer/*, FolderNavigationBar.NavigateListener<DocumentInfo>*/ {

    companion object {
        var TAG = "DocumentActivity"

        const val EXTRA_ROOT_TYPE = "root_type"
        const val EXTRA_ROOT_INFO = "root_info"

        fun start(context: Context, @Providers.RootType rootType: String, rootInfo: RootInfo?) {
            LogUtil.d(TAG, "onClick start root $rootType")
            context.startActivity(Intent(context, DocumentActivity::class.java).apply {
                putExtra(EXTRA_ROOT_TYPE, rootType)
                putExtra(EXTRA_ROOT_INFO, rootInfo)
            })
            LogUtil.d(TAG, "onClick start end")
        }
    }

    private lateinit var state: DocumentState
    private lateinit var rootInfo: RootInfo
    private lateinit var appBarLayout: AppBarLayout
    private var rootType: String = Providers.ROOT_IMAGE
    private var actionModeState = false

    //    private var navigate: FolderNavigationBar? = null
    private var currentDocumentInfo: DocumentInfo? = null

    private fun parseIntent(intent: Intent) {

        var root = intent.getParcelableExtra<RootInfo>(EXTRA_ROOT_INFO)
        rootType = intent.getStringExtra(EXTRA_ROOT_TYPE)

        if (null == root) {
            root = Providers.getRoot(this@DocumentActivity, rootType)
        }
        rootInfo = root
        state = DocumentState(rootInfo)
        title = rootInfo.title

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)
        appBarLayout = findViewById(R.id.barLayout)
        setupActionBar(R.id.toolbar) {
            setHomeAsUpIndicator(R.drawable.ic_menu)
            setDisplayHomeAsUpEnabled(true)
            (findViewById<Toolbar>(R.id.toolbar)).apply {
                setSupportActionBar(this)
                this.setNavigationOnClickListener { onBackPressed() }
            }
        }
//        navigate = findViewById(R.id.navigate)
        parseIntent(intent)
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
        documentInfo.apply {
            if (!back) {
                state.pushStack(this)
                dumpStack()
            }
            DocumentFragment.start(supportFragmentManager, rootInfo, this)
            onDirectoryChanged(this)
        }
    }

    fun saveDisplayState(key: String, view: View) {
        SparseArray<Parcelable>().apply {
            view.saveHierarchyState(this)
            state.saveDisplayState(key, this)
        }
    }

    fun restoreDisplayState(key: String, view: View) {
        val displayState = getDisplayState(key)
        if (null != displayState) {
            view.restoreHierarchyState(displayState)
        }
    }

    fun getState(): DocumentState {
        return state
    }

    fun getDisplayState(key: String): SparseArray<Parcelable>? {
        return state.getDisplayState(key)
    }

    fun getCurrentDirectory(): DocumentInfo? {
        return state.peekStack()
    }

    fun openRootDirectory() {
        var documentInfo = getCurrentDirectory()
        if (null == documentInfo) {
            documentInfo = DocumentInfo(true)
        }
        openDirectory(rootInfo, documentInfo, false)
    }

    fun onDirectoryChanged(documentInfo: DocumentInfo) {
        var name = documentInfo.displayName
        if (TextUtils.isEmpty(name)) {
            name = rootInfo.title
        }
//        navigate?.openFolder(documentInfo, name)
        currentDocumentInfo = documentInfo
        title = name
    }

//    override fun onNavigate(info: DocumentInfo) {
//        if (null != currentDocumentInfo) {
//            if (currentDocumentInfo!! == info) {
//                return
//            }
//        }
//        if (info.isRootDirectory) {
//            state.clear()
//            openRootDirectory()
//        } else if (null != state) {
//            var documentInfo = state!!.peekStack()
//            while (documentInfo != info) {
//                state!!.popStack()
//                documentInfo = state!!.peekStack()
//            }
//            openDirectory(rootInfo!!, info, false)
//        }
//    }

    fun updateActionModeState(start: Boolean) {
        if (start != actionModeState) {
            actionModeState = start
            if (BuildUtils.hasLollipop()) {
                window.apply {
                    decorView.systemUiVisibility = if (start) {
                        statusBarColor = resources.getColor(R.color.blue)
                        decorView.systemUiVisibility xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    } else {
                        statusBarColor = resources.getColor(R.color.white)
                        decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        dumpStack()
        if (state.stackSize > 0) {
            state.popStack()
            state.peekStack()?.run {
                openDirectory(rootInfo, this, true)
                return
            }
        }
        super.onBackPressed()
    }


    override fun onDestroy() {
        super.onDestroy()
        state.clear()
    }

    private fun dumpStack() {
        Log.d(TAG, "Current stack: size = " + state.mStack.size)
        Log.d(TAG, " * " + state.mStack.root)
        for (doc in state.mStack) {
            Log.d(TAG, " +-- $doc")
        }
    }

    fun deleteDocuments(list: ArrayList<DocumentInfo>) {
        if (list.isNotEmpty()) {
            DeleteTask(this, list).run(this)
        }
    }

    override fun onTaskStart() {
        Log.d(TAG, "onTaskStart")
    }

    override fun onTaskProgress(progress: TaskProgress) {
        Log.d(TAG, "onTaskProgress progress ${progress.progress}, max ${progress.max}")
    }

    override fun onTaskComplete(result: TaskResult) {
        Log.d(TAG, "onTaskComplete state ${result.state}, msg ${result.msg}")
    }
}
