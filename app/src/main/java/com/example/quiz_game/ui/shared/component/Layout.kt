package com.example.quiz_game.ui.shared.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LayoutSectionHeadline(
    modifier: Modifier = Modifier,
    @DrawableRes leadingIcon: Int? = null,
    title: String,
    actionText: String? = null,
    @DrawableRes actionIcon: Int? = null,
    onClick: () -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            leadingIcon?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface
                    ),
                    shape = RoundedCornerShape(40)
                ) {
                    IconButton(
                        painter = painterResource(it),
                        modifier = Modifier.padding(6.dp),
                        tint = contentColorFor(MaterialTheme.colorScheme.inverseSurface)
                    )
                }
            }
            TextBig(
                text = title,
                fontWeight = FontWeight.SemiBold
            )
        }

        actionText?.let {
            ButtonSecondary(onClick = onClick) {
                TextSmol(text = it)
                actionIcon?.let { IconButton(painter = painterResource(it)) }
            }
        }
    }
}
