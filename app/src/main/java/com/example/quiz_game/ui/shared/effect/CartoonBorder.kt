package com.example.quiz_game.ui.shared.effect

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.translate

fun Modifier.cartoonBorder(
    color: Color,
    width: Dp = 2.dp,
    shape: Shape,
    shadowColor: Color = color.copy(alpha = 0.5f),
    shadowOffset: Dp = 4.dp
) = this.drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)
    val path = androidx.compose.ui.graphics.Path().apply {
        when (outline) {
            is androidx.compose.ui.graphics.Outline.Rectangle -> addRect(outline.rect)
            is androidx.compose.ui.graphics.Outline.Rounded -> addRoundRect(outline.roundRect)
            is androidx.compose.ui.graphics.Outline.Generic -> addPath(outline.path)
        }
    }
    
    // Draw shadow
    translate(top = shadowOffset.toPx()) {
        drawPath(
            path = path,
            color = shadowColor
        )
    }

    // Draw border
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = width.toPx())
    )
}
