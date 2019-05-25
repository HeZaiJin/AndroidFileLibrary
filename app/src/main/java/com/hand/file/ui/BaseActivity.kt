package com.hand.file.ui

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

abstract class BaseActivity : AppCompatActivity() {

    var mPermissionSubscribe: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission()
    }

    private fun checkPermission() {
        mPermissionSubscribe = RxPermissions(this)
            .requestEachCombined(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe { t ->
                if (t.shouldShowRequestPermissionRationale) {
                    finish()
                }
            }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (!mPermissionSubscribe!!.isDisposed) {
            mPermissionSubscribe!!.dispose()
        }
    }
}