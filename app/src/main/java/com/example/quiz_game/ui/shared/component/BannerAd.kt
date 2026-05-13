package com.example.quiz_game.ui.shared.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.quiz_game.other.AdManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    var adViewRef by remember { mutableStateOf<AdView?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Pause / resume / destroy in sync with Activity lifecycle so AdMob's
    // internal auto-refresh timer keeps ticking correctly.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME  -> adViewRef?.resume()
                Lifecycle.Event.ON_PAUSE   -> adViewRef?.pause()
                Lifecycle.Event.ON_DESTROY -> adViewRef?.destroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adViewRef?.destroy()
            adViewRef = null
        }
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdManager.BANNER_AD_UNIT_ID

                // On failure, clear the loaded tag so the update block can
                // retry on the next recomposition rather than staying blank.
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        tag = null
                    }
                }

                // Load immediately on creation — each new AdView instance
                // (e.g. after navigation) triggers a fresh request right away.
                loadAd(AdRequest.Builder().build())
                tag = true

                adViewRef = this
            }
        },
        update = { adView ->
            adViewRef = adView
            // Retry if a previous load failed (tag was reset by the listener).
            if (adView.tag != true) {
                adView.loadAd(AdRequest.Builder().build())
                adView.tag = true
            }
        }
    )
}
