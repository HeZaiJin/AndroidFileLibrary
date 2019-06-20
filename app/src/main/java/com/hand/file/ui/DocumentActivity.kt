package com.hand.file.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hand.document.core.DocumentInfo
import com.hand.document.core.RootInfo
import com.hand.document.provider.Providers
import com.hand.file.R

class DocumentActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)
        var roots: List<RootInfo> = Providers.getLocalRoots(applicationContext)
        openDirectory(roots.get(0), null)
    }

    public fun openDirectory(rootInfo: RootInfo, documentInfo: DocumentInfo?) {
        DocumentFragment.start(supportFragmentManager, rootInfo, documentInfo)
    }
}
