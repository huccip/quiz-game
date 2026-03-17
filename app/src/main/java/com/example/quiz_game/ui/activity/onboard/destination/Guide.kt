package com.example.quiz_game.ui.activity.onboard.destination

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import com.example.quiz_game.App
import com.example.quiz_game.R
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

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ButtonPrimary(
            onClick = {
                sharedAction(SharedAction.StartActivity(context, MainActivity::class.java))

                App.userPrefs.edit {
                    putBoolean(User.KEY_ONBOARDED, true)
                    commit()
                }
            }
        ) { TextButton(text = stringResource(R.string.onboard_guide_continue)) }
    }
}
