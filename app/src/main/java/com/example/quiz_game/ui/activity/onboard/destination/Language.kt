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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.Utils
import com.example.quiz_game.ui.shared.component.ButtonSecondary
import com.example.quiz_game.ui.shared.component.CardSelectable
import com.example.quiz_game.ui.shared.component.DialogYesOrNo
import com.example.quiz_game.ui.shared.component.IconButton
import com.example.quiz_game.ui.shared.component.LoadingInfiniteLine
import com.example.quiz_game.ui.shared.component.TextBerySmol
import com.example.quiz_game.ui.shared.component.TextSmol
import com.example.quiz_game.ui.viewmodel.OnboardAction
import com.example.quiz_game.ui.viewmodel.OnboardState
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedState
import com.example.quiz_game.ui.viewmodel.TranslatorStatus
import kotlinx.coroutines.delay

private const val TAG = "test1234 Language"

@Composable
fun Language(
    modifier: Modifier = Modifier,
    onboardAction: (OnboardAction) -> Unit = {},
    sharedAction: (SharedAction) -> Unit = {},
    onboardState: OnboardState = OnboardState(),
    sharedState: SharedState = SharedState(),
    navController: NavController = rememberNavController(),
    fromOnboarding: Boolean = true
) {
    val context = LocalContext.current
    var showHasNoWifiOnWarning by rememberSaveable { mutableStateOf(false) }
    var showHasNoInternetConnectionWarning by rememberSaveable { mutableStateOf(false) }
    var selectedLanguage by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(sharedState) {
        if (sharedState.translator == null) {
            if (sharedState.executing) {
                println(
                    "test1234 translator is still preparing ⏳ (status: ${sharedState.translatorStatus})"
                )
                return@LaunchedEffect
            }

            println("test1234 translator is not ready ❌ (status: ${sharedState.translatorStatus})")
            return@LaunchedEffect
        }

        println("test1234 translator is ready! ✅\nref : ${sharedState.translator}")
    }

    // Auto-retry when connectivity returns after a failed download
    LaunchedEffect(sharedState.translatorStatus) {
        if (sharedState.translatorStatus != TranslatorStatus.Failed) return@LaunchedEffect
        if (selectedLanguage.isEmpty()) return@LaunchedEffect
        // Poll until internet is back
        while (!Utils.hasInternet()) {
            delay(3000)
        }
        // Auto-retry
        sharedAction(SharedAction.PrepareTranslator(language = selectedLanguage, context = context))
    }

    when {
        sharedState.executing || onboardState.executing -> {
            val statusMessage =
                when (sharedState.translatorStatus) {
                    TranslatorStatus.Saving ->
                        stringResource(R.string.onboard_form_loading_subject)

                    TranslatorStatus.Downloading ->
                        stringResource(R.string.onboard_language_downloading)

                    TranslatorStatus.SlowDownload ->
                        stringResource(R.string.onboard_language_slow_download)

                    TranslatorStatus.Restarting ->
                        stringResource(R.string.onboard_language_restarting)

                    else -> stringResource(R.string.onboard_form_loading_subject)
                }
            LoadingInfiniteLine(subject = arrayOf(statusMessage))
        }

        sharedState.translatorStatus == TranslatorStatus.Failed -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboard_language_download_failed),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(R.string.onboard_language_auto_retry),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Only navigate to Form if we are NOT in the middle of updating language
        // and language is already set (e.g. user re-opened app)
        // AND we are in the onboarding flow (not settings)
        !onboardState.user.language.isNullOrEmpty() && fromOnboarding -> {
            // sharedAction(SharedAction.Navigate(OnboardDestination.Form, navController))
        }

        else -> {
            when {
                showHasNoWifiOnWarning -> {
                    DialogYesOrNo(
                        title = R.string.generic_warning_message,
                        text = R.string.onboard_language_no_wifi_warning_message,
                        buttonConfirmText =
                            R.string.onboard_language_no_wifi_warning_positive_button,
                        buttonDismissText =
                            R.string.onboard_language_no_wifi_warning_negative_button,
                        icon = R.drawable.ic_no_wifi,
                        onConfirm = {
                            sharedAction(
                                SharedAction.PrepareTranslator(
                                    language = selectedLanguage,
                                    context = context
                                )
                            )
                            // sharedAction(SharedAction.PrepareTranslator) // Move to Effect
                            // sharedAction(SharedAction.Restart(context))  // Move to Effect

                            showHasNoWifiOnWarning = false
                        },
                        onDismiss = { showHasNoWifiOnWarning = false }
                    )
                }

                showHasNoInternetConnectionWarning -> {
                    DialogYesOrNo(
                        title = R.string.generic_warning_message,
                        text = R.string.onboard_language_no_internet_warning_message,
                        buttonConfirmText =
                            R.string.onboard_language_no_internet_warning_positive_button,
                        buttonDismissText =
                            R.string.onboard_language_no_internet_warning_negative_button,
                        icon = R.drawable.ic_no_internet,
                        onConfirm = {
                            sharedAction(
                                SharedAction.PrepareTranslator(
                                    language = selectedLanguage,
                                    context = context
                                )
                            )

                            showHasNoInternetConnectionWarning = false
                        },
                        onDismiss = { showHasNoInternetConnectionWarning = false }
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(10.dp),
                    ) {
                        item {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                ButtonSecondary(
                                    enabled = selectedLanguage.isNotEmpty(),
                                    onClick = {
                                        if (!Utils.hasInternet()) {
                                            showHasNoInternetConnectionWarning = true
                                        } else if (!Utils.hasWifiOn()) {
                                            showHasNoWifiOnWarning = true
                                        } else {
                                            sharedAction(
                                                SharedAction.PrepareTranslator(
                                                    language = selectedLanguage,
                                                    context = context
                                                )
                                            )
                                            // sharedAction(SharedAction.PrepareTranslator) //
                                            // Move to Effect
                                            // sharedAction(SharedAction.Restart(context))  //
                                            // Move to Effect
                                        }
                                    }
                                ) {
                                    Text(
                                        text =
                                            stringResource(
                                                R.string.onboard_language_next_button
                                            )
                                    )
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
                                onSelect = { selectedLanguage = language },
                                content = { modifier ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = modifier
                                    ) {
                                        IconButton(
                                            model =
                                                Utils.countryFlag(
                                                    countryCode = countryCode
                                                )
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
                                                painter =
                                                    painterResource(R.drawable.ic_pin),
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
