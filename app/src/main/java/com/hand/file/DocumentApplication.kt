package com.hand.file

import android.app.Application
import com.hand.document.provider.StorageVolumeProvider

class DocumentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        StorageVolumeProvider.get().init(applicationContext)
    }
}
