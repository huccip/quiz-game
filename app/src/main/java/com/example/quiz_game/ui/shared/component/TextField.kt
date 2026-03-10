package com.example.quiz_game.ui.shared.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.quiz_game.R
import com.example.quiz_game.ui.theme.Indigo600

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
    onValid: (Boolean, String) -> Unit = { _, _ -> }
) {
    var errors by rememberSaveable {
        mutableStateOf(emptySet<String>())
    }

    var value by rememberSaveable {
        mutableStateOf("")
    }

    var isFocused by rememberSaveable {
        mutableStateOf(false)
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

    // ── Animated border color ──
    val borderColor by animateColorAsState(
        targetValue = when {
            errors.isNotEmpty() -> MaterialTheme.colorScheme.error
            isFocused -> Indigo600
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(200),
        label = "textFieldBorder"
    )

    Column {
        // ── Static label above the field ──
        label?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = when {
                    errors.isNotEmpty() -> MaterialTheme.colorScheme.error
                    isFocused -> Indigo600
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(Modifier.height(8.dp))
        }

        // ── Modern rounded text field ──
        OutlinedTextField(
            modifier = modifier
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            value = value,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                // Normal state
                focusedBorderColor = Indigo600,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                // Error state
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f),
                errorCursorColor = MaterialTheme.colorScheme.error,
                errorTextColor = MaterialTheme.colorScheme.onSurface,
                // Cursor
                cursorColor = Indigo600,
                // Text
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                // Placeholder
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            ),
            placeholder = {
                Text(
                    text = stringResource(
                        id = placeholder ?: R.string.textfield_placeholder_default
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            onValueChange = { newValue ->
                value = newValue.trim()
            },
            // No floating label — we use the static label above
            label = null,
            enabled = enabled,
            singleLine = true,
            isError = errors.isNotEmpty(),
            supportingText = if (errors.isNotEmpty()) {
                {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        errors.onEach { error ->
                            Text(
                                text = "• $error",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            } else null,
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
