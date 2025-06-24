package com.example.quiz_game.ui.activity.main.destination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastSumBy
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.data.quiz.Quiz
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.CardSelectable
import com.example.quiz_game.ui.shared.component.IconButton
import com.example.quiz_game.ui.shared.component.LoadingInfiniteLine
import com.example.quiz_game.ui.shared.component.TextRegular
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.SessionAction
import com.example.quiz_game.ui.viewmodel.SessionState
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedState
import kotlinx.coroutines.async

private const val TAG = "test1234 Game"

// TODO: remake

@Composable
fun Game(
    modifier: Modifier = Modifier,
    quizzesUids: List<String> = emptyList(),
    categoryUid: String? = null,
    quizState: QuizState = QuizState(),
    quizAction: (QuizAction) -> Unit = {},
    sharedAction: (SharedAction) -> Unit = {},
    sharedState: SharedState = SharedState(),
    sessionState: SessionState = SessionState(),
    sessionAction: (SessionAction) -> Unit = {},
    navController: NavController = rememberNavController(),
) {
    val session = sessionState.session
    var quizzes = quizState.sessionQuizzes
    var renderUi by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (session.createdAt == null || session.expiredAt != null) { // no session or expired session, start new
            async { sessionAction(SessionAction.InitiateSession(quizzesUids = quizzesUids, categoryUid = categoryUid)) }.await()
            async { sessionAction(SessionAction.GetAll) }.await()
        }

        async {
            quizAction(
                QuizAction.GetBySession(
                    uids = quizzesUids,
                    translator = sharedState.translator
                )
            )
        }.await()
    }

    LaunchedEffect(session, quizzes) {
        renderUi = (session.createdAt != null) && (quizzes.isNotEmpty())

        if (renderUi) {
            sessionAction(
                SessionAction.UpdateMaxScore(
                    uid = session.uid,
                    maxScore = quizzes.fastSumBy { it.mark!! })
            )
            return@LaunchedEffect
        }
    }

    when {
        sessionState.executing && quizState.executing -> {
            LoadingInfiniteLine(subject = arrayOf("session"))
        }

        renderUi -> {
            var index by rememberSaveable { mutableIntStateOf(0) }
            val onNext = {
                quizAction(QuizAction.UpdateExpired(quizzes[index].uid))

                if (index == quizzes.lastIndex) {
                    sessionAction(SessionAction.EndSession(session.uid))
                    for (quiz in quizzes) {
                        quizAction(QuizAction.DeleteByUid(quiz.uid))
                    }
                    navController.navigate(MainDestination.PostGame)
                } else index += 1
            }

            Box {
                val quiz = remember(index) { quizzes[index] }

                if (quiz.expired) {
                    onNext()
                } else {
                    QuizCard(
                        modifier = Modifier.fillMaxSize(),
                        quiz = quiz,
                        onAnswered = {
                            println("test1234 $it")
                        },
                        onNext = onNext,
                        lastIndex = index == quizzes.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
fun QuizCard(
    modifier: Modifier = Modifier,
    quiz: Quiz,
    onAnswered: (Boolean) -> Unit,
    onNext: () -> Unit,
    lastIndex: Boolean = false,
) {
    val question = remember(quiz) { quiz.question }
    val answers = remember(quiz) {
        buildList {
            addAll(quiz.incorrectAnswers!!)
            add(quiz.correctAnswer!!)
            shuffle()
        }
    }

    var answeredState by remember(quiz) { mutableStateOf(AnsweredState.IDLE) }

    val buttonText = when (answeredState) {
        AnsweredState.IDLE -> "Waiting..."
        AnsweredState.PICKED -> if (lastIndex) "Finish" else "Next"
        AnsweredState.LOCKED -> if (lastIndex) "Finished" else "Locked"
    }

    var selectedAnswer by rememberSaveable(quiz) { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { TextRegular(text = question!!) }

        items(items = answers, key = { it.hashCode() }) { answer ->
            CardSelectable(
                onSelect = {
                    answeredState = AnsweredState.PICKED
                    selectedAnswer = answer
                    onAnswered(answer == quiz.correctAnswer)
                },
                selected = answer == selectedAnswer,
            ) {
                TextRegular(text = answer)

                if (answeredState == AnsweredState.PICKED && answer == selectedAnswer) {
                    IconButton(painter = painterResource(R.drawable.ic_pin))
                }
            }
        }

        item {
            ButtonPrimary(onClick = onNext, enabled = answeredState == AnsweredState.PICKED) {
                TextRegular(text = buttonText, color = MaterialTheme.colorScheme.surface)
            }
        }
    }
}

enum class AnsweredState {
    IDLE, PICKED, LOCKED
}