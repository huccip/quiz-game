package com.example.quiz_game.ui.activity.main.destination

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.quiz_game.ui.shared.ButtonPrimary
import com.example.quiz_game.ui.shared.ButtonSecondary
import com.example.quiz_game.ui.shared.LoadingInfiniteLine
import com.example.quiz_game.ui.shared.TextBig
import com.example.quiz_game.ui.shared.TextButton
import com.example.quiz_game.ui.shared.TextFancy
import com.example.quiz_game.ui.shared.TextSmol
import com.example.quiz_game.ui.viewmodel.CategoryState
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedState
import kotlinx.coroutines.delay

// TODO: Fix the button behavior and track the score (log mark per question)

private const val TAG = "test1234 Game"

@Composable
fun Game(
    modifier: Modifier = Modifier,
    categoryName: String? = null,
    quizState: QuizState = QuizState(),
    quizAction: (QuizAction) -> Unit = {},
    sharedAction: (SharedAction) -> Unit = {},
    sharedState: SharedState = SharedState(),
    categoryState: CategoryState = CategoryState(),
) {
    if (quizState.executing.or(sharedState.executing).or(categoryState.executing)) {
        LoadingInfiniteLine(subject = arrayOf("test"))
    } else {
        val context = LocalContext.current
        var currentQuizIndex by rememberSaveable { mutableIntStateOf(0) }

        val quizzes = categoryName?.let { category ->
            quizState.quizzes.filter {
                it.category == category
            }
        } ?: quizState.quizzes

        Log.d(TAG, "Game: ${quizzes.joinToString("\n")}")

        var selectedAnswer by remember {
            mutableStateOf("")
        }
        var mark by remember {
            mutableIntStateOf(0)
        }
        var incorrectlyAnswered by remember {
            mutableIntStateOf(0)
        }

        var disableQuizOnTimeout by remember {
            mutableStateOf(false)
        }
        QuizCard(
            categoryName = quizzes[currentQuizIndex].category,
            question = quizzes[currentQuizIndex].question.orEmpty(),
            choices = listOf(quizzes[currentQuizIndex].correctAnswer.orEmpty()) + (quizzes[currentQuizIndex].incorrectAnswers
                ?: emptyList()),
            onAnswerSelected = {
                selectedAnswer = it
                quizzes[currentQuizIndex].incorrectAnswers?.let { incorrects ->
                    if (incorrects.contains(it)) {
                        incorrectlyAnswered += 1
                        mark -= quizzes[currentQuizIndex].generateMark()
                    } else {
                        mark += quizzes[currentQuizIndex].generateMark()
                    }
                }
            },
            onConfirm = {
                sharedAction(SharedAction.UpdateScore(context, mark, incorrectlyAnswered))
                currentQuizIndex += 1
            },
        )
    }
}

@Composable
fun QuizCard(
    modifier: Modifier = Modifier,
    categoryName: String? = null,
    question: String,
    choices: List<String>,
    onAnswerSelected: (String) -> Unit,
    onConfirm: (String) -> Unit,
) {
    var selectedAnswer by rememberSaveable { mutableStateOf<String?>(null) }
    var timeRemaining by rememberSaveable { mutableIntStateOf(10) } // Timer starts at 10 seconds
    var isAnswered by rememberSaveable { mutableStateOf(false) }
    var disableQuizOnTimeout by rememberSaveable { mutableStateOf(false) }

    // Timer logic
    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0 && !isAnswered) {
            delay(1000L) // Wait for 1 second
            timeRemaining -= 1
        } else if (timeRemaining == 0 && !isAnswered) {
            // Time up, auto-confirm answer
            disableQuizOnTimeout = true
        }
    }

    // When the answer is selected, update the state
    fun onSelectAnswer(answer: String) {
        if (!isAnswered) {
            selectedAnswer = answer
            onAnswerSelected(answer)
        }
    }

    // Determine button text based on whether the question is answered
    val buttonText =
        if (isAnswered) "Done" else if (timeRemaining == 0) "Next question" else "Confirm my answer"

    // Layout for the Quiz Card
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Category Name (only show if it's not null)
            categoryName?.let {
                TextFancy(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Question
            TextBig(
                text = question,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Choices (Buttons)
            choices.forEach { choice ->
                ButtonPrimary(
                    onClick = { onSelectAnswer(choice) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    TextButton(text = choice)
                }
            }

            // Timer
            TextSmol(
                text = "Time remaining: $timeRemaining seconds",
                color = if (timeRemaining <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Confirm Button
            Spacer(modifier = Modifier.height(16.dp))
            ButtonSecondary(
                onClick = {
                    if (selectedAnswer != null) {
                        isAnswered = true
                        onConfirm(selectedAnswer ?: "")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = disableQuizOnTimeout
            ) {
                TextButton(text = buttonText)
            }
        }
    }
}
