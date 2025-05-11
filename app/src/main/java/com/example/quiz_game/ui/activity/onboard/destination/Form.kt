package com.example.quiz_game.ui.activity.onboard.destination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.App
import com.example.quiz_game.R
import com.example.quiz_game.ui.activity.main.MainActivity
import com.example.quiz_game.ui.activity.onboard.OnboardDestination
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.TextButton
import com.example.quiz_game.ui.viewmodel.OnboardAction
import com.example.quiz_game.ui.viewmodel.SharedAction

@Composable
fun Form(
    modifier: Modifier = Modifier,
    sharedAction: (SharedAction) -> Unit = {},
    onboardAction: (OnboardAction) -> Unit = {},
    navController: NavController = rememberNavController(),
) {
    val context = LocalContext.current
    var nickname by rememberSaveable {
        mutableStateOf("")
    }
    var avatarDrawable by rememberSaveable {
        mutableIntStateOf(R.drawable.ic_launcher_foreground)
    }
    var avatarString by rememberSaveable {
        mutableIntStateOf(R.string.app_name)
    }
    LaunchedEffect(Unit) {
        if (App.userPrefs.contains("onboarded") && App.userPrefs.getBoolean("onboarded", true)) {
            sharedAction(SharedAction.StartActivity(context, MainActivity::class.java))
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = nickname,
            onValueChange = { nickname = it }
        )

        ButtonPrimary(
            onClick = {
                onboardAction(OnboardAction.Submit(nickname, avatarDrawable, avatarString))
                sharedAction(SharedAction.Navigate(OnboardDestination.Guide, navController))
            },
            content = {
                TextButton(
                    text = stringResource(R.string.onboard_button_letsgo)
                )
            }
        )
    }
}