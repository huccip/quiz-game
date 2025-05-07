package com.example.quiz_game.ui.activity.onboard.destination

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.quiz_game.App
import com.example.quiz_game.R
import com.example.quiz_game.ui.activity.main.MainActivity
import com.example.quiz_game.ui.shared.ButtonPrimary
import com.example.quiz_game.ui.viewmodel.OnboardAction
import com.example.quiz_game.ui.viewmodel.SharedAction

@OptIn(UnstableApi::class)
@Composable
fun Guide(
    modifier: Modifier = Modifier,
    onboardAction: (OnboardAction) -> Unit = {},
    sharedAction: (SharedAction) -> Unit = {}
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (App.userPrefs.contains("guided") && App.userPrefs.getBoolean("guided", true)) {
            sharedAction(SharedAction.StartActivity(context, MainActivity::class.java))
        }
    }

    var videoEnded by remember {
        mutableStateOf(false)
    }
    val videoUri = remember {
        "android.resource://${context.packageName}/${R.raw.something}".toUri()
    }
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(videoUri))
                prepare()
                playWhenReady = true

                addListener(object : Player.Listener {
                    override fun onPlayerStateChanged(
                        playWhenReady: Boolean,
                        playbackState: Int
                    ) {
                        if (playbackState == ExoPlayer.STATE_ENDED) {
                            videoEnded = true
                        }
                    }
                })
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(factory = {
            PlayerView(context).apply {
                player = exoPlayer
            }
        })

        ButtonPrimary(
            enabled = videoEnded,
            onClick = {
                onboardAction(OnboardAction.Done)
                sharedAction(SharedAction.StartActivity(context, MainActivity::class.java))
            }
        ) {
            Text(stringResource(R.string.guide_button_finish))
        }
    }
}