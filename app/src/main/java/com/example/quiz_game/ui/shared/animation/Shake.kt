package com.example.quiz_game.ui.shared.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.shakeLinear(
    triggered: Boolean,
    orientation: Orientation,
    intensity: Float = 10f,
    durationMillis: Int = 500
): Modifier = composed {
    val offset = remember { Animatable(0f) }

    LaunchedEffect(triggered) {
        if (triggered) {
            offset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    val stepDuration = durationMillis / 10

                    0f at 0
                    intensity at stepDuration
                    -intensity at stepDuration * 2
                    intensity * 0.8f at stepDuration * 3
                    -intensity * 0.8f at stepDuration * 4
                    intensity * 0.6f at stepDuration * 5
                    -intensity * 0.6f at stepDuration * 6
                    intensity * 0.4f at stepDuration * 7
                    -intensity * 0.4f at stepDuration * 8
                    intensity * 0.2f at stepDuration * 9
                    0f at durationMillis
                }
            )
        }
    }

    this.graphicsLayer {
        when (orientation) {
            Orientation.Horizontal -> translationX = offset.value
            Orientation.Vertical -> translationY = offset.value
        }
    }
}

fun Modifier.shakeCircular(
    triggered: Boolean,
    intensity: Float = 10f,
    durationMillis: Int = 500
): Modifier = composed {
    val offset = remember { Animatable(0f) }

    LaunchedEffect(triggered) {
        if (triggered) {
            offset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    val stepDuration = durationMillis / 10

                    0f at 0
                    intensity at stepDuration
                    -intensity * 0.8f at stepDuration * 3
                    intensity * 0.6f at stepDuration * 5
                    intensity * 0.4f at stepDuration * 7
                    intensity * 0.2f at stepDuration * 9
                    0f at durationMillis
                }
            )
        }
    }

    this.graphicsLayer {
        rotationZ = offset.value
    }
}

enum class Orientation {
    Horizontal, Vertical
}