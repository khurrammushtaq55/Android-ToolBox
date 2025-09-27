package com.mmushtaq.orm.allinone.ads

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialAdManager(
    private val activity: Activity,
    private var adUnitId: String = activity.getString(com.mmushtaq.orm.allinone.R.string.admob_interstitial_id),
) {
    private var interstitial: InterstitialAd? = null

    fun load(onLoaded: (() -> Unit)? = null, onFailed: (() -> Unit)? = null) {
        if(AdsSetting.USE_TEST_ADS) adUnitId = activity.getString(com.mmushtaq.orm.allinone.R.string.admob_test_interstitial)

        InterstitialAd.load(
            activity,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    onLoaded?.invoke()
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                    onFailed?.invoke()
                }
            }
        )
    }

    fun show(onDismiss: (() -> Unit)? = null) {
        val ad = interstitial ?: return onDismiss?.invoke() ?: Unit
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                onDismiss?.invoke()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitial = null
                onDismiss?.invoke()
            }
        }
        ad.show(activity)
    }

    val isReady: Boolean get() = interstitial != null
}
