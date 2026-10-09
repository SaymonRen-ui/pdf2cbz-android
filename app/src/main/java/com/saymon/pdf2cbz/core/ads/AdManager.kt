package com.saymon.pdf2cbz.core.ads

import android.app.Activity
import android.content.Context
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader

/**
 * Межстраничная реклама РСЯ на кнопке «Конвертировать в CBZ».
 * Логика как в PDFcon: реклама предзагружается заранее; по нажатию
 * показываем её и СРАЗУ стартуем конвертацию — очередь идёт параллельно.
 */
object AdManager {

    // Боевой ID блока РСЯ (Реклама → Приложения → PDF2CBZ → Блоки).
    // Пока приложение не прошло модерацию РСЯ — сервер отдаёт
    // тестовые заглушки, после — настоящую рекламу. Код менять не надо.
    // Важно: сервер РСЯ не отдаёт рекламу дебажным сборкам (отказ code=3),
    // проверять показ только на релизе!
    const val AD_UNIT_ID = "R-M-20206448-1"

    private var loader: InterstitialAdLoader? = null
    private var ad: InterstitialAd? = null

    fun preload(context: Context) {
        if (ad != null) return
        val l = InterstitialAdLoader(context.applicationContext).apply {
            setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    AdManager.ad = ad
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    AdManager.ad = null
                }
            })
        }
        loader = l
        try {
            l.loadAd(AdRequestConfiguration.Builder(AD_UNIT_ID).build())
        } catch (_: Exception) {
            // без рекламы — просто конвертируем
        }
    }

    fun showAndConvert(activity: Activity, onStartConvert: () -> Unit) {
        val a = ad
        ad = null
        if (a == null) {
            onStartConvert()
            preload(activity)
            return
        }
        a.setAdEventListener(object : InterstitialAdEventListener {
            override fun onAdShown() {}
            override fun onAdFailedToShow(error: AdError) {}
            override fun onAdDismissed() {}
            override fun onAdClicked() {}
            override fun onAdImpression(data: ImpressionData?) {}
        })
        try {
            a.show(activity)
        } catch (_: Exception) {
            // показ упал — конвертация всё равно идёт
        }
        // Конвертация параллельно, не ждём закрытия рекламы
        onStartConvert()
        preload(activity)
    }
}
