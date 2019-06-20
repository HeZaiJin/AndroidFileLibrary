package com.hand.file.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hand.document.core.DirectoryLoader
import com.hand.document.core.DirectoryResult
import com.hand.document.core.DocumentInfo
import com.hand.document.core.RootInfo
import com.hand.document.util.LogUtil
import com.hand.file.R

class DocumentFragment : Fragment() {

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
        }
    }

    private var recyclerView: RecyclerView? = null
    private var adapter: DocAdapter? = null
    private var rootInfo: RootInfo? = null
    private var documentInfo: DocumentInfo? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.list)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_document, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        rootInfo = arguments!!.get(EXTRA_ROOT) as RootInfo?
        documentInfo = arguments!!.get(EXTRA_DOC) as DocumentInfo?

        adapter = DocAdapter(R.layout.item_doc)

        recyclerView!!.adapter = adapter

        var callback = object : LoaderManager.LoaderCallbacks<DirectoryResult> {

            override fun onCreateLoader(id: Int, args: Bundle?): Loader<DirectoryResult> {
                return DirectoryLoader(context!!, rootInfo, documentInfo)
            }

            override fun onLoadFinished(loader: Loader<DirectoryResult>, data: DirectoryResult?) {
                adapter!!.swapResult(data!!.cursor)
                LogUtil.d(MainActivity.TAG, "onLoadFinished")
            }

            override fun onLoaderReset(loader: Loader<DirectoryResult>) {
                LogUtil.d(MainActivity.TAG, "onLoaderReset")
            }

        }
        LoaderManager.getInstance(activity!!).restartLoader(LOADER_ID, null, callback)
        //test code
        adapter!!.setItemClickListener { view, position ->
            run {
                (activity as DocumentActivity).openDirectory(rootInfo!!, adapter!!.getItem(position))
            }
        }
    }
}