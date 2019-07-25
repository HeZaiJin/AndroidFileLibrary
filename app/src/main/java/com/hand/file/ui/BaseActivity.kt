package com.hand.file.ui

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.hand.file.R
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

abstract class BaseActivity : AppCompatActivity() {

    private var mPermissionSubscribe: Disposable? = null

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


    private fun getContentView(): View {
        return findViewById(R.id.contentPanel)
    }

    fun showToast(msg: String) {
        Snackbar.make(getContentView(), msg, Snackbar.LENGTH_SHORT).show()
    }

    fun showToast(msg: Int) {
        Snackbar.make(getContentView(), msg, Snackbar.LENGTH_SHORT).show()
    }
}