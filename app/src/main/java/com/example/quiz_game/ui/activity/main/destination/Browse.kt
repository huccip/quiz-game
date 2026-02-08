package com.example.quiz_game.ui.activity.main.destination

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastMap
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.data.category.Category
import com.example.quiz_game.other.Constants
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.LoadingFullScreenLowOpacityWithInfiniteSpinner
import com.example.quiz_game.ui.shared.effect.scaleDownOnPress
import com.example.quiz_game.ui.viewmodel.CategoryAction
import com.example.quiz_game.ui.viewmodel.CategoryState
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.SessionAction
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

@Composable
fun Browse(
    modifier: Modifier = Modifier,
    categoryState: CategoryState = CategoryState(),
    sharedState: SharedState = SharedState(),
    quizState: QuizState = QuizState(),
    quizStateFlow: StateFlow<QuizState>? = null,
    categoryAction: (CategoryAction) -> Unit = {},
    quizAction: (QuizAction) -> Unit = {},
    sharedAction: (SharedAction) -> Unit = {},
    sessionAction: (SessionAction) -> Unit = {},
    navController: NavController = rememberNavController()
) {
    val interactionSource = remember { MutableInteractionSource() }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    var loading by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(selectedCategory) {
        if (selectedCategory == null) return@LaunchedEffect
        if (quizStateFlow == null) return@LaunchedEffect

        loading = true
        quizAction(QuizAction.GetByCategory(categoryUid = selectedCategory!!.uid))

        // Collect the flow and wait for quizzes to be available
        quizStateFlow
            .first { state ->
                val quizzesForCategory =
                    state.quizzes.fastFilter { it.category == selectedCategory!!.name }
                // Wait until not executing AND we have the desired amount of quizzes for this category (check Constants.kt)
                !state.executing && quizzesForCategory.count() == Constants.DEFAULT_QUIZ_AMOUNT
            }
            .let { state ->
                val quizzesForCategory =
                    state.quizzes.fastFilter { it.category == selectedCategory!!.name && !it.expired }
                val sessionQuizzes = quizzesForCategory.take(Constants.DEFAULT_QUIZ_SESSION_AMOUNT)

                loading = false

                // Initiate the session with only the session quizzes
                sessionAction(
                    SessionAction.InitiateSession(
                        quizzesUids = sessionQuizzes.fastMap { it.uid },
                        maxScore = sessionQuizzes.sumOf { it.mark ?: 0 }
                    )
                )

                // Navigate
                sharedAction(
                    SharedAction.Navigate(
                        MainDestination.Game(
                            quizzesUids = sessionQuizzes.map { eachQuiz -> eachQuiz.uid }
                        ),
                        navController
                    )
                )
            }
    }

    if (loading) {
        LoadingFullScreenLowOpacityWithInfiniteSpinner()
    } else {
        LazyColumn {
            items(items = categoryState.categories, key = { it.uid }) { category ->
                ListItem(
                    headlineContent = {
                        Column {
                            Text(
                                category.name ?: "Undefined"
                            )
                            HorizontalDivider()
                        }
                    },
                    modifier = modifier
                        .fillMaxWidth()
                        .scaleDownOnPress(
                            interactionSource = interactionSource,
                            scaleRatio = .3f,
                        )
                        .clickable(
                            enabled = true,
                            onClick = { selectedCategory = category }
                        )
                )
            }
        }
    }
}