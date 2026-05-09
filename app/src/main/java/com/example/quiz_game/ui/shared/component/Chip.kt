package com.example.quiz_game.ui.shared.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.example.quiz_game.ui.shared.effect.cartoonBorder
import com.example.quiz_game.ui.shared.shape.StickerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun InformativeChip(
    modifier: Modifier = Modifier,
    containerColor: Color,
    textColor: Color,
    iconColor: Color,
    borderColor: Color,
    textComposable: @Composable (() -> Unit) = {},
    iconSize: Dp,
    @DrawableRes icon: Int,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = StickerShape,
        color = containerColor,
        contentColor = textColor,
        modifier = modifier
            .cartoonBorder(
                color = borderColor,
                width = 2.dp,
                shape = StickerShape,
                shadowColor = borderColor.copy(alpha = 0.5f),
                shadowOffset = 3.dp
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(icon),
                contentDescription = null,
                tint = iconColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            textComposable()
        }
    }
}
