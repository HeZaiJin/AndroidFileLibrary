package com.hand.file.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.view.ActionMode
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hand.document.adapter.MultiChoiceHelper
import com.hand.document.core.DirectoryLoader
import com.hand.document.core.DirectoryResult
import com.hand.document.core.DocumentInfo
import com.hand.document.core.RootInfo
import com.hand.document.util.LogUtil
import com.hand.file.R
import com.hand.file.ui.widget.BottomChooseDialog


/**
 * TODO save FragmentState
 */
class DocumentFragment : Fragment(), MultiChoiceHelper.MultiChoiceListener, DocumentActionMode.Callback {


    companion object {
        var TAG = "DocumentFragment"
        var LOADER_ID = 3

        val EXTRA_TYPE = "type"
        val EXTRA_ROOT = "root"
        val EXTRA_DOC = "doc"

        fun start(fragmentManager: FragmentManager, rootInfo: RootInfo, documentInfo: DocumentInfo?) {
            val args = Bundle()
            args.putParcelable(EXTRA_ROOT, rootInfo)
            args.putParcelable(EXTRA_DOC, documentInfo)
            var fragment = DocumentFragment()
            fragment.arguments = args
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.content, fragment)
            fragmentTransaction.commitAllowingStateLoss()
            LogUtil.d(TAG, "start document $documentInfo")
        }
    }

    private lateinit var loadingBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DocAdapter
    private lateinit var rootInfo: RootInfo
    private lateinit var documentInfo: DocumentInfo
    private var actionModeCallback: DocumentActionMode? = null
    private var actionMode: ActionMode? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.list)
        loadingBar = view.findViewById(R.id.loading)
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_document, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        rootInfo = arguments!!.get(EXTRA_ROOT)!! as RootInfo
        documentInfo = arguments!!.get(EXTRA_DOC)!! as DocumentInfo

        adapter = DocAdapter(R.layout.item_doc)
        recyclerView.adapter = adapter
        (activity as DocumentActivity).getState().getDirCursor(getDisplayStateKey())?.let {
            loadingBar.visibility = View.INVISIBLE
            adapter.swapResult(it)
            restoreDisplayState()
        }
        var callback = object : LoaderManager.LoaderCallbacks<DirectoryResult> {

            override fun onCreateLoader(id: Int, args: Bundle?): Loader<DirectoryResult> {

                return DirectoryLoader(context!!, rootInfo, documentInfo)
            }

            override fun onLoadFinished(loader: Loader<DirectoryResult>, data: DirectoryResult?) {
                loadingBar!!.visibility = View.INVISIBLE
                adapter!!.swapResult(data!!.cursor)
                restoreDisplayState()
                LogUtil.d(MainActivity.TAG, "onLoadFinished")
            }

            override fun onLoaderReset(loader: Loader<DirectoryResult>) {
                LogUtil.d(MainActivity.TAG, "onLoaderReset")
            }

        }
        activity?.let {
            LoaderManager.getInstance(it).restartLoader(LOADER_ID, null, callback)
        }
        //test code
        adapter.setItemClickListener { v, position ->
            (activity as DocumentActivity).openDirectory(rootInfo, adapter.getItem(position), false)
        }
        adapter.setMultiChoiceListener(this)
    }

    private fun getActionModeCallback(): DocumentActionMode? {
        context?.run {
            if (null == actionModeCallback) {
                actionModeCallback = DocumentActionMode(this, adapter, rootInfo, documentInfo, this@DocumentFragment)
            }
        }
        return actionModeCallback
    }

    override fun onItemCheckedStateChanged(position: Int, checked: Boolean) {
        actionModeCallback?.update(actionMode, position, checked)
    }

    override fun onEditingStateChanged(edit: Boolean) {
        LogUtil.d(TAG, "onEditingStateChanged start")
        if (edit) {
            var documentActionMode = getActionModeCallback() ?: return
            LogUtil.d(TAG, "onEditingStateChanged get ActionMode end")
            actionMode = (activity as DocumentActivity)?.startSupportActionMode(documentActionMode)
            LogUtil.d(TAG, "onEditingStateChanged start ActionMode end")
        } else {
            actionMode?.apply {
                finish()
            }
        }
    }

    override fun onSelectAll(selectAll: Boolean) {
        actionModeCallback?.updateSelectAll(selectAll)
    }

    override fun onActionModeDestroy() {
        (activity as DocumentActivity)?.run {
            updateActionModeState(false)
        }
        if (adapter.isEditing) {
            adapter.exitEditing()
        }
    }

    override fun onActionModeCreated() {
        (activity as DocumentActivity)?.run {
            updateActionModeState(true)
        }
    }

    private fun selectAll(selectAll: Boolean) {
        adapter.selectAll(selectAll)
    }

    override fun onActionMenuClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_select_all -> {
                var selectAll = adapter.isSelectAll
                selectAll(!selectAll)
                actionModeCallback?.run {
                    updateSelectAll(!selectAll)
                    actionMode?.invalidate()
                }
                return false
            }
            R.id.menu_delete -> {
                return true
            }
            R.id.menu_copy -> {
                return true
            }
            R.id.menu_cut -> {
                context?.run {
                    BottomChooseDialog<RootInfo>(this).apply {
                        var roots = ArrayList<BottomChooseDialog.Data<RootInfo>>()
                        var data = BottomChooseDialog.Data<RootInfo>(
                            rootInfo.title,
                            resources.getDrawable(R.drawable.ic_root_documents),
                            rootInfo
                        )
                        bindData(
                            roots.apply {
                                add(data)
                            }
                        )
                    }.show()
                }
                return true
            }
            R.id.menu_info -> {
                return true
            }
            R.id.menu_rename -> {
                return true
            }
            R.id.menu_share -> {
                return true
            }
            R.id.menu_compress -> {
                return true
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        adapter.cursor?.run {
            (activity as DocumentActivity)?.apply {
                getState().saveDirCursor(getDisplayStateKey(), adapter.cursor)
                view?.apply {
                    saveDisplayState(getDisplayStateKey(), this)
                }
            }
        }
    }

    private fun restoreDisplayState() {
        (activity as DocumentActivity)?.apply {
            if (null != view) {
                restoreDisplayState(getDisplayStateKey(), view!!)
            }
        }
    }

    private fun getDisplayStateKey(): String {
        return rootInfo!!.rootId + ":" + documentInfo!!.documentId
    }
}