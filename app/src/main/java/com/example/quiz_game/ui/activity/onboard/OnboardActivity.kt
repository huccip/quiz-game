package com.example.quiz_game.ui.activity.onboard

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.App
import com.example.quiz_game.AppDestination
import com.example.quiz_game.BaseActivity
import com.example.quiz_game.data.user.User
import com.example.quiz_game.ui.activity.onboard.destination.Form
import com.example.quiz_game.ui.activity.onboard.destination.Guide
import com.example.quiz_game.ui.activity.onboard.destination.Language
import com.example.quiz_game.ui.theme.QuizgameTheme
import com.example.quiz_game.ui.viewmodel.OnboardViewModel
import com.example.quiz_game.ui.viewmodel.SharedViewModel
import kotlinx.serialization.Serializable

class OnboardActivity : BaseActivity() {

    val onboardViewModel by viewModels<OnboardViewModel>()
    val sharedViewModel by viewModels<SharedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val navController = rememberNavController()

            QuizgameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = when {
                                !App.userPrefs.contains(User.KEY_SELECTED_LANGUAGE) -> OnboardDestination.Language
                                !App.userPrefs.contains(User.KEY_USERNAME) -> OnboardDestination.Form
                                else -> OnboardDestination.Guide
                            }
                        ) {
                            composable<OnboardDestination.Form> {
                                Form(
                                    sharedAction = sharedViewModel::onAction,
                                    onboardAction = onboardViewModel::onAction,
                                    navController = navController
                                )
                            }

                            composable<OnboardDestination.Guide> {
                                Guide(
                                    sharedAction = sharedViewModel::onAction,
                                    onboardAction = onboardViewModel::onAction
                                )
                            }

                            composable<OnboardDestination.Language> {
                                Language(
                                    sharedAction = sharedViewModel::onAction,
                                    onboardAction = onboardViewModel::onAction,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed interface OnboardDestination : AppDestination {
    @Serializable
    data object Form : OnboardDestination

    @Serializable
    data object Guide : OnboardDestination

    @Serializable
    data object Language : OnboardDestination
}