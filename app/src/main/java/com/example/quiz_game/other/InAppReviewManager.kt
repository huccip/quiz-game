package com.example.quiz_game.other

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

object InAppReviewManager {
    private const val TAG = "InAppReviewManager"

    /**
     * Silently asks the Play Store to show an in-app review dialog.
     * Google throttles how often this actually shows to the user — we just
     * call it at the right moment (new high score) and let the OS decide.
     */
    fun requestReview(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow()
            .addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    manager.launchReviewFlow(activity, request.result)
                        .addOnCompleteListener {
                            Log.d(TAG, "Review flow completed.")
                        }
                } else {
                    Log.w(TAG, "Review request failed: ${request.exception?.message}")
                }
            }
    }
}
