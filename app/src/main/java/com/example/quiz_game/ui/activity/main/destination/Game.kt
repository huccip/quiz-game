package com.example.quiz_game.ui.activity.main.destination

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.data.quiz.Quiz
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.ButtonGameChoices
import com.example.quiz_game.ui.shared.ButtonPrimary
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// TODO: Fix

private const val TAG = "test1234 Game"

@Composable
fun Game(
    modifier: Modifier = Modifier,
    categoryName: String? = null,
    quizState: QuizState = QuizState(),
    quizAction: (QuizAction) -> Unit = {},
    sharedAction: (SharedAction) -> Unit = {},
    navController: NavController = rememberNavController(),
) {
    val context = LocalContext.current
    var currentQuizIndex by rememberSaveable { mutableIntStateOf(0) }

    // Filter quizzes by category
    val quizzes = categoryName?.let { category ->
        quizState.quizzes.filter { it.category == category }
    } ?: quizState.quizzes

    // Debugging output to check the quizzes data
    if (quizzes.isEmpty()) {
        Log.e("test1234 Game", "Quizzes list is empty. categoryName: $categoryName, quizState: ${quizState.quizzes}")
    }

    // Ensure that quizzes is not empty and currentQuizIndex is within valid bounds
    if (quizzes.isEmpty() || currentQuizIndex >= quizzes.size) {
        Text("No quizzes available or index is invalid.")
        return
    }

    val quiz = quizzes[currentQuizIndex]
    val choices = buildList {
        quiz.correctAnswer?.let { add(it) }
        quiz.incorrectAnswers?.let { addAll(it) }
    }.shuffled() // Randomize the choices

    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var lastMark by remember { mutableIntStateOf(0) }
    var incorrectlyAnswered by remember { mutableIntStateOf(0) }

    // QuizCard composable will handle the display of the question and choices
    QuizCard(
        categoryName = quiz.category,
        question = quiz.question.orEmpty(),
        choices = choices,
        isLastQuestion = currentQuizIndex == quizzes.lastIndex,
        onAnswerSelected = { selectedAnswer = it },
        quizzes = quizzes,
        currentQuizIndex = currentQuizIndex,
        correctAnswer = quiz.correctAnswer,
        onConfirm = { answer ->
            val correct = quiz.correctAnswer
            val markEarned = if (answer == correct) quiz.mark ?: 0 else -(quiz.mark ?: 0)

            lastMark = markEarned
            showFeedback = true

            if (answer != correct) incorrectlyAnswered += 1
            sharedAction(SharedAction.UpdateScore(markEarned))
            quizAction(QuizAction.UpdateExpired(quiz.uid))

            // Handle the logic to move to the next question or finish the quiz
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)  // Delay for feedback visibility
                selectedAnswer = null
                showFeedback = false
                if (currentQuizIndex < quizzes.lastIndex) {
                    currentQuizIndex++  // Move to the next question
                } else {
                    // Once all questions are answered, finish the quiz
                    sharedAction(SharedAction.FinishQuizSession(context, incorrectlyAnswered))

                    // Navigate to the PostGame screen
                    sharedAction(
                        SharedAction.Navigate(
                            MainDestination.PostGame(quizzes.fastMap { it.uid }),
                            navController
                        )
                    )
                }
            }
        }
    )
}

@Composable
fun QuizCard(
    modifier: Modifier = Modifier,
    categoryName: String? = null,
    question: String,
    choices: List<String>,
    correctAnswer: String? = null,
    onAnswerSelected: (String) -> Unit,
    onConfirm: (String) -> Unit,
    sharedState: SharedState = SharedState(),
    isLastQuestion: Boolean = false,
    quizzes: List<Quiz> = emptyList(),
    currentQuizIndex: Int = 0
) {
    var selectedAnswer by rememberSaveable { mutableStateOf<String?>(null) }
    var isAnswered by rememberSaveable { mutableStateOf(false) }
    var timeRemaining by rememberSaveable { mutableIntStateOf(15) }
    var translatedTimerText by remember { mutableStateOf("") }

    // Reset when the question changes
    LaunchedEffect(question) {
        isAnswered = false
        selectedAnswer = null
        timeRemaining = 15
    }

    // Countdown timer
    LaunchedEffect(timeRemaining, isAnswered) {
        if (timeRemaining > 0 && !isAnswered) {
            delay(1000L)
            timeRemaining--
        } else if (timeRemaining == 0 && !isAnswered) {
            isAnswered = true // reveal correct answers, don't confirm yet
        }

        translatedTimerText =
            sharedState.translator?.translate("Time remaining: $timeRemaining seconds")?.await()
                ?: "Time remaining: $timeRemaining seconds"
    }

    val showAnswers = isAnswered
    val buttonText = when {
        isLastQuestion && showAnswers -> "Finish"
        showAnswers -> "Next question"
        selectedAnswer != null -> "Confirm my choice"
        else -> "Waiting..."
    }

    val choiceStates = choices.associateWith { it == correctAnswer }

    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            categoryName?.let {
                TextFancy(text = it, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            TextBig(text = question, modifier = Modifier.padding(bottom = 16.dp))

            choices.forEach { choice ->
                ButtonGameChoices(
                    onClick = {
                        if (!isAnswered && timeRemaining > 0) {
                            selectedAnswer = choice
                            onAnswerSelected(choice)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    reveal = showAnswers,
                    isCorrectChoice = choiceStates[choice] == true,
                    isSelected = selectedAnswer == choice,
                    enabled = !showAnswers
                ) {
                    TextButton(text = choice)
                }
            }

            TextSmol(
                text = translatedTimerText,
                color = if (timeRemaining <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Show score feedback after confirming (only if user answered)
            if (showAnswers && selectedAnswer != null) {
                val currentQuiz = quizzes.getOrNull(currentQuizIndex)
                val mark = currentQuiz?.mark ?: 0
                val markText = if (selectedAnswer == correctAnswer) "+$mark" else "-$mark"
                val color = if (selectedAnswer == correctAnswer) Color.Green else MaterialTheme.colorScheme.error

                TextBig(
                    text = markText,
                    color = color,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            ButtonPrimary(
                onClick = {
                    if (!isAnswered && selectedAnswer != null) {
                        isAnswered = true
                        onConfirm(selectedAnswer ?: "")
                    } else if (showAnswers) {
                        onConfirm(selectedAnswer ?: "")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedAnswer != null || showAnswers
            ) {
                TextButton(text = buttonText)
            }
        }
    }
}
