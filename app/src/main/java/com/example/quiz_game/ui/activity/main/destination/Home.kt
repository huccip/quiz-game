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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastSumBy
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.App
import com.example.quiz_game.R
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.session.Session
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.Utils
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.ButtonSecondary
import com.example.quiz_game.ui.shared.component.DialogYesOrNo
import com.example.quiz_game.ui.shared.component.IconButton
import com.example.quiz_game.ui.shared.component.LoadingInfiniteLine
import com.example.quiz_game.ui.shared.component.TextButton
import com.example.quiz_game.ui.viewmodel.CategoryAction
import com.example.quiz_game.ui.viewmodel.CategoryState
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.QuoteAction
import com.example.quiz_game.ui.viewmodel.QuoteState
import com.example.quiz_game.ui.viewmodel.SessionAction
import com.example.quiz_game.ui.viewmodel.SessionState
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.Locale

private const val TAG = "test1234 Home"

@Composable
fun Home(
    modifier: Modifier = Modifier,
    sharedState: StateFlow<SharedState>? = null,
    quizState: QuizState = QuizState(),
    quizAction: (QuizAction) -> Unit = {},
    categoryState: CategoryState = CategoryState(),
    quoteState: QuoteState = QuoteState(),
    quoteAction: (QuoteAction) -> Unit = {},
    sharedAction: (SharedAction) -> Unit = {},
    sessionState: StateFlow<SessionState>? = null,
    sessionAction: (SessionAction) -> Unit = {},
    categoryAction: (CategoryAction) -> Unit = {},
    navController: NavController = rememberNavController()
) {
    var translated by remember { mutableStateOf("Undefined") }
    var selectedCountryCode by remember { mutableStateOf("Undefined") }
    var confirmationDialog by remember { mutableStateOf(false) }
    var currentSession: Session? by remember { mutableStateOf(null) }
    var currentSharedState: SharedState? by remember { mutableStateOf(null) }
    var whereTo by remember { mutableStateOf(WhereTo.Start) }

    LaunchedEffect(Unit) {
        if (sessionState == null) return@LaunchedEffect

        sessionState.first { sessionState ->
            !sessionState.executing && sessionState.session.uid.isNotEmpty() && sessionState.session.expiredAt == null
        }.let { currentSession = it.session }
    }

    LaunchedEffect(Unit) {
        val userLanguage = Repository.getUser()?.language
        val supportedLanguage = Constants.SUPPORTED_LANGUAGES.find { it.first == userLanguage }

        if (supportedLanguage != null) {
            val (_, nameAndCountry, country) = supportedLanguage
            translated = nameAndCountry.split(" ").last()
            selectedCountryCode = country
        } else {
            translated = "English"
            selectedCountryCode = "us"
        }
    }

    LaunchedEffect(sharedState) {
        if (sharedState == null) return@LaunchedEffect

        sharedState.first { sharedState ->
            !sharedState.executing && sharedState.translator != null
        }.let {
            currentSharedState = it
        }
    }

    if (currentSharedState?.executing == true || quizState.executing || categoryState.executing) {
        LoadingInfiniteLine(subject = stringArrayResource(R.array.home_loading_subjects))
    } else {
        if (confirmationDialog) {
            DialogYesOrNo(
                modifier = Modifier.fillMaxWidth(),
                title = R.string.dialog_discard_session_title,
                text = R.string.dialog_discard_session_text,
                icon = R.drawable.ic_warning,
                buttonConfirmText = R.string.dialog_discard_session_confirm_button,
                buttonDismissText = R.string.dialog_discard_session_dissmiss_button,
                onConfirm = {
                    currentSession?.let { sessionAction(SessionAction.EndSession(it.uid)) }

                    when (whereTo) {
                        WhereTo.Start -> {
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
                        WhereTo.Browse -> {
                            sharedAction(
                                SharedAction.Navigate(
                                    MainDestination.Browse,
                                    navController
                                )
                            )
                        }
                    }

                    confirmationDialog = false
                },
                onDismiss = { confirmationDialog = false }
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
                        if (currentSession == null) {
                            val sessionQuizzes = quizState.quizzes.fastFilter { !it.expired }.take(
                                Constants.DEFAULT_QUIZ_SESSION_AMOUNT
                            )

                            //Initiate a new session
                            sessionAction(
                                SessionAction.InitiateSession(
                                quizzesUids = sessionQuizzes.fastMap { it.uid },
                                maxScore = sessionQuizzes.fastSumBy { it.mark!! }
                            ))

                            // Navigate
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

                        whereTo = WhereTo.Start
                        confirmationDialog = true
                    }
                ) {
                    TextButton(text = stringResource(R.string.home_button_start))
                }

                if (currentSession != null) {
                    ButtonPrimary(
                        onClick = {
                            sharedAction(
                                SharedAction.Navigate(
                                    MainDestination.Game(
                                        quizzesUids = currentSession!!.quizzesUids
                                            ?: emptyList()
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
                    if (currentSession == null) {
                        sharedAction(
                            SharedAction.Navigate(
                                MainDestination.Browse,
                                navController
                            )
                        )

                        return@ButtonSecondary
                    }

                    whereTo = WhereTo.Browse
                    confirmationDialog = true
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

            ButtonSecondary(
                onClick = {
                    sharedAction(
                        SharedAction.Navigate(
                            MainDestination.Language,
                            navController
                        )
                    )
                }
            ) {
                IconButton(
                    model = Utils.countryFlag(
                        countryCode = selectedCountryCode
                    )
                )
                TextButton(
                    text = translated,
                )
            }
        }
    }
}

private enum class WhereTo {
    Start, Browse
}