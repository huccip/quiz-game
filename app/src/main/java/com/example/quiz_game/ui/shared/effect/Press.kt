package com.example.quiz_game.ui.shared.effect

import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.scaleDownOnPress(
    scaleRatio: Float = .6f,
    interactionSource: InteractionSource = MutableInteractionSource()
) = composed {
    val isPressed by interactionSource.collectIsPressedAsState()

    val animationSpec: FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleRatio else 1f,
        animationSpec = animationSpec,
        label = "scaleDownOnPress"
    )

    return@composed this.scale(scale)
}

fun Modifier.alphaOutOnPress(
    alphaRatio: Float = .6f,
    interactionSource: InteractionSource = MutableInteractionSource()
) = composed {
    val isPressed by interactionSource.collectIsPressedAsState()

    val animationSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = 100,
        delayMillis = 50,
        easing = EaseOutExpo
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) alphaRatio else 1f,
        animationSpec = animationSpec,
        label = "alphaOutOnPress"
    )

    return@composed this.alpha(alpha)
}

fun Modifier.bounceOnPress(
    interactionSource: InteractionSource = MutableInteractionSource()
) = composed {
    val isPressed by interactionSource.collectIsPressedAsState()

    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioHighBouncy,
        stiffness = Spring.StiffnessLow
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.1f else 1f,
        animationSpec = animationSpec,
        label = "bounceOnPress"
    )

    return@composed this.scale(scale)
}

fun Modifier.gamePressEffect(
    interactionSource: InteractionSource = MutableInteractionSource(),
    offsetDp: Float = 4f
) = composed {
    val isPressed by interactionSource.collectIsPressedAsState()

    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val offset by animateFloatAsState(
        targetValue = if (isPressed) offsetDp else 0f,
        animationSpec = animationSpec,
        label = "gamePressEffect"
    )

    return@composed this.graphicsLayer {
        translationY = offset * density
    }
}
