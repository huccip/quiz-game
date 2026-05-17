package com.example.quiz_game.other

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.quiz_game.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdManager {
    private const val TAG = "AdManager"

    // Ad Unit IDs — automatically swapped by BuildConfig:
    //   debug build  → Google test IDs (safe to click, won't flag your account)
    //   release build → your real AdMob IDs
    val BANNER_AD_UNIT_ID: String get() = BuildConfig.ADMOB_BANNER_ID
    private val INTERSTITIAL_AD_UNIT_ID: String get() = BuildConfig.ADMOB_INTERSTITIAL_ID
    private val REWARDED_AD_UNIT_ID: String get() = BuildConfig.ADMOB_REWARDED_ID
    private val REWARDED_INTERSTITIAL_AD_UNIT_ID: String
        get() = BuildConfig.ADMOB_REWARDED_INTERSTITIAL_ID

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null

    private var quizzesCompletedCount = 0
    private const val QUIZZES_UNTIL_INTERSTITIAL = 4

    private val _isAdShowing = MutableStateFlow(false)
    val isAdShowing: StateFlow<Boolean> = _isAdShowing.asStateFlow()

    private val _isRewardedLoaded = MutableStateFlow(false)
    val isRewardedLoaded: StateFlow<Boolean> = _isRewardedLoaded.asStateFlow()

    var isRewardedInterstitialLoaded = MutableStateFlow(false)
        private set

    /**
     * Entry point called from each Activity's onCreate.
     * Runs the UMP consent flow first; once consent is resolved (or not
     * required) it initialises MobileAds and pre-loads all ad formats.
     */
    fun requestConsentAndInitialize(activity: Activity) {
        val params = ConsentRequestParameters.Builder().build()
        val consentInfo = UserMessagingPlatform.getConsentInformation(activity)

        consentInfo.requestConsentInfoUpdate(activity, params, {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                if (formError != null) {
                    Log.w(TAG, "Consent form error: ${formError.message}")
                }
                if (consentInfo.canRequestAds()) {
                    initialize(activity)
                }
            }
        }, { requestError ->
            Log.w(TAG, "Consent request error: ${requestError.message}")
            // Fail open — still try to show ads (non-EEA users unaffected)
            initialize(activity)
        })

        // If consent was already gathered in a previous session, start ads
        // immediately without waiting for the (no-op) form round-trip.
        if (consentInfo.canRequestAds()) {
            initialize(activity)
        }
    }

    fun initialize(context: Context) {
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "MobileAds initialized: $initializationStatus")
            loadInterstitialAd(context)
            loadRewardedAd(context)
            loadRewardedInterstitialAd(context)
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

    private fun loadRewardedInterstitialAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(
            context,
            REWARDED_INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "RewardedInterstitialAd failed to load: ${adError.message}")
                    rewardedInterstitialAd = null
                    isRewardedInterstitialLoaded.value = false
                }

                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    Log.d(TAG, "RewardedInterstitialAd loaded.")
                    rewardedInterstitialAd = ad
                    isRewardedInterstitialLoaded.value = true
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

    // TODO: wire this up with MainActivity.kt, use it on the Daily Reward that displays once you land on Home screen.
    fun showRewardedInterstitialAd(activity: Activity, onRewarded: () -> Unit) {
        if (rewardedInterstitialAd != null) {
            rewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "RewardedInterstitialAd dismissed.")
                    _isAdShowing.value = false
                    rewardedInterstitialAd = null
                    isRewardedInterstitialLoaded.value = false
                    loadRewardedInterstitialAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d(TAG, "RewardedInterstitialAd failed to show: ${adError.message}")
                    _isAdShowing.value = false
                    rewardedInterstitialAd = null
                    isRewardedInterstitialLoaded.value = false
                    loadRewardedInterstitialAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "RewardedInterstitialAd showed.")
                    _isAdShowing.value = true
                    rewardedInterstitialAd = null
                    isRewardedInterstitialLoaded.value = false
                }
            }
            rewardedInterstitialAd?.show(activity) { rewardItem ->
                Log.d(TAG, "User earned the reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewarded()
            }
        } else {
            Log.d(TAG, "The rewarded interstitial ad wasn't ready yet.")
            loadRewardedInterstitialAd(activity)
        }
    }
}
