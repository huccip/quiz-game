package com.example.quiz_game.ui.shared.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.example.quiz_game.R

@Composable
fun TextFieldPrimary(
    modifier: Modifier = Modifier,
    @StringRes placeholder: Int? = null,
    @StringRes label: Int? = null,
    @DrawableRes trailingIcon: Int? = null,
    @DrawableRes leadingIcon: Int? = null,
    enabled: Boolean = true,
    onDone: (String) -> Unit = {},
    onNext: (String) -> Unit = {},
    onTrailingIconClicked: () -> Unit = {},
    onLeadingIconClicked: () -> Unit = {},
    vararg regex: Pair<Regex, String>? = emptyArray<Pair<Regex, String>>(),
    isLast: Boolean = false,
    cleared: Boolean = false,
    onValid: (Boolean, String) -> Unit = { isValid, value -> }
) {
    var errors by rememberSaveable {
        mutableStateOf(emptySet<String>())
    }

    var value by rememberSaveable {
        mutableStateOf("")
    }

    val onValidate: (String) -> Unit = { newValue ->
        var isValid = true
        regex.filter { it != null }.onEach {
            val (regex, description) = it!!

            if (newValue.matches(regex)) {
                errors = errors.minusElement(description)
            } else {
                isValid = false
                errors = errors.plusElement(description)
            }
        }

        onValid(isValid, newValue)

        if (isValid) {
            value = newValue
        }
    }

    LaunchedEffect(value) {
        onValidate(value)
    }

    LaunchedEffect(cleared) {
        value = ""
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        leadingIcon?.let {
            IconButton(
                onClick = onLeadingIconClicked
            ) {
                IconButton(
                    painter = painterResource(it)
                )
            }
        }

        OutlinedTextField(
            modifier = modifier,
            value = value,
            colors = OutlinedTextFieldDefaults.colors(
                errorTextColor = MaterialTheme.colorScheme.error,
                errorCursorColor = MaterialTheme.colorScheme.error,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
            ),
            placeholder = {
                TextSmol(
                    text = stringResource(
                        id = placeholder ?: R.string.textfield_placeholder_default
                    )
                )
            },
            onValueChange = { newValue ->
                value = newValue.trim()
            },
            label = {
                TextBerySmol(text = stringResource(id = label ?: R.string.textfield_label_default))
            },
            enabled = enabled,
            isError = errors.isNotEmpty(),
            supportingText = {
                Column {
                    errors.onEach {
                        TextBerySmol(text = "- $it", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            trailingIcon = {
                trailingIcon?.let {
                    IconButton(
                        onClick = onTrailingIconClicked,
                    ) {
                        IconButton(
                            painter = painterResource(it),
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                autoCorrectEnabled = true,
                imeAction = when {
                    errors.isNotEmpty() -> ImeAction.None
                    isLast -> ImeAction.Done
                    else -> ImeAction.Next
                }
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone(value) },
                onNext = { onNext(value) }
            )
        )
    }
}