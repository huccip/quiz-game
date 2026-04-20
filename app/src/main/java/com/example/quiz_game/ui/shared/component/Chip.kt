package com.example.quiz_game.ui.shared.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.unit.dp

@Composable
fun InformativeChip(
    modifier: Modifier = Modifier,
    color: Color,
    text: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit = {},
) {
    var textDp by remember { mutableStateOf(0.dp) }

    SuggestionChip(
        onClick = onClick,
        elevation = SuggestionChipDefaults.suggestionChipElevation(
            elevation = 0.dp,
            pressedElevation = 0.dp
        ),
        border = BorderStroke(
            width = 2.dp,
            color = color
        ),
        shape = RoundedCornerShape(50),
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = modifier
                    .fillMaxHeight()
                    .graphicsLayer { textDp = this.size.height.toDp() }
            )
        },
        icon = {
            Icon(
                modifier = Modifier.size(textDp),
                painter = painterResource(icon),
                contentDescription = null,
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = Color.Transparent,
            labelColor = color,
            iconContentColor = color
        )
    )
}