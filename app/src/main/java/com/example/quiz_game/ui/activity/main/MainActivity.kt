package com.example.quiz_game.ui.activity.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.quiz_game.AppDestination
import com.example.quiz_game.BaseActivity
import com.example.quiz_game.R
import com.example.quiz_game.other.NetworkConnectivityObserver
import com.example.quiz_game.other.Utils
import com.example.quiz_game.ui.activity.main.MainDestination.Home
import com.example.quiz_game.ui.activity.main.destination.Browse
import com.example.quiz_game.ui.activity.main.destination.Game
import com.example.quiz_game.ui.activity.main.destination.Home
import com.example.quiz_game.ui.activity.main.destination.PostGame
import com.example.quiz_game.ui.activity.onboard.destination.Language
import com.example.quiz_game.ui.theme.QuizgameTheme
import com.example.quiz_game.ui.viewmodel.CategoryViewModel
import com.example.quiz_game.ui.viewmodel.OnboardViewModel
import com.example.quiz_game.ui.viewmodel.QuizViewModel
import com.example.quiz_game.ui.viewmodel.SessionViewModel
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class MainActivity : BaseActivity() {

    val sharedViewModel by viewModels<SharedViewModel>()
    val categoryViewModel by viewModels<CategoryViewModel>()
    val quizViewModel by viewModels<QuizViewModel>()
    val sessionViewModel by viewModels<SessionViewModel>()
    val onboardViewModel by viewModels<OnboardViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        println("test1234 MainActivity onCreate()")

        setContent {
            val navController = rememberNavController()
            val sharedState by sharedViewModel.state.collectAsStateWithLifecycle()
            val quizState by quizViewModel.state.collectAsStateWithLifecycle()
            val categoryState by categoryViewModel.state.collectAsStateWithLifecycle()
            val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()
            val onboardState by onboardViewModel.state.collectAsStateWithLifecycle()

            val context = LocalContext.current
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            val onError: (String) -> Unit = { message ->
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(message)
                }
            }
            val connectivityObserver = remember { NetworkConnectivityObserver(context) }
            val initialNetworkStatus =
                    if (Utils.hasInternet()) NetworkConnectivityObserver.Status.Available
                    else NetworkConnectivityObserver.Status.Unavailable
            val networkStatus by
                    connectivityObserver
                            .observe()
                            .collectAsStateWithLifecycle(initialValue = initialNetworkStatus)
            var isInitialStatus by rememberSaveable { mutableStateOf(true) }

            val offlineMessage = stringResource(R.string.connectivity_offline)
            val onlineMessage = stringResource(R.string.connectivity_online)

            LaunchedEffect(networkStatus) {
                if (isInitialStatus) {
                    isInitialStatus = false
                    if (networkStatus == NetworkConnectivityObserver.Status.Lost ||
                                    networkStatus == NetworkConnectivityObserver.Status.Unavailable
                    ) {
                        snackbarHostState.showSnackbar(
                                offlineMessage,
                                duration = SnackbarDuration.Indefinite
                        )
                    }
                } else {
                    if (networkStatus == NetworkConnectivityObserver.Status.Available) {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(onlineMessage)

                        sharedViewModel.onAction(
                                SharedAction.PrepareTranslator(
                                        language = onboardState.user.language ?: "",
                                        context = context
                                )
                        )

                        // val currentCategoryState = categoryViewModel.state.value
                        // if (!currentCategoryState.executing &&
                        //     currentCategoryState.categories.isEmpty()
                        // ) {
                        //     categoryViewModel.onAction(
                        //         CategoryAction.GetAll(sharedState.translator)
                        //     )
                        // }

                        // val currentQuizState = quizViewModel.state.value
                        // if (!currentQuizState.executing) {
                        //     quizViewModel.onAction(QuizAction.GetAll)
                        // }
                    } else if (networkStatus == NetworkConnectivityObserver.Status.Lost ||
                                    networkStatus == NetworkConnectivityObserver.Status.Unavailable
                    ) {
                        snackbarHostState.showSnackbar(
                                offlineMessage,
                                duration = SnackbarDuration.Indefinite,
                                withDismissAction = true
                        )
                    }
                }
            }

            QuizgameTheme {
                Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                            Modifier.fillMaxSize().padding(paddingValues = innerPadding),
                            contentAlignment = Alignment.Center
                    ) {
                        NavHost(navController = navController, startDestination = Home) {
                            composable<Home> {
                                Home(
                                        quizState = quizState,
                                        quizAction = quizViewModel::onAction,
                                        categoryState = categoryState,
                                        sessionState = sessionState,
                                        sharedAction = sharedViewModel::onAction,
                                        navController = navController,
                                        sessionAction = sessionViewModel::onAction,
                                        onError = onError
                                )
                            }

                            composable<MainDestination.Game> {
                                Game(
                                        quizState = quizViewModel.state,
                                        quizzesUids =
                                                it.toRoute<MainDestination.Game>().quizzesUids,
                                        sharedState = sharedState,
                                        sharedAction = sharedViewModel::onAction,
                                        quizAction = quizViewModel::onAction,
                                        sessionState = sessionViewModel.state,
                                        sessionAction = sessionViewModel::onAction,
                                        navController = navController
                                )
                            }

                            composable<MainDestination.Browse> {
                                Browse(
                                        quizState = quizState,
                                        sharedState = sharedState,
                                        categoryState = categoryState,
                                        sharedAction = sharedViewModel::onAction,
                                        navController = navController,
                                        quizAction = quizViewModel::onAction,
                                        categoryAction = categoryViewModel::onAction,
                                        sessionAction = sessionViewModel::onAction,
                                        onError = onError
                                )
                            }

                            composable<MainDestination.Language> {
                                Language(
                                        sharedState = sharedState,
                                        sharedAction = sharedViewModel::onAction,
                                        onboardState = onboardState,
                                        onboardAction = onboardViewModel::onAction,
                                        navController = navController,
                                        fromOnboarding = false
                                )
                            }

                            composable<MainDestination.PostGame> {
                                PostGame(
                                        sharedAction = sharedViewModel::onAction,
                                        navController = navController,
                                        quizState = quizState,
                                        quizAction = quizViewModel::onAction,
                                        sessionState = sessionState,
                                        sessionAction = sessionViewModel::onAction,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed interface MainDestination : AppDestination {
    @Serializable data object Home : MainDestination

    @Serializable
    data class Game(val quizzesUids: List<String> = emptyList<String>()) : MainDestination

    @Serializable data object Browse : MainDestination

    @Serializable data object Language : MainDestination

    @Serializable data object PostGame : MainDestination
}
