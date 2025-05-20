package com.example.quiz_game.ui.activity.onboard.destination

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.ui.activity.main.MainActivity
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.LoadingInfiniteLine
import com.example.quiz_game.ui.viewmodel.OnboardAction
import com.example.quiz_game.ui.viewmodel.OnboardState
import com.example.quiz_game.ui.viewmodel.SharedAction

@Composable
fun Guide(
    modifier: Modifier = Modifier,
    onboardAction: (OnboardAction) -> Unit = {},
    sharedAction: (SharedAction) -> Unit = {},
    onboardState: OnboardState = OnboardState(),
    navController: NavController = rememberNavController()
) {
    val context = LocalContext.current

    LaunchedEffect(onboardAction) {
        if (onboardState.user.onboarded) {
            sharedAction(SharedAction.StartActivity(context, MainActivity::class.java))
        }
    }

    if (onboardState.executing) {
        LoadingInfiniteLine(subject = arrayOf("Guide"))
    } else {
        ButtonPrimary(
            modifier = modifier,
            enabled = true,
            onClick = {
                onboardAction(OnboardAction.UpdateOnboarded)
                sharedAction(SharedAction.StartActivity(context, MainActivity::class.java))
            }
        ) {
            Text("Continue")
        }
    }
}