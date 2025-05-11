package com.example.quiz_game.ui.shared.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

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
        onDismissRequest = { /*TODO*/ }, // will dismiss only by clicking one of the buttons below
        confirmButton = {
            ButtonDanger(
                onClick = onConfirm,
                icon = buttonDismissIcon
            ) {
                Text(stringResource(buttonConfirmText))
            }
        },
        dismissButton = {
            ButtonSecondary(
                onClick = onDismiss,
                icon = buttonConfirmIcon,
            ) {
                Text(stringResource(buttonDismissText))
            }
        },
        title = { TextBig(text = stringResource(title)) },
        text = { TextSmol(text = stringResource(text)) },
        icon = {
            icon?.let {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null
                )
            }
        }
    )
}