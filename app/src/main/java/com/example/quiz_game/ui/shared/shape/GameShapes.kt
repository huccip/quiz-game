package com.example.quiz_game.ui.shared.shape

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size

val WobblyCardShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    val cornerRadius1 = width * 0.04f
    val cornerRadius2 = width * 0.06f

    moveTo(cornerRadius1, 0f)
    lineTo(width - cornerRadius2, 0f)
    quadraticBezierTo(width, 0f, width, cornerRadius2)
    lineTo(width, height - cornerRadius1)
    quadraticBezierTo(width, height, width - cornerRadius1, height)
    lineTo(cornerRadius2, height)
    quadraticBezierTo(0f, height, 0f, height - cornerRadius2)
    lineTo(0f, cornerRadius1)
    quadraticBezierTo(0f, 0f, cornerRadius1, 0f)
    close()
}

val StickerShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    val cornerRadius = height / 2f
    val squash = height * 0.05f

    moveTo(cornerRadius, squash)
    lineTo(width - cornerRadius, 0f)
    quadraticBezierTo(width, 0f, width, cornerRadius)
    lineTo(width, height - cornerRadius)
    quadraticBezierTo(width, height, width - cornerRadius, height - squash)
    lineTo(cornerRadius, height)
    quadraticBezierTo(0f, height, 0f, height - cornerRadius)
    lineTo(0f, cornerRadius)
    quadraticBezierTo(0f, squash, cornerRadius, squash)
    close()
}

val TicketShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    val cornerRadius = height * 0.15f
    val toothWidth = height * 0.1f
    val toothCount = (height / toothWidth).toInt()

    moveTo(cornerRadius, 0f)
    lineTo(width - cornerRadius, 0f)
    quadraticBezierTo(width, 0f, width, cornerRadius)
    
    // Draw serrated right edge
    var currentY = cornerRadius
    val toothHeight = (height - 2 * cornerRadius) / toothCount
    for (i in 0 until toothCount) {
        lineTo(width - toothWidth, currentY + toothHeight / 2)
        lineTo(width, currentY + toothHeight)
        currentY += toothHeight
    }
    
    lineTo(width, height - cornerRadius)
    quadraticBezierTo(width, height, width - cornerRadius, height)
    lineTo(cornerRadius, height)
    quadraticBezierTo(0f, height, 0f, height - cornerRadius)
    lineTo(0f, cornerRadius)
    quadraticBezierTo(0f, 0f, cornerRadius, 0f)
    close()
}
