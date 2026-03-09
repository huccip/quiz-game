package com.example.quiz_game.ui.shared.effect

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.applyYellowBorder(width: Int) = composed {
    return@composed this.border(width.dp, Color.Yellow)
}

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.applyRedBorder(width: Int) = composed {
    return@composed this.border(width.dp, Color.Red)
}

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.applyBlueBorder(width: Int) = composed {
    return@composed this.border(width.dp, Color.Blue)
}