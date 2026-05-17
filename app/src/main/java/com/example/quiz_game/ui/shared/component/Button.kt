package com.example.quiz_game.ui.shared.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quiz_game.R
import com.example.quiz_game.other.withTap
import com.example.quiz_game.ui.activity.main.destination.AnsweredState
import com.example.quiz_game.ui.shared.effect.alphaOutOnPress
import com.example.quiz_game.ui.shared.effect.bounceOnPress
import com.example.quiz_game.ui.shared.effect.scaleDownOnPress
import com.example.quiz_game.ui.theme.Violet600
import com.example.quiz_game.ui.shared.effect.gamePressEffect
import com.example.quiz_game.ui.shared.effect.cartoonBorder
import com.example.quiz_game.ui.shared.shape.WobblyCardShape

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ButtonFancy(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    content: @Composable () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = withTap(onClick),
        enabled = enabled,
        shape = RoundedCornerShape(50),
        color = if (enabled) Color(0xFFFECE44) else Color.LightGray,
        tonalElevation = 6.dp,
        shadowElevation = 10.dp,
        modifier = modifier
            .bounceOnPress(interactionSource)
            .border(
                BorderStroke(2.dp, Color(0xFFFF6F61)),
                RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(50))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

@Composable
fun ButtonPrimary(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    color: Color = Violet600,
    contentColor: Color = Color.White,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = withTap(onClick),
        enabled = enabled,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = contentColor,
            disabledContainerColor = color.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .height(52.dp)
            .cartoonBorder(
                color = color.copy(alpha = 0.1f),
                width = 0.dp,
                shape = RoundedCornerShape(50),
                shadowColor = color.copy(alpha = if (enabled) 0.5f else 0.2f),
                shadowOffset = 4.dp
            )
            .gamePressEffect(interactionSource),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                content()
            }
        )
    }
}

@Composable
fun ButtonSecondary(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    content: @Composable () -> Unit = {}
) {

    val interactionSource = remember { MutableInteractionSource() }

    TextButton(
        withTap(onClick),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
            .alphaOutOnPress(.8f, interactionSource),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(0.dp),


        ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            content = {
                content()
            }
        )
    }
}

@Composable
fun ButtonDanger(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
    content: @Composable () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = withTap(onClick),
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) Color(0xFFFF4C4C) else Color(0xFFBDBDBD),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        ),
        modifier = modifier
            .scaleDownOnPress(0.9f, interactionSource)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon?.let {
                IconButton(painter = painterResource(it))
                Spacer(modifier = Modifier.width(6.dp))
            }
            content()
        }
    }
}

@Composable
fun ButtonGameChoices(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    isCorrectChoice: Boolean = true,
    answeredState: AnsweredState = AnsweredState.IDLE,
    content: @Composable () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    val contentColor = when {
        !enabled && answeredState == AnsweredState.PICKED -> MaterialTheme.colorScheme.primary
        !enabled && isCorrectChoice -> Color.Green
        !enabled && !isCorrectChoice -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    val containerColor = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .cartoonBorder(
                color = borderColor,
                width = 2.dp,
                shape = WobblyCardShape,
                shadowColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
                shadowOffset = 4.dp
            )
            .gamePressEffect(interactionSource)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = withTap(onClick)
            ),
        shape = WobblyCardShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            if (!enabled && answeredState == AnsweredState.PICKED) {
                Icon(
                    painter = painterResource(R.drawable.ic_pin),
                    contentDescription = null,
                    tint = contentColor
                )
            }
        }
    }
}
