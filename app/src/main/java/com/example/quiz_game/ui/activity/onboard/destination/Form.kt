package com.example.quiz_game.ui.activity.onboard.destination

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.ui.activity.onboard.OnboardDestination
import com.example.quiz_game.ui.shared.animation.Orientation
import com.example.quiz_game.ui.shared.animation.shake
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.IconButton
import com.example.quiz_game.ui.shared.component.ScreenHeader
import com.example.quiz_game.ui.shared.component.TextButton
import com.example.quiz_game.ui.shared.component.TextFieldPrimary
import com.example.quiz_game.ui.viewmodel.OnboardAction
import com.example.quiz_game.ui.viewmodel.SharedAction

@Composable
fun Form(
    modifier: Modifier = Modifier,
    sharedAction: (SharedAction) -> Unit = {},
    onboardAction: (OnboardAction) -> Unit = {},
    navController: NavController = rememberNavController(),
) {
    var textfieldEnabled by rememberSaveable { mutableStateOf(true) }
    var usernameState by rememberSaveable { mutableStateOf("") }
    var buttonEnabled by rememberSaveable { mutableStateOf(true) }
    var textfieldCleared by rememberSaveable { mutableStateOf(false) }

    val onUpdateUsername: (String) -> Unit = { username ->
        textfieldEnabled = false
        buttonEnabled = false
        sharedAction(SharedAction.Navigate(OnboardDestination.Guide, navController))
        onboardAction(OnboardAction.UpdateUsername(username))
    }

    val onClear: () -> Unit = {
        usernameState = ""
        textfieldCleared = true
    }

    val validationRules =
        arrayOf(
            Regex("^.{0,10}$") to
                    stringResource(R.string.onboard_name_textfield_max_characters),
            Regex("^.{4,}$") to
                    stringResource(R.string.onboard_name_textfield_min_characters),
            Regex("^[\\p{L}0-9]+$") to
                    stringResource(R.string.onboard_name_textfield_unallowed_characters)
        )

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenHeightDp < configuration.screenWidthDp

    Column(
        modifier =
            modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .imePadding()
    ) {
        // ── Scrollable content ──
        Column(modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())) {
            // ── Edge-to-edge illustration (no horizontal padding) ──
            if (!isLandscape) {
                Image(
                    painter = painterResource(R.drawable.illustration_form),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                )
            }

            // ── Padded content below illustration ──
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(24.dp))

                // ── Header (title + subtitle only, no illustration) ──
                ScreenHeader(
                    title =
                        stringResource(
                            R.string.onboard_form_greet,
                            usernameState
                        ),
                    subtitle = stringResource(R.string.onboard_form_question),
                )

                Spacer(Modifier.height(36.dp))

                // ── Nickname text field ──
                TextFieldPrimary(
                    modifier =
                        Modifier
                                .fillMaxWidth()
                                .shake(
                                        !buttonEnabled,
                                        Orientation.Horizontal
                                ),
                    enabled = textfieldEnabled,
                    placeholder = R.string.onboard_name_placeholder,
                    label = R.string.onboard_name_label,
                    trailingIcon =
                        if (usernameState.isEmpty()) null
                        else R.drawable.ic_erase,
                    isLast = true,
                    regex = validationRules,
                    onDone = { username -> onUpdateUsername(username) },
                    onTrailingIconClicked = onClear,
                    cleared = textfieldCleared,
                    onValid = { isValid, username ->
                        buttonEnabled = isValid
                        usernameState = username
                        textfieldCleared = false
                    }
                )

                // ── Character counter hint ──
                if (usernameState.isNotEmpty() && usernameState.length < 4) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${usernameState.length}/4",
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                                .copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        // ── Sticky bottom CTA ──
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically { it / 2 },
        ) {
            Box(
                modifier =
                    Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                ButtonPrimary(
                    onClick = { onUpdateUsername(usernameState) },
                    enabled = buttonEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        text = stringResource(R.string.onboard_form_submit)
                    )
                    IconButton(
                        painter =
                            painterResource(
                                R.drawable.ic_arrow_forward
                            ),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FormPreview() {
    com.example.quiz_game.ui.shared.component.Preview { Form() }
}
