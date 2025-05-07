package com.example.quiz_game.ui.activity.main.destination

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.App
import com.example.quiz_game.R
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.Utils
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.ButtonPrimary
import com.example.quiz_game.ui.shared.ButtonSecondary
import com.example.quiz_game.ui.shared.IconButton
import com.example.quiz_game.ui.shared.LoadingInfiniteLine
import com.example.quiz_game.ui.shared.TextButton
import com.example.quiz_game.ui.viewmodel.CategoryState
import com.example.quiz_game.ui.viewmodel.QuizState
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
    navController: NavController = rememberNavController()
) {
    var translated by remember { mutableStateOf("Undefined") }
    var selectedCountryCode by remember { mutableStateOf("Undefined") }

    LaunchedEffect(sharedState) {
        translated =
            sharedState.translator?.translate(Locale.forLanguageTag(App.userPrefs.getString("selectedLanguage", null)).displayLanguage)?.await()
                ?: "English"

        Constants.SUPPORTED_LANGUAGES.map {
            it.first
        }.indexOf(App.userPrefs.getString("selectedLanguage", null)).apply {
            selectedCountryCode = if (this == -1) {
                "us"
            } else {
                Constants.SUPPORTED_LANGUAGES[
                    Constants.SUPPORTED_LANGUAGES.map {
                        it.first
                    }.indexOf(App.userPrefs.getString("selectedLanguage", null))
                ].third
            }
        }
    }

    if (sharedState.executing || quizState.executing || categoryState.executing) {
        LoadingInfiniteLine(subject = stringArrayResource(R.array.loading_subjects))
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ButtonPrimary(
                onClick = {
                    sharedAction(
                        SharedAction.Navigate(
                            MainDestination.Game(),
                            navController
                        )
                    )
                }
            ) {
                TextButton(text = stringResource(R.string.home_button_primary))
            }
            ButtonSecondary(
                onClick = {
                    sharedAction(
                        SharedAction.Navigate(
                            MainDestination.Browse,
                            navController
                        )
                    )
                }
            ) {
                TextButton(
                    text = stringResource(R.string.home_button_secondary),
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