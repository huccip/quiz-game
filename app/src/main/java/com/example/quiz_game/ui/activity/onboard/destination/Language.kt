package com.example.quiz_game.ui.activity.onboard.destination

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.TranslatorManager
import com.example.quiz_game.other.TranslatorStatus
import com.example.quiz_game.other.Utils
import com.example.quiz_game.ui.activity.onboard.OnboardDestination
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.ButtonSecondary
import com.example.quiz_game.ui.shared.component.CardSelectable
import com.example.quiz_game.ui.shared.component.DialogYesOrNo
import com.example.quiz_game.ui.shared.component.IconButton
import com.example.quiz_game.ui.shared.component.LoadingProgressiveLine
import com.example.quiz_game.ui.shared.component.TextBerySmol
import com.example.quiz_game.ui.shared.component.TextButton
import com.example.quiz_game.ui.shared.component.TextSmol
import com.example.quiz_game.ui.viewmodel.OnboardAction
import com.example.quiz_game.ui.viewmodel.OnboardState
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedState
import kotlinx.coroutines.delay

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
    var showOfflineNotice by rememberSaveable { mutableStateOf(false) }
    var isBackOnline by rememberSaveable { mutableStateOf(false) }
    var selectedLanguage by rememberSaveable { mutableStateOf("") }

    // Observe TranslatorManager status directly
    val translatorStatus by TranslatorManager.status.collectAsStateWithLifecycle()
    val translator by TranslatorManager.translator.collectAsStateWithLifecycle()

    LaunchedEffect(translator) {
        if (translator == null) {
            return@LaunchedEffect
        }
    }

    // Auto-detect connectivity while showing offline notice
    // When internet returns, show "back online" state then auto-start download
    LaunchedEffect(showOfflineNotice) {
        if (!showOfflineNotice) return@LaunchedEffect
        if (selectedLanguage.isEmpty()) return@LaunchedEffect
        // Poll until internet is back
        while (!Utils.hasInternet()) {
            delay(2000)
        }
        // Internet is back! Show the "back online" state
        isBackOnline = true
        // Wait 3 seconds so the user sees the message
        delay(3000)
        // Dismiss banner and start the download
        showOfflineNotice = false
        isBackOnline = false
        sharedAction(SharedAction.PrepareTranslator(language = selectedLanguage, context = context))
    }

    when {
        // Active download in progress
        sharedState.executing || onboardState.executing -> {
            val statusMessage =
                    when (translatorStatus) {
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
            LoadingProgressiveLine(status = translatorStatus, statusMessage = statusMessage)
        }

        // Internet lost during download (timeout or disconnection)
        translatorStatus == TranslatorStatus.InternetLost -> {
            OfflineStateSection(
                    title = stringResource(R.string.onboard_language_internet_lost),
                    subtitle = stringResource(R.string.onboard_language_auto_retry),
                    onContinueWithout = {
                        TranslatorManager.skipForNow(selectedLanguage)
                        if (fromOnboarding) {
                            navController.navigate(OnboardDestination.Form)
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onRetry =
                            if (selectedLanguage.isNotEmpty()) {
                                {
                                    sharedAction(
                                            SharedAction.PrepareTranslator(
                                                    language = selectedLanguage,
                                                    context = context
                                            )
                                    )
                                }
                            } else null
            )
        }

        // Generic download failure (non-network)
        translatorStatus == TranslatorStatus.Failed -> {
            OfflineStateSection(
                    title = stringResource(R.string.onboard_language_download_failed),
                    subtitle = null,
                    onContinueWithout = {
                        TranslatorManager.skipForNow(selectedLanguage)
                        if (fromOnboarding) {
                            navController.navigate(OnboardDestination.Form)
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onRetry =
                            if (selectedLanguage.isNotEmpty()) {
                                {
                                    sharedAction(
                                            SharedAction.PrepareTranslator(
                                                    language = selectedLanguage,
                                                    context = context
                                            )
                                    )
                                }
                            } else null
            )
        }

        // Language already set — skip past language screen (onboarding only)
        !onboardState.user.language.isNullOrEmpty() && fromOnboarding -> {
            // Already picked, auto-navigate is handled elsewhere
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
                                showHasNoWifiOnWarning = false
                            },
                            onDismiss = { showHasNoWifiOnWarning = false }
                    )
                }
                else -> {
                    Column {
                        // Inline offline notice — replaces the old dialog
                        AnimatedVisibility(
                                visible = showOfflineNotice,
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                        ) {
                            OfflineNoticeBanner(
                                    isBackOnline = isBackOnline,
                                    onContinueWithout = {
                                        showOfflineNotice = false
                                        isBackOnline = false
                                        TranslatorManager.skipForNow(selectedLanguage)
                                        if (fromOnboarding) {
                                            navController.navigate(OnboardDestination.Form)
                                        } else {
                                            navController.popBackStack()
                                        }
                                    }
                            )
                        }

                        LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(10.dp),
                        ) {
                            item {
                                Box(
                                        Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterEnd
                                ) {
                                    ButtonSecondary(
                                            enabled = selectedLanguage.isNotEmpty(),
                                            onClick = {
                                                if (!Utils.hasInternet()) {
                                                    // Show inline offline notice instead of dialog
                                                    showOfflineNotice = true
                                                } else if (!Utils.hasWifiOn()) {
                                                    showHasNoWifiOnWarning = true
                                                } else {
                                                    sharedAction(
                                                            SharedAction.PrepareTranslator(
                                                                    language = selectedLanguage,
                                                                    context = context
                                                            )
                                                    )
                                                }
                                            }
                                    ) {
                                        Text(
                                                text =
                                                        stringResource(
                                                                R.string
                                                                        .onboard_language_next_button
                                                        )
                                        )
                                        IconButton(
                                                painter =
                                                        painterResource(
                                                                R.drawable.ic_arrow_forward
                                                        ),
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
                                                            color =
                                                                    MaterialTheme.colorScheme
                                                                            .onSurface
                                                    )
                                                    TextBerySmol(text = country.split(" ").last())
                                                }
                                                if (selectedLanguage == language) {
                                                    IconButton(
                                                            painter =
                                                                    painterResource(
                                                                            R.drawable.ic_pin
                                                                    ),
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
}

/**
 * Inline banner shown above the language list when user has no internet. Has two states:
 * - Offline: shows error icon, "You're offline" message, and "Continue without" button
 * - Back online: shows check icon, "You're back online!" message (auto-dismisses)
 */
@Composable
private fun OfflineNoticeBanner(isBackOnline: Boolean, onContinueWithout: () -> Unit) {
    Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedContent(
                targetState = isBackOnline,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "banner_state"
        ) { online ->
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
            ) {
                if (online) {
                    // ✅ Back online state
                    Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                            text = stringResource(R.string.onboard_language_back_online_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                    )
                    Text(
                            text = stringResource(R.string.onboard_language_back_online_body),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // ❌ Offline state
                    Icon(
                            painter = painterResource(R.drawable.ic_no_internet),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                            text = stringResource(R.string.onboard_language_offline_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                    )
                    Text(
                            text = stringResource(R.string.onboard_language_offline_body),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    ButtonPrimary(onClick = onContinueWithout) {
                        TextButton(
                                text = stringResource(R.string.onboard_language_continue_without)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full-screen section shown when download fails (InternetLost or generic failure). Shows error
 * message, optional retry, and always allows continuing without translation.
 */
@Composable
private fun OfflineStateSection(
        title: String,
        subtitle: String?,
        onContinueWithout: () -> Unit,
        onRetry: (() -> Unit)? = null
) {
    Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Icon(
                painter = painterResource(R.drawable.ic_no_internet),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(24.dp))
        if (onRetry != null) {
            ButtonSecondary(onClick = onRetry) {
                Text(text = stringResource(R.string.onboard_form_error_button))
            }
            Spacer(Modifier.height(8.dp))
        }
        ButtonPrimary(onClick = onContinueWithout) {
            TextButton(text = stringResource(R.string.onboard_language_continue_without))
        }
    }
}
