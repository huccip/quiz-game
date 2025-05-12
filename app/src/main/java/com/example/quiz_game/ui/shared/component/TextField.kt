package com.example.quiz_game.ui.shared.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.example.quiz_game.R

@Composable
fun TextFieldPrimary(
    modifier: Modifier = Modifier,
    @StringRes placeholder: Int? = null,
    @StringRes label: Int? = null,
    enabled: Boolean = true,
    onDone: (String) -> Unit = {},
    onNext: (String) -> Unit = {},
    vararg regex: Pair<Regex, String>? = emptyArray<Pair<Regex, String>>(),
    isLast: Boolean = false,
    onValid: (Boolean) -> Unit = {}
) {
    var errors by rememberSaveable {
        mutableStateOf(emptyList<String>())
    }

    var value by rememberSaveable {
        mutableStateOf("")
    }

    val onValidate: (String) -> Unit = { newValue ->
        var newErrors = mutableListOf<String>()

        regex.filter { it != null }.onEach {
            val (regex, description) = it!!

            onValid(newValue.matches(regex))

            if (newValue.matches(regex)) {
                value = newValue
                newErrors.remove(description)
            } else {
                newErrors.add(description)
            }
        }

        errors = newErrors
    }

    LaunchedEffect(value) {
        onValidate(value)
    }

    TextField(
        modifier = modifier,
        value = value,
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