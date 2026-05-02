package com.example.quiz_game.other

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdManager {
    private const val TAG = "AdManager"

    // Test Ad Unit IDs
    const val BANNER_AD_UNIT_ID = "***REMOVED***"
    private const val INTERSTITIAL_AD_UNIT_ID = "***REMOVED***"
    private const val REWARDED_AD_UNIT_ID = "***REMOVED***"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var quizzesCompletedCount = 0
    private const val QUIZZES_UNTIL_INTERSTITIAL = 4

    private val _isAdShowing = MutableStateFlow(false)
    val isAdShowing: StateFlow<Boolean> = _isAdShowing.asStateFlow()
    
    private val _isRewardedLoaded = MutableStateFlow(false)
    val isRewardedLoaded: StateFlow<Boolean> = _isRewardedLoaded.asStateFlow()

    fun initialize(context: Context) {
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "MobileAds initialized: $initializationStatus")
            loadInterstitialAd(context)
            loadRewardedAd(context)
        }
    }

    private fun loadInterstitialAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "InterstitialAd failed to load: ${adError.message}")
                    interstitialAd = null
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "InterstitialAd loaded.")
                    interstitialAd = ad
                }
            }
        )
    }

    private fun loadRewardedAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "RewardedAd failed to load: ${adError.message}")
                    rewardedAd = null
                    _isRewardedLoaded.value = false
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "RewardedAd loaded.")
                    rewardedAd = ad
                    _isRewardedLoaded.value = true
                }
            }
        )
    }

    /**
     * Call this when a quiz finishes. It will show the interstitial if the threshold is met.
     */
    fun onQuizCompleted(activity: Activity) {
        quizzesCompletedCount++
        if (quizzesCompletedCount >= QUIZZES_UNTIL_INTERSTITIAL) {
            quizzesCompletedCount = 0
            showInterstitialAd(activity)
        }
    }

    fun showInterstitialAd(activity: Activity) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "InterstitialAd dismissed.")
                    _isAdShowing.value = false
                    interstitialAd = null
                    loadInterstitialAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d(TAG, "InterstitialAd failed to show: ${adError.message}")
                    _isAdShowing.value = false
                    interstitialAd = null
                    loadInterstitialAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "InterstitialAd showed.")
                    _isAdShowing.value = true
                    interstitialAd = null
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.")
            // Try to load again just in case
            loadInterstitialAd(activity)
        }
    }

    fun showRewardedAd(activity: Activity, onRewarded: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "RewardedAd dismissed.")
                    _isAdShowing.value = false
                    rewardedAd = null
                    _isRewardedLoaded.value = false
                    loadRewardedAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d(TAG, "RewardedAd failed to show: ${adError.message}")
                    _isAdShowing.value = false
                    rewardedAd = null
                    _isRewardedLoaded.value = false
                    loadRewardedAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "RewardedAd showed.")
                    _isAdShowing.value = true
                    rewardedAd = null
                    _isRewardedLoaded.value = false
                }
            }
            rewardedAd?.show(activity) { rewardItem ->
                Log.d(TAG, "User earned the reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewarded()
            }
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
            loadRewardedAd(activity)
        }
    }
}