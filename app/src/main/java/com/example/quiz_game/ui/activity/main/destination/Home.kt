package com.example.quiz_game.ui.activity.main.destination

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.contentColorFor
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.data.Repository
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.TranslatorManager
import com.example.quiz_game.other.Utils
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.ButtonSecondary
import com.example.quiz_game.ui.shared.component.CardClickable
import com.example.quiz_game.ui.shared.component.DialogYesOrNo
import com.example.quiz_game.ui.shared.component.IconButton
import com.example.quiz_game.ui.shared.component.LayoutSectionHeadline
import com.example.quiz_game.ui.shared.component.LoadingFullScreenLowOpacityWithInfiniteSpinner
import com.example.quiz_game.ui.shared.component.LoadingInfiniteLine
import com.example.quiz_game.ui.shared.component.TextBerySmol
import com.example.quiz_game.ui.shared.component.TextButton
import com.example.quiz_game.ui.shared.component.TextRegular
import com.example.quiz_game.ui.viewmodel.CategoryAction
import com.example.quiz_game.ui.viewmodel.CategoryState
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.QuoteAction
import com.example.quiz_game.ui.viewmodel.QuoteState
import com.example.quiz_game.ui.viewmodel.SessionAction
import com.example.quiz_game.ui.viewmodel.SessionState
import com.example.quiz_game.ui.viewmodel.SharedAction

@Composable
fun Home(
        modifier: Modifier = Modifier,
        quizState: QuizState,
        quizAction: (QuizAction) -> Unit = {},
        categoryState: CategoryState = CategoryState(),
        quoteState: QuoteState = QuoteState(),
        quoteAction: (QuoteAction) -> Unit = {},
        sharedAction: (SharedAction) -> Unit = {},
        sessionState: SessionState = SessionState(),
        sessionAction: (SessionAction) -> Unit = {},
        categoryAction: (CategoryAction) -> Unit = {},
        navController: NavController = rememberNavController(),
        onError: (String) -> Unit = {}
) {
    var translated by remember { mutableStateOf("Undefined") }
    var selectedCountryCode by remember { mutableStateOf("Undefined") }
    var confirmationDialog by remember { mutableStateOf(false) }
    var whereTo by remember { mutableStateOf(WhereTo.Start) }
    var startGameTrigger by remember { mutableStateOf(false) }
    val currentSession = sessionState.session

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

    // React to quiz state: when ready + not executing + trigger is set → create session
    LaunchedEffect(startGameTrigger, quizState.ready, quizState.executing) {
        if (!startGameTrigger) return@LaunchedEffect
        if (quizState.executing) return@LaunchedEffect

        if (!quizState.ready && quizState.errors.isNotEmpty()) {
            onError("Failed to load quizzes, reason : ${quizState.errors.joinToString()}.")
            quizState.errors.fastForEach { e -> Log.e("Home", "Home: ${e.message}") }
            startGameTrigger = false

            return@LaunchedEffect
        }

        val sessionQuizzes =
                quizState
                        .quizzes
                        .fastFilter { !it.expired }
                        .take(Constants.DEFAULT_QUIZ_SESSION_AMOUNT)

        if (sessionQuizzes.isEmpty()) {
            onError("We couldn't get quizzes. Please check your internet connection.")
            startGameTrigger = false
            return@LaunchedEffect
        }

        // Initiate a new session
        sessionAction(
                SessionAction.InitiateSession(
                        quizzesUids = sessionQuizzes.fastMap { it.uid },
                        maxScore = sessionQuizzes.sumOf { it.mark ?: 0 }
                )
        )

        // Navigate
        sharedAction(SharedAction.Navigate(MainDestination.Game(), navController))

        startGameTrigger = false
    }

    if (quizState.executing || categoryState.executing || startGameTrigger) {
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
                                        SharedAction.Navigate(MainDestination.Game(), navController)
                                )
                            }
                            WhereTo.Browse -> {
                                sharedAction(
                                        SharedAction.Navigate(MainDestination.Browse, navController)
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
                            if (currentSession == null || currentSession.expiredAt != null) {
                                startGameTrigger = true
                            } else {
                                whereTo = WhereTo.Start
                                confirmationDialog = true
                            }
                        }
                ) { TextButton(text = stringResource(R.string.home_button_start)) }

                if (currentSession != null && currentSession.expiredAt == null) {
                    ButtonPrimary(
                            onClick = {
                                sharedAction(
                                        SharedAction.Navigate(
                                                MainDestination.Game(
                                                        quizzesUids = currentSession.quizzesUids
                                                                        ?: emptyList()
                                                ),
                                                navController
                                        )
                                )
                            }
                    ) { TextButton(text = stringResource(R.string.home_button_resume)) }
                }
            }
            ButtonSecondary(
                    onClick = {
                        if (currentSession == null || currentSession.expiredAt != null) {
                            sharedAction(
                                    SharedAction.Navigate(MainDestination.Browse, navController)
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
                IconButton(painter = painterResource(R.drawable.ic_arrow_north_east))
            }

            ButtonSecondary(
                    onClick = {
                        sharedAction(SharedAction.Navigate(MainDestination.Language, navController))
                    }
            ) {
                IconButton(model = Utils.countryFlag(countryCode = selectedCountryCode))
                TextButton(
                        text = translated,
                )
            }
        }
    }
}

@Composable
private fun GreetingSection() {
    val translator by TranslatorManager.translator.collectAsStateWithLifecycle()
    var greetingMessage by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(translator) {
        greetingMessage = Utils.greetingBasedOnTimezone(translator = translator)
    }
    LayoutSectionHeadline(
            title = "$greetingMessage ${Repository.getUser()?.username}",
            leadingIcon = R.drawable.ic_wave
    )
}

@Composable
private fun QuoteCard(quoteState: QuoteState, onAuthorClick: (String?) -> Unit) {
    val padding = 10.dp

    CardClickable(
            onClick = { onAuthorClick(quoteState.quote?.author) },
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
    ) {
        if (quoteState.executing) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingFullScreenLowOpacityWithInfiniteSpinner()
            }
            return@CardClickable
        }

        Column(
                Modifier.fillMaxWidth().padding(vertical = padding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier.padding(horizontal = padding)
            ) {
                IconButton(
                        painter = painterResource(R.drawable.ic_feather),
                        tint = MaterialTheme.colorScheme.primary
                )

                TextBerySmol(
                        text = stringResource(R.string.home_quote_label),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = padding)
                )
            }

            TextRegular(
                    text =
                            if (quoteState.quote?.quote != null) "\"${quoteState.quote.quote}\""
                            else stringResource(R.string.home_quote_not_found),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = padding),
                    textAlign = TextAlign.Start
            )

            if (!quoteState.quote?.author.isNullOrBlank()) {
                SuggestionChip(
                        onClick = { onAuthorClick(quoteState.quote?.author) },
                        modifier = Modifier.padding(horizontal = padding),
                        label = {
                            TextBerySmol(
                                    text = quoteState.quote?.author ?: "",
                                    color = contentColorFor(MaterialTheme.colorScheme.surface)
                            )
                        },
                        icon = { IconButton(imageVector = Icons.Default.AccountCircle) },
                        border = null,
                        colors =
                                SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor =
                                                contentColorFor(MaterialTheme.colorScheme.surface)
                                ),
                        shape = RoundedCornerShape(100)
                )
            }
        }
    }
}

private enum class WhereTo {
    Start,
    Browse
}
