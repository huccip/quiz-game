package com.example.quiz_game.ui.activity.onboard.destination

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import com.example.quiz_game.App
import com.example.quiz_game.data.user.User
import com.example.quiz_game.ui.activity.main.MainActivity
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.TextButton
import com.example.quiz_game.ui.viewmodel.OnboardAction
import com.example.quiz_game.ui.viewmodel.SharedAction

@Composable
fun Guide(
    modifier: Modifier = Modifier,
    onboardAction: (OnboardAction) -> Unit = {},
    sharedAction: (SharedAction) -> Unit = {}
) {
    val context = LocalContext.current

    ButtonPrimary(onClick = {
        sharedAction(
            SharedAction.StartActivity(
                context,
                MainActivity::class.java
            )
        )

        App.userPrefs.edit {
            putBoolean(User.KEY_ONBOARDED, true)
            commit()
        }
    }) { TextButton(text = "Continue") }
}