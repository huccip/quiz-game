package com.example.quiz_game.ui.shared.animation

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.zoomOut(): Modifier = composed {
    var initialScale by remember { mutableFloatStateOf(5f) }
    var targetScale by remember { mutableFloatStateOf(1f) }

    val currentState = remember { MutableTransitionState(initialScale) }
    currentState.targetState = targetScale
    val transition = rememberTransition(currentState, label = "animation: zoomOut")

    graphicsLayer {
        scale(transition.currentState)
    }
}