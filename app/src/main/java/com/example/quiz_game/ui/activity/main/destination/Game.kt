package com.example.quiz_game.ui.activity.main.destination

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
    navController: NavController = rememberNavController(),
) {
    if (quizState.executing.or(sharedState.executing).or(categoryState.executing)) {
        LoadingInfiniteLine(subject = arrayOf(stringResource(R.string.loading_quizzes_by_category)))
    } else {
        val context = LocalContext.current
        var currentQuizIndex by rememberSaveable { mutableIntStateOf(0) }

        val quizzes = categoryName?.let { category ->
            quizState.quizzes.filter { it.category == category }
        } ?: quizState.quizzes

        var selectedAnswer by remember { mutableStateOf<String?>(null) }
        var showFeedback by remember { mutableStateOf(false) }
        var lastMark by remember { mutableIntStateOf(0) }
        var incorrectlyAnswered by remember { mutableIntStateOf(0) }

        QuizCard(
            categoryName = quizzes[currentQuizIndex].category,
            question = quizzes[currentQuizIndex].question.orEmpty(),
            choices = listOf(quizzes[currentQuizIndex].correctAnswer.orEmpty()) + (quizzes[currentQuizIndex].incorrectAnswers
                ?: emptyList()),
            isLastQuestion = currentQuizIndex == quizzes.size - 1,
            onAnswerSelected = { selectedAnswer = it },
            quizzes = quizzes,
            currentQuizIndex = currentQuizIndex,
            correctAnswer = quizzes[currentQuizIndex].correctAnswer,
            onConfirm = { answer ->
                val currentQuiz = quizzes[currentQuizIndex]
                val correct = currentQuiz.correctAnswer
                val markEarned =
                    if (answer == correct) currentQuiz.generateMark() else -currentQuiz.generateMark()

                lastMark = markEarned
                showFeedback = true

                if (answer != correct) incorrectlyAnswered += 1
                sharedAction(SharedAction.UpdateScore(context, markEarned, incorrectlyAnswered))

                // Delay to show feedback before proceeding
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    selectedAnswer = null
                    showFeedback = false
                    if (currentQuizIndex < quizzes.lastIndex) {
                        currentQuizIndex++
                    } else {
                        sharedAction(SharedAction.Navigate(MainDestination.PostGame, navController))
                    }
                }
            }
        )
    }
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
    var timeRemaining by rememberSaveable { mutableIntStateOf(10) }
    var translatedTimerText by remember { mutableStateOf("") }

    // Reset isAnswered state when the question changes (every time currentQuizIndex updates)
    LaunchedEffect(key1 = question) {
        isAnswered = false
        selectedAnswer = null
        timeRemaining = 15 // Reset time for each new question
    }

    // TIMER
    LaunchedEffect(timeRemaining, isAnswered) {
        if (timeRemaining > 0 && !isAnswered) {
            delay(1000L)
            timeRemaining -= 1
        }

        translatedTimerText =
            sharedState.translator?.translate("Time remaining: $timeRemaining seconds")?.await()
                ?: "Time remaining: $timeRemaining seconds"
    }

    val showAnswers = isAnswered || timeRemaining == 0
    val buttonText = when {
        isLastQuestion -> "Finish"
        showAnswers -> "Next question"
        selectedAnswer != null -> "Confirm my choice"
        else -> "Waiting..."
    }

    // Track the correct and wrong answers states after confirmation
    val choiceStates = choices.associate { choice ->
        choice to (choice == correctAnswer)
    }

    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
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
                    isCorrectChoice = choiceStates[choice] == true, // Only true if this choice is correct
                    isSelected = selectedAnswer == choice,
                    enabled = !showAnswers // Prevent selection after confirmation
                ) {
                    TextButton(text = choice)
                }
            }

            // Show Timer Text
            TextSmol(
                text = translatedTimerText,
                color = if (timeRemaining <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Show Score Overlay
            if (isAnswered && selectedAnswer != null) {
                val markText = if (quizzes.isNotEmpty() && currentQuizIndex < quizzes.size) {
                    val currentQuiz = quizzes[currentQuizIndex]
                    if (selectedAnswer == currentQuiz.correctAnswer) {
                        "+${currentQuiz.generateMark()}"
                    } else {
                        "-${currentQuiz.generateMark()}"
                    }
                } else {
                    "Error: Invalid question index"
                }
                TextBig(
                    text = markText,
                    color = if (selectedAnswer == correctAnswer) Color.Green else MaterialTheme.colorScheme.error,
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
