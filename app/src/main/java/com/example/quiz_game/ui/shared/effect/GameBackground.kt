package com.example.quiz_game.ui.shared.effect

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.dottedBackground(
    color: Color,
    opacity: Float = 0.15f,
    spacing: Dp = 20.dp,
    radius: Dp = 2.dp
) = this.drawBehind {
    val spacingPx = spacing.toPx()
    val radiusPx = radius.toPx()
    val dotColor = color.copy(alpha = opacity)

    var y = 0f
    while (y < size.height) {
        var x = 0f
        while (x < size.width) {
            drawCircle(
                color = dotColor,
                radius = radiusPx,
                center = Offset(x, y)
            )
            x += spacingPx
        }
        y += spacingPx
    }
}
