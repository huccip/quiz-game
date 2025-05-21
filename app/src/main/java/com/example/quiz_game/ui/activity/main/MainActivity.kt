package com.example.quiz_game.ui.activity.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.quiz_game.AppDestination
import com.example.quiz_game.BaseActivity
import com.example.quiz_game.ui.activity.main.MainDestination.Home
import com.example.quiz_game.ui.activity.main.destination.Browse
import com.example.quiz_game.ui.activity.main.destination.Game
import com.example.quiz_game.ui.activity.main.destination.Home
import com.example.quiz_game.ui.activity.onboard.destination.Language
import com.example.quiz_game.ui.activity.main.destination.PostGame
import com.example.quiz_game.ui.activity.main.destination.Store
import com.example.quiz_game.ui.theme.QuizgameTheme
import com.example.quiz_game.ui.viewmodel.CategoryAction
import com.example.quiz_game.ui.viewmodel.CategoryViewModel
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizViewModel
import com.example.quiz_game.ui.viewmodel.SessionViewModel
import com.example.quiz_game.ui.viewmodel.SharedViewModel
import kotlinx.serialization.Serializable

private const val TAG = "test1234 MainActivity"

class MainActivity : BaseActivity() {

    val sharedViewModel by viewModels<SharedViewModel>()
    val categoryViewModel by viewModels<CategoryViewModel>()
    val quizViewModel by viewModels<QuizViewModel>()
    val sessionViewModel by viewModels<SessionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val navController = rememberNavController()
            val sharedState by sharedViewModel.state.collectAsStateWithLifecycle()
            val quizState by quizViewModel.state.collectAsStateWithLifecycle()
            val categoryState by categoryViewModel.state.collectAsStateWithLifecycle()
            val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(sharedState) {
                categoryViewModel.onAction(
                    CategoryAction.GetAll(sharedState.translator)
                )

                quizViewModel.onAction(
                    QuizAction.GetAll
                )
            }

            QuizgameTheme {
                Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues = innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Home
                        ) {
                            composable<Home> {
                                Home(
                                    sharedState = sharedState,
                                    quizState = quizState,
                                    categoryState = categoryState,
                                    sessionState = sessionState,
                                    sharedAction = sharedViewModel::onAction,
                                    navController = navController,
                                    sessionAction = sessionViewModel::onAction
                                )
                            }

                            composable<MainDestination.Game> {
                                Game(
                                    quizzesUids = it.toRoute<MainDestination.Game>().quizzesUids,
                                    quizState = quizState,
                                    sharedAction = sharedViewModel::onAction,
                                    quizAction = quizViewModel::onAction,
                                    sharedState = sharedState,
                                    sessionState = sessionState,
                                    sessionAction = sessionViewModel::onAction,
                                    navController = navController
                                )
                            }

                            composable<MainDestination.Browse> {
                                Browse(
                                    sharedState = sharedState,
                                    categoryState = categoryState,
                                    sharedAction = sharedViewModel::onAction,
                                    navController = navController,
                                    quizAction = quizViewModel::onAction,
                                    categoryAction = categoryViewModel::onAction
                                )
                            }

                            composable<MainDestination.Store> {
                                Store()
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

    override fun onDestroy() {
        super.onDestroy()

        quizViewModel.state.value.quizzes.fastFilter { it.expired }.fastForEach {
            quizViewModel.onAction(QuizAction.DeleteByUid(it.uid))
        }
    }
}

sealed interface MainDestination : AppDestination {
    @Serializable
    data object Home : MainDestination

    @Serializable
    data class Game(val quizzesUids: List<String> = emptyList<String>()) : MainDestination

    @Serializable
    data object Browse : MainDestination

    @Serializable
    data object PostGame : MainDestination

    @Serializable
    data object Store : MainDestination
}