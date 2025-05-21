package com.example.quiz_game.ui.activity.onboard.destination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.other.Constants
import com.example.quiz_game.ui.activity.onboard.OnboardDestination
import com.example.quiz_game.ui.shared.component.ButtonSecondary
import com.example.quiz_game.ui.shared.component.CardSelectable
import com.example.quiz_game.ui.shared.component.IconButton
import com.example.quiz_game.ui.shared.component.LoadingInfiniteLine
import com.example.quiz_game.ui.shared.component.TextBerySmol
import com.example.quiz_game.ui.shared.component.TextSmol
import com.example.quiz_game.ui.viewmodel.OnboardAction
import com.example.quiz_game.ui.viewmodel.OnboardState
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedState

private const val TAG = "test1234 Language"

@Composable
fun Language(
    modifier: Modifier = Modifier,
    onboardAction: (OnboardAction) -> Unit = {},
    sharedAction: (SharedAction) -> Unit = {},
    onboardState: OnboardState = OnboardState(),
    sharedState: SharedState = SharedState(),
    navController: NavController = rememberNavController()
) {
    val context = LocalContext.current

    LaunchedEffect(onboardState) {
        if (!onboardState.user.language.isNullOrEmpty()) {
            sharedAction(SharedAction.Navigate(OnboardDestination.Form, navController))
        }
    }

    var selectedLanguage by rememberSaveable { mutableStateOf("") }

    if (sharedState.executing || onboardState.executing) {
        LoadingInfiniteLine(subject = arrayOf("Language"))
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(10.dp),
        ) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    ButtonSecondary(
                        enabled = selectedLanguage.isNotEmpty(),
                        onClick = {
                            onboardAction(OnboardAction.UpdateLanguage(language = selectedLanguage))
                            sharedAction(SharedAction.PrepareTranslator)
                            sharedAction(SharedAction.Restart(context))
                        }
                    ) {
                        Text(text = stringResource(R.string.onboard_language_next_button))
                        IconButton(
                            painter = painterResource(R.drawable.ic_arrow_forward),
                        )
                    }
                }
            }
            items(items = Constants.SUPPORTED_LANGUAGES, key = { it.hashCode() }) {
                val (language, country, countryCode) = it
                CardSelectable(
                    selected = selectedLanguage == language,
                    onSelect = {
                        selectedLanguage = language
                    },
                    content = { modifier ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = modifier
                        ) {
                            IconButton(
                                model = "https://flagsapi.com/$countryCode/shiny/64.png"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                TextSmol(
                                    text = country.split(" ").first(),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextBerySmol(text = country.split(" ").last())
                            }
                            if (selectedLanguage == language) {
                                IconButton(
                                    painter = painterResource(R.drawable.ic_check),
                                )
                            }
                        }
                    }
                )
            }
        }
    }

}

@Preview
@Composable
private fun LanguagePreview() {
    com.example.quiz_game.ui.shared.component.Preview {
        Language()
    }
}