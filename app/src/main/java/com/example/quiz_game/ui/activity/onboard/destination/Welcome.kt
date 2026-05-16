package com.example.quiz_game.ui.activity.onboard.destination

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.quiz_game.R
import com.example.quiz_game.ui.activity.main.MainActivity
import com.example.quiz_game.ui.viewmodel.SharedAction

@Composable
fun Welcome(
    modifier: Modifier = Modifier,
    sharedAction: (SharedAction) -> Unit = {}
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.welcome))
        val logoAnimationState =
            animateLottieCompositionAsState(composition = composition)
        LottieAnimation(
            composition = composition,
            progress = { logoAnimationState.progress },
            modifier = Modifier.fillMaxSize()
        )

        if (logoAnimationState.isAtEnd && logoAnimationState.isPlaying) {
            sharedAction(SharedAction.StartActivity(context, MainActivity::class.java))
            (context as? android.app.Activity)?.finish()
        }
    }
}
