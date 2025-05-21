package com.example.quiz_game.ui.activity.main.destination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.App
import com.example.quiz_game.AppDestination
import com.example.quiz_game.R
import com.example.quiz_game.data.Repository
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.Utils
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.ButtonSecondary
import com.example.quiz_game.ui.shared.component.DialogYesOrNo
import com.example.quiz_game.ui.shared.component.IconButton
import com.example.quiz_game.ui.shared.component.LoadingInfiniteLine
import com.example.quiz_game.ui.shared.component.TextButton
import com.example.quiz_game.ui.viewmodel.CategoryState
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.SessionAction
import com.example.quiz_game.ui.viewmodel.SessionState
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedState
import kotlinx.coroutines.tasks.await
import java.util.Locale

private const val TAG = "test1234 Home"

@Composable
fun Home(
    modifier: Modifier = Modifier,
    sharedState: SharedState = SharedState(),
    quizState: QuizState = QuizState(),
    categoryState: CategoryState = CategoryState(),
    sharedAction: (SharedAction) -> Unit = {},
    sessionState: SessionState = SessionState(),
    sessionAction: (SessionAction) -> Unit = {},
    navController: NavController = rememberNavController()
) {
    var confirmationTrigger by rememberSaveable { mutableStateOf(false) }
    var confirmationDestination: MainDestination? by rememberSaveable { mutableStateOf(null) }
    var openConfirmationDialog: (MainDestination) -> Unit = {
        confirmationTrigger = true
        confirmationDestination = it
    }

    if (sharedState.executing || quizState.executing || categoryState.executing) {
        LoadingInfiniteLine(subject = stringArrayResource(R.array.home_loading_subjects))
    } else {
        if (confirmationTrigger) {
            DialogYesOrNo(
                modifier = Modifier.fillMaxWidth(),
                title = R.string.dialog_discard_session_title,
                text = R.string.dialog_discard_session_text,
                icon = R.drawable.ic_warning,
                buttonConfirmText = R.string.dialog_discard_session_confirm_button,
                buttonDismissText = R.string.dialog_discard_session_dissmiss_button,
                onConfirm = {
                    sessionAction(SessionAction.EndSession(sessionState.session.uid))
                    sharedAction(
                        SharedAction.Navigate(
                            if (confirmationDestination == MainDestination.Game) {
                                MainDestination.Game(
                                    quizzesUids = quizState.quizzes.take(
                                        Constants.DEFAULT_QUIZ_SESSION_AMOUNT
                                    ).map { it.uid }
                                )
                            } else {
                                MainDestination.Browse
                            },
                            navController
                        )
                    )

                    confirmationTrigger = false
                },
                onDismiss = { confirmationTrigger = false }
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ButtonPrimary(
                    onClick = {
                        if (sessionState.session.uid.isEmpty()) {
                            sharedAction(
                                SharedAction.Navigate(
                                    MainDestination.Game(
                                        quizzesUids = quizState.quizzes.take(
                                            Constants.DEFAULT_QUIZ_SESSION_AMOUNT
                                        ).map { it.uid }
                                    ),
                                    navController
                                )
                            )
                            return@ButtonPrimary
                        }

                        openConfirmationDialog
                    }
                ) {
                    TextButton(text = stringResource(R.string.home_button_start))
                }

                if (sessionState.session.uid.isNotEmpty()) {
                    ButtonPrimary(
                        onClick = {
                            sharedAction(
                                SharedAction.Navigate(
                                    MainDestination.Game(
                                        quizzesUids = quizState.quizzes.take(
                                            Constants.DEFAULT_QUIZ_SESSION_AMOUNT
                                        ).map { it.uid }
                                    ),
                                    navController
                                )
                            )
                        }
                    ) {
                        TextButton(text = stringResource(R.string.home_button_resume))
                    }
                }
            }
            ButtonSecondary(
                onClick = {
                    if (sessionState.session.uid.isEmpty()) {
                        sharedAction(
                            SharedAction.Navigate(
                                MainDestination.Browse,
                                navController
                            )
                        )

                        return@ButtonSecondary
                    }

                    openConfirmationDialog
                }
            ) {
                TextButton(
                    text = stringResource(R.string.home_button_browse),
                    textDecoration = TextDecoration.Underline
                )
                IconButton(
                    painter = painterResource(R.drawable.ic_arrow_north_east)
                )
            }
        }
    }
}