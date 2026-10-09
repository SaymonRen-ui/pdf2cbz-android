package com.saymon.pdf2cbz

import android.app.Application
import com.saymon.pdf2cbz.core.ads.AdManager
import com.yandex.mobile.ads.common.MobileAds

class Pdf2CbzApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            MobileAds.initialize(this) {
                AdManager.preload(this)
            }
        } catch (_: Exception) {
        }
    }
}
