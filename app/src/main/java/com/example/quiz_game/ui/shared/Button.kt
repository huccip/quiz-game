package com.example.quiz_game.ui.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quiz_game.R
import com.example.quiz_game.other.alphaOutOnPress
import com.example.quiz_game.other.scaleDownOnPress

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ButtonFancy(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    content: @Composable () -> Unit = {}
) {

}

@Composable
fun ButtonPrimary(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    content: @Composable () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    OutlinedCard(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
        modifier = modifier.scaleDownOnPress(.8f, interactionSource),
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp,
            ),
            interactionSource = interactionSource,
            modifier = modifier.padding(vertical = 2.dp, horizontal = 6.dp),
        ) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                content = {
                    content()
                }
            )
        }
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
        onClick,
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
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
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
    content: @Composable () -> Unit = {}
) {

}

@Preview(showBackground = true)
@Composable
private fun ButtonPreview() {
    Preview {
        ButtonSecondary {
            Text("Start", fontSize = 10.sp)
            IconButton(
                painter = painterResource(R.drawable.ic_arrow_north_east)
            )
        }
    }
}