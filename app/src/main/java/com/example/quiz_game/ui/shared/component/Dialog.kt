package com.example.quiz_game.ui.shared.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign

@Composable
fun DialogYesOrNo(
        modifier: Modifier = Modifier,
        @StringRes title: Int,
        @StringRes text: Int,
        @DrawableRes icon: Int? = null,
        @StringRes buttonConfirmText: Int,
        @StringRes buttonDismissText: Int,
        @DrawableRes buttonConfirmIcon: Int? = null,
        @DrawableRes buttonDismissIcon: Int? = null,
        onConfirm: () -> Unit = {},
        onDismiss: () -> Unit = {}
) {
    AlertDialog(
            modifier = modifier,
            onDismissRequest = {}, // Dismiss only by clicking one of the buttons below
            confirmButton = {
                ButtonDanger(onClick = onConfirm, icon = buttonDismissIcon) {
                    Text(stringResource(buttonConfirmText))
                }
            },
            dismissButton = {
                ButtonSecondary(
                        onClick = onDismiss,
                ) {
                    Text(stringResource(buttonDismissText), color = MaterialTheme.colorScheme.onSurface)
                    buttonDismissIcon?.let { IconButton(painter = painterResource(it)) }
                }
            },
            title = { TextBig(text = stringResource(title)) },
            text = { TextSmol(text = stringResource(text)) },
            icon = { icon?.let { Icon(painter = painterResource(it), contentDescription = null) } }
    )
}

@Composable
fun DialogInfo(
        modifier: Modifier = Modifier,
        @StringRes title: Int,
        @StringRes text: Int,
        @DrawableRes icon: Int? = null,
        @StringRes buttonConfirmText: Int,
        onConfirm: () -> Unit = {},
        onDismiss: () -> Unit = {}
) {
    AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismiss,
            confirmButton = {
                ButtonPrimary(onClick = onConfirm) { Text(stringResource(buttonConfirmText)) }
            },
            title = { TextBig(text = stringResource(title)) },
            text = {
                TextSmol(
                        text = stringResource(text),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                )
            },
            icon = { icon?.let { Icon(painter = painterResource(it), contentDescription = null) } }
    )
}
