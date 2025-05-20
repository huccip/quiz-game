package com.example.quiz_game.ui.activity.onboard.destination

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.example.quiz_game.ui.shared.component.LoadingInfiniteLine
import com.example.quiz_game.ui.shared.component.TextButton
import com.example.quiz_game.ui.shared.component.TextFancy
import com.example.quiz_game.ui.shared.component.TextFieldPrimary
import com.example.quiz_game.ui.viewmodel.OnboardAction
import com.example.quiz_game.ui.viewmodel.OnboardState
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedState

private const val TAG = "test1234 Form"

@Composable
fun Form(
    modifier: Modifier = Modifier,
    sharedAction: (SharedAction) -> Unit = {},
    sharedState: SharedState = SharedState(),
    onboardState: OnboardState = OnboardState(),
    onboardAction: (OnboardAction) -> Unit = {},
    navController: NavController = rememberNavController(),
) {
    LaunchedEffect(onboardAction) {
        if (!onboardState.user.username.isNullOrEmpty()) {
            sharedAction(SharedAction.Navigate(OnboardDestination.Guide, navController))
        }
    }

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

    val validationRules = arrayOf(
        Regex("^.{0,10}$") to stringResource(R.string.onboard_name_textfield_max_characters),
        Regex("^.{4,}$") to stringResource(R.string.onboard_name_textfield_min_characters),
        Regex("^[\\p{L}0-9]+$") to stringResource(R.string.onboard_name_textfield_unallowed_characters)
    )

    if (sharedState.executing) {
        LoadingInfiniteLine(subject = arrayOf(stringResource(R.string.onboard_form_loading_subject)))
    } else {
        Column(modifier = modifier) {
            TextFancy(
                text = stringResource(
                    R.string.onboard_form_greet,
                    if (!buttonEnabled) "you" else usernameState
                ),
            )
            Spacer(Modifier.height(5.dp))
            TextFancy(
                text = stringResource(R.string.onboard_form_question),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(25.dp))

            TextFieldPrimary(
                modifier = Modifier.fillMaxWidth().shake(!buttonEnabled, Orientation.Horizontal),
                enabled = textfieldEnabled,
                placeholder = R.string.onboard_name_placeholder,
                label = R.string.onboard_name_label,
                trailingIcon = if (usernameState.isEmpty()) null else R.drawable.ic_erase,
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

            Spacer(Modifier.height(25.dp))

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                ButtonPrimary(
                    onClick = { onUpdateUsername(usernameState) },
                    enabled = buttonEnabled,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    TextButton(text = stringResource(R.string.onboard_form_submit))
                    IconButton(
                        painter = painterResource(R.drawable.ic_arrow_forward),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FormPreview() {
    com.example.quiz_game.ui.shared.component.Preview {
        Form()
    }
}