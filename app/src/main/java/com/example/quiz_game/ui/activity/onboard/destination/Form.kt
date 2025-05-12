package com.example.quiz_game.ui.activity.onboard.destination

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.TextButton
import com.example.quiz_game.ui.shared.component.TextFieldPrimary
import com.example.quiz_game.ui.viewmodel.OnboardAction
import com.example.quiz_game.ui.viewmodel.SharedAction

private const val TAG = "test1234 Form"

@Composable
fun Form(
    modifier: Modifier = Modifier,
    sharedAction: (SharedAction) -> Unit = {},
    onboardAction: (OnboardAction) -> Unit = {},
    navController: NavController = rememberNavController(),
) {
    var buttonEnabled by rememberSaveable {
        mutableStateOf(true)
    }
    var textfieldEnabled by rememberSaveable {
        mutableStateOf(true)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextFieldPrimary(
            modifier = modifier,
            enabled = textfieldEnabled,
            isLast = true,
            regex = arrayOf(
                Regex("^.{0,20}$") to "Nickname must be less than 20 characters",
                Regex("^.{3,}$") to "Nickname must be at least 3 characters",
                Regex("^[a-zA-Z0-9]+$") to "Nickname can only contain letters and numbers"
            ),
            onDone = { nickname ->
                textfieldEnabled = false
                Log.d(TAG, "Form: $nickname")
            },
            onValid = {
                buttonEnabled = it
            }
        )
        ButtonPrimary(
            onClick = {
                textfieldEnabled = false
            },
            enabled = buttonEnabled,
        ) {
            TextButton(text = "submit")
        }
    }
}