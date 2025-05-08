package com.example.quiz_game.ui.activity.main.destination

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.data.quiz.Quiz
import com.example.quiz_game.other.Constants
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.ButtonGameChoices
import com.example.quiz_game.ui.shared.ButtonPrimary
import com.example.quiz_game.ui.shared.TextBig
import com.example.quiz_game.ui.shared.TextButton
import com.example.quiz_game.ui.shared.TextFancy
import com.example.quiz_game.ui.shared.TextSmol
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.SharedAction
import kotlinx.coroutines.delay

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

    val quizzes = (categoryName?.let { category ->
        quizState.quizzes.filter { it.category == category }
    } ?: quizState.quizzes)
        .take(Constants.DEFAULT_QUIZ_SESSION_AMOUNT)
        .filter { !it.expired }

    if (quizzes.isEmpty()) {
        Text("No quizzes found for this category.")
        return
    }

    var quizIndex by rememberSaveable { mutableIntStateOf(0) }
    var incorrectlyAnswered by rememberSaveable { mutableIntStateOf(0) }
    val quiz = quizzes[quizIndex]
    val choices = remember(quiz.uid) {
        buildList {
            quiz.correctAnswer?.let { add(it) }
            quiz.incorrectAnswers?.let { addAll(it) }
        }.shuffled()
    }

    QuizCard(
        quiz = quiz,
        choices = choices,
        correctChoice = quiz.correctAnswer,
        onAnswered = { answer, mark ->
            quiz.incorrectAnswers?.let { incorrects ->
                if (incorrects.contains(answer)) incorrectlyAnswered += 1
            }

            // update this session's score
            sharedAction(SharedAction.UpdateScore(mark))

            // update quiz expiration
            quizAction(QuizAction.UpdateExpired(quiz.uid))

            if (quizIndex >= quizzes.lastIndex) {
                // update achievements
                sharedAction(
                    SharedAction.UpdateTrophies(
                        context,
                        incorrectlyAnswered
                    )
                )

                // end session
                sharedAction(SharedAction.Navigate(MainDestination.PostGame, navController))

                return@QuizCard
            }

            quizIndex += 1
        }
    )
}

@Composable
fun QuizCard(
    modifier: Modifier = Modifier,
    quiz: Quiz = Quiz(),
    choices: List<String> = emptyList<String>(),
    correctChoice: String? = null,
    onAnswered: (String, Int) -> Unit = { str, int -> }, // send selected answer and its mark
) {
    var answer by rememberSaveable { mutableStateOf("") }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var answeredState by rememberSaveable { mutableStateOf(AnsweredState.IDLE) }

    var timer by rememberSaveable { mutableIntStateOf(Constants.DEFAULT_QUIZ_TIMER) }

    LaunchedEffect(quiz.uid) {
        timer = Constants.DEFAULT_QUIZ_TIMER
        enabled = true
        answer = ""
        answeredState = AnsweredState.IDLE
    }

    LaunchedEffect(answeredState) {
        if (answeredState == AnsweredState.LOCKED) {
            delay(1500L)
            onAnswered(answer, if (answer == correctChoice) +quiz.mark!! else -quiz.mark!!)
        }
    }

    LaunchedEffect(timer) {
        if (answeredState != AnsweredState.LOCKED) {
            if (timer > 0) {
                delay(1000L)
                timer -= 1
            } else {
                // same logic as if clicked "confirm"
                enabled = false
                answeredState = AnsweredState.LOCKED
            }
        }
    }

    Card(Modifier.padding(16.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            quiz.category?.let {
                TextFancy(text = it, color = MaterialTheme.colorScheme.primary)

                Spacer(Modifier.height(16.dp))
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { timer.toFloat() / Constants.DEFAULT_QUIZ_TIMER },
                    color = when (timer) {
                        in Constants.DEFAULT_QUIZ_TIMER / 2..Constants.DEFAULT_QUIZ_TIMER -> MaterialTheme.colorScheme.primary
                        in Constants.DEFAULT_QUIZ_TIMER / 6..Constants.DEFAULT_QUIZ_TIMER / 2 -> Color.Yellow
                        else -> MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                TextSmol(
                    text = "$timer",
                    color = when (timer) {
                        in Constants.DEFAULT_QUIZ_TIMER / 2..Constants.DEFAULT_QUIZ_TIMER -> MaterialTheme.colorScheme.primary
                        in Constants.DEFAULT_QUIZ_TIMER / 6..Constants.DEFAULT_QUIZ_TIMER / 2 -> Color.Yellow
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            quiz.question?.let {
                TextBig(text = it)

                Spacer(Modifier.height(16.dp))
            }

            if (choices.isNotEmpty()) {
                choices.onEach {
                    ButtonGameChoices(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            answer = it
                            answeredState = AnsweredState.PICKED
                        },
                        enabled = enabled && answer != it,
                        isCorrectChoice = correctChoice == it,
                        answeredState = answeredState
                    ) {
                        TextButton(text = it, modifier = Modifier.fillMaxWidth())
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            if (answeredState == AnsweredState.LOCKED) {
                AnimatedVisibility(
                    visible = answeredState == AnsweredState.LOCKED,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(750, easing = EaseInExpo)
                    ) + fadeIn(initialAlpha = 0.25f),
                    exit = slideOutVertically(
                        targetOffsetY = { it / 2 },
                        animationSpec = tween(750, easing = EaseInExpo)
                    ) + fadeOut()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextBig(
                            text = "${if (answer == correctChoice) "+" else "-"}${quiz.mark}",
                            color = if (answer == correctChoice) Color.Green else MaterialTheme.colorScheme.error
                        )

                        Spacer(Modifier.height(16.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            ButtonPrimary(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    enabled = false
                    answeredState = AnsweredState.LOCKED
                },
                enabled = answeredState == AnsweredState.PICKED
            ) {
                TextButton(
                    text = when (answeredState) {
                        AnsweredState.IDLE -> "Waiting..."
                        AnsweredState.PICKED -> "Confirm answer"
                        AnsweredState.LOCKED -> "Next question"
                    }
                )
            }
        }
    }
}

enum class AnsweredState {
    IDLE, PICKED, LOCKED
}