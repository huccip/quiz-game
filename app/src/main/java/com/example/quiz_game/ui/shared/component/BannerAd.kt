package com.example.quiz_game.ui.shared.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.quiz_game.other.AdManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.delay

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    var loadAd by remember { mutableStateOf(false) }

    // Debounce: Wait half a second before requesting an ad. If the Activity is recreated
    // (e.g. rapid theme toggle) before the delay finishes, this effect is cancelled
    // and the ad request is never sent, saving bandwidth and preventing account flags.
    LaunchedEffect(Unit) {
        delay(500L)
        loadAd = true
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdManager.BANNER_AD_UNIT_ID
                // loadAd is deferred to the update block
            }
        },
        update = { adView ->
            // Only trigger the network request once the debounce period has passed
            // and ensure we only load it once per AdView instance.
            if (loadAd && adView.tag != true) {
                adView.loadAd(AdRequest.Builder().build())
                adView.tag = true
            }
        }
    )
}
