package com.example.quiz_game.ui.shared.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quiz_game.R
import com.example.quiz_game.ui.shared.effect.scaleDownOnPress
import com.example.quiz_game.ui.theme.Indigo50
import com.example.quiz_game.ui.theme.Indigo100
import com.example.quiz_game.ui.theme.Indigo600


@Composable
fun CardSelectable(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onSelect: () -> Unit,
    color: Color = MaterialTheme.colorScheme.surface,
    selectionColor: Color = Indigo600.copy(alpha = 0.12f),
    showCheckmark: Boolean = false,
    content: @Composable (Modifier) -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val borderColor = when {
        selected -> Indigo600
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }
    val containerColor = if (selected) selectionColor else color

    OutlinedCard(
        onClick = { onSelect() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.outlinedCardElevation(
            defaultElevation = if (selected) 0.dp else 1.dp,
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .scaleDownOnPress(
                interactionSource = interactionSource,
                scaleRatio = .97f
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            content(Modifier.weight(1f))

            if (selected && showCheckmark) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = if (isSystemInDarkTheme()) Indigo100 else Indigo600,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}


@Composable
fun CardButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    @StringRes buttonText: Int,
    @StringRes contextualText: Int,
    vararg subjectText: String,
    @DrawableRes contextualIcon: Int? = null,
    @DrawableRes buttonIcon: Int? = null,
) {
    OutlinedCard {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            ButtonPrimary(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                content = {
                    contextualIcon?.let {
                        Icon(
                            painter = painterResource(it),
                            contentDescription = stringResource(contextualText)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    TextBig(text = stringResource(contextualText))
                    subjectText.onEach {
                        Spacer(Modifier.height(5.dp))
                        TextSmol(text = "- $it")
                    }

                    Spacer(Modifier.height(15.dp))

                    ButtonPrimary(
                        onClick = onClick,
                        content = {
                            TextButton(text = stringResource(buttonText))
                            buttonIcon?.let { IconButton(painter = painterResource(it)) }
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun CardClickable(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier =
            modifier.scaleDownOnPress(
                scaleRatio = .9f,
                interactionSource = interactionSource
            ),
        onClick = onClick,
        interactionSource = interactionSource,
        colors =
            CardDefaults.cardColors(
                containerColor = color,
                contentColor = contentColorFor(color)
            )
    ) { content() }
}

@Preview(showBackground = true)
@Composable
private fun CardWithCheckboxPrev() {
    var checked by rememberSaveable { mutableStateOf(false) }
    Preview {
        CardSelectable(
            content = { modifier ->
                Text("English", modifier = modifier)

                if (checked) {
                    IconButton(
                        painter = painterResource(R.drawable.ic_check),
                    )
                }
            },
            onSelect = { checked = !checked },
            selected = checked
        )
    }
}
