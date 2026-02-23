package com.example.quiz_game.ui.shared.animation

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.zoomOut(): Modifier = composed {
    val initialScale = 5f
    val targetScale = 1f

    val currentState = remember { MutableTransitionState(initialScale) }
    currentState.targetState = targetScale
    val transition = rememberTransition(currentState, label = "animation: zoomOut")

    graphicsLayer { scale(transition.currentState) }
}
