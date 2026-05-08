package com.example.quiz_game.ui.activity.onboard.destination

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.example.quiz_game.ui.shared.component.CardSelectable
import com.example.quiz_game.ui.shared.component.DialogYesOrNo
import com.example.quiz_game.ui.shared.component.IconButton
import com.example.quiz_game.ui.shared.component.LoadingProgressiveLine
import com.example.quiz_game.ui.shared.component.Preview
import com.example.quiz_game.ui.shared.component.ScreenHeader
import com.example.quiz_game.ui.theme.Indigo100
import com.example.quiz_game.ui.theme.Indigo600
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
    // The language the user chose but hasn't finished downloading yet — kept
    // alive in the singleton so it survives navigation away from this screen.
    // We seed our local `selectedLanguage` from it on (re)entry so the
    // OfflineStateSection's Retry button is always available, even when the
    // user lands on this screen after a background retry has failed and they
    // never re-tapped a language card.
    val pendingLang by TranslatorManager.pendingLanguage.collectAsStateWithLifecycle()

    LaunchedEffect(pendingLang) {
        if (selectedLanguage.isEmpty() && !pendingLang.isNullOrEmpty()) {
            selectedLanguage = pendingLang!!
        }
    }

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
        sharedAction(
            SharedAction.PrepareTranslator(
                language = selectedLanguage,
                context = context
            )
        )
    }

    when {
        // Active translator work in progress. Gate on the TranslatorStatus
        // itself rather than the generic `executing` flag — the latter also
        // flips true for unrelated state loads (e.g. GetUser on activity
        // restart), which leaked a stray loading bar before Form appeared.
        translatorStatus == TranslatorStatus.Saving ||
                translatorStatus == TranslatorStatus.Downloading ||
                translatorStatus == TranslatorStatus.SlowDownload ||
                translatorStatus == TranslatorStatus.Restarting -> {
            val statusMessage =
                when (translatorStatus) {
                    TranslatorStatus.Saving ->
                        stringResource(
                            R.string.onboard_form_loading_subject
                        )

                    TranslatorStatus.Downloading ->
                        stringResource(
                            R.string.onboard_language_downloading
                        )

                    TranslatorStatus.SlowDownload ->
                        stringResource(
                            R.string.onboard_language_slow_download
                        )

                    TranslatorStatus.Restarting ->
                        stringResource(R.string.onboard_language_restarting)

                    else ->
                        stringResource(
                            R.string.onboard_form_loading_subject
                        )
                }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingProgressiveLine(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    status = translatorStatus,
                    statusMessage = statusMessage
                )
            }
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
                        text =
                            R.string
                                .onboard_language_no_wifi_warning_message,
                        buttonConfirmText =
                            R.string
                                .onboard_language_no_wifi_warning_positive_button,
                        buttonDismissText =
                            R.string
                                .onboard_language_no_wifi_warning_negative_button,
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
                    Column(
                        modifier =
                            Modifier
                                    .fillMaxSize()
                                    .background(
                                            MaterialTheme.colorScheme
                                                    .background
                                    )
                    ) {
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
                                    TranslatorManager
                                        .skipForNow(
                                            selectedLanguage
                                        )
                                    if (fromOnboarding) {
                                        navController
                                            .navigate(
                                                OnboardDestination
                                                    .Form
                                            )
                                    } else {
                                        navController
                                            .popBackStack()
                                    }
                                }
                            )
                        }

                        // ── Scrollable language list ──
                        LazyColumn(
                            modifier =
                                Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                            contentPadding =
                                PaddingValues(bottom = 16.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            // ── Edge-to-edge illustration ──
                            item {
                                Image(
                                    painter =
                                        painterResource(
                                            R.drawable
                                                .illustration_language
                                        ),
                                    contentDescription = null,
                                    contentScale =
                                        ContentScale.Fit,
                                    modifier =
                                        Modifier
                                                .fillMaxWidth()
                                                .height(
                                                        220.dp
                                                )
                                )
                            }

                            // ── Screen header ──
                            item {
                                ScreenHeader(
                                    title =
                                        stringResource(
                                            R.string
                                                .onboard_language_welcome_title
                                        ),
                                    subtitle =
                                        stringResource(
                                            R.string
                                                .onboard_language_subtitle
                                        ),
                                    showTitle = fromOnboarding,
                                    modifier =
                                        Modifier.padding(
                                            start =
                                                20.dp,
                                            end = 20.dp,
                                            top = 16.dp,
                                            bottom =
                                                12.dp
                                        )
                                )
                            }

                            items(
                                items =
                                    Constants
                                        .SUPPORTED_LANGUAGES,
                                key = { it.hashCode() }
                            ) {
                                val (
                                    language,
                                    country,
                                    countryCode) =
                                    it
                                CardSelectable(
                                    modifier =
                                        Modifier.padding(
                                            horizontal =
                                                20.dp
                                        ),
                                    selected =
                                        selectedLanguage ==
                                                language,
                                    onSelect = {
                                        selectedLanguage =
                                            language
                                    },
                                    showCheckmark = true,
                                    content = { contentModifier
                                        ->
                                        Row(
                                            verticalAlignment =
                                                Alignment
                                                    .CenterVertically,
                                            modifier =
                                                contentModifier
                                        ) {
                                            Card(
                                                shape =
                                                    RoundedCornerShape(
                                                        8.dp
                                                    ),
                                                modifier =
                                                    Modifier.size(
                                                        height =
                                                            28.dp,
                                                        width =
                                                            40.dp
                                                    )
                                            ) {
                                                IconButton(
                                                    modifier =
                                                        Modifier.fillMaxSize(),
                                                    model =
                                                        Utils.countryFlag(
                                                            countryCode =
                                                                countryCode
                                                        ),
                                                )
                                            }

                                            Spacer(
                                                modifier =
                                                    Modifier.width(
                                                        14.dp
                                                    )
                                            )

                                            Text(
                                                text =
                                                    country.split(
                                                        " "
                                                    )
                                                        .last(),
                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .titleMedium,
                                                color =
                                                    if (selectedLanguage ==
                                                        language
                                                    ) {
                                                        if (isSystemInDarkTheme()
                                                        )
                                                            Indigo100
                                                        else
                                                            Indigo600
                                                    } else {
                                                        MaterialTheme
                                                            .colorScheme
                                                            .onSurface
                                                    }
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        // ── Sticky bottom CTA ──
                        AnimatedVisibility(
                            visible = selectedLanguage.isNotEmpty(),
                            enter = fadeIn() + slideInVertically { it },
                            exit = fadeOut() + slideOutVertically { it }
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                            .fillMaxWidth()
                                            .background(
                                                    MaterialTheme
                                                            .colorScheme
                                                            .background
                                            )
                                            .padding(
                                                    horizontal =
                                                            20.dp,
                                                    vertical =
                                                            16.dp
                                            )
                            ) {
                                ButtonPrimary(
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                    onClick = {
                                        if (!Utils.hasInternet()
                                        ) {
                                            // Show
                                            // inline
                                            // offline
                                            // notice
                                            // instead
                                            // of dialog
                                            showOfflineNotice =
                                                true
                                        } else if (!Utils.hasWifiOn()
                                        ) {
                                            showHasNoWifiOnWarning =
                                                true
                                        } else {
                                            sharedAction(
                                                SharedAction
                                                    .PrepareTranslator(
                                                        language =
                                                            selectedLanguage,
                                                        context =
                                                            context
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
                                            ),
                                        style =
                                            MaterialTheme
                                                .typography
                                                .titleMedium,
                                        fontWeight =
                                            FontWeight
                                                .SemiBold
                                    )
                                }
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
        modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        text =
                            stringResource(
                                R.string
                                    .onboard_language_back_online_title
                            ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // ❌ Offline state
                    Icon(
                        painter =
                            painterResource(R.drawable.ic_no_internet),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text =
                            stringResource(
                                R.string
                                    .onboard_language_offline_title
                            ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text =
                            stringResource(
                                R.string
                                    .onboard_language_offline_body
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    ButtonPrimary(
                        onClick = onContinueWithout,
                        color = MaterialTheme.colorScheme.tertiary
                    ) {
                        Text(
                            text =
                                stringResource(
                                    R.string
                                        .onboard_language_continue_without
                                ),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(
                            painter =
                                painterResource(
                                    R.drawable.ic_arrow_forward
                                ),
                            tint = MaterialTheme.colorScheme.surface
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
        modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_no_internet),
            contentDescription = null,
            modifier = Modifier.size(52.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(32.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            ButtonPrimary(
                onClick = onContinueWithout,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text =
                        stringResource(
                            R.string.onboard_language_continue_without
                        ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal
                )
                IconButton(
                    modifier = Modifier.scale(1.2f),
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    tint = MaterialTheme.colorScheme.surface
                )
            }

            Spacer(Modifier.height(8.dp))

            if (onRetry != null) {
                ButtonPrimary(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text =
                            stringResource(
                                R.string.onboard_form_error_button
                            ),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal
                    )
                    IconButton(
                        modifier = Modifier.scale(1.2f),
                        painter = painterResource(R.drawable.ic_retry),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineStateSectionPreview() {
    Preview {
        OfflineStateSection(
            title = "Could not set up your language.",
            subtitle =
                "It could be Google downloading servers or it could be a failing internet connection either way we apologize for the inconvenience",
            onContinueWithout = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OfflineNoticeBannerPreview() {
    Preview { OfflineNoticeBanner(isBackOnline = false, onContinueWithout = {}) }
}

@Preview(showBackground = true)
@Composable
private fun LanguagePreview() {
    Preview {
        Box(
            Modifier
                    .fillMaxSize()
                    // .background(color = Color("#a4b0be".toColorInt()))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) { Language() }
    }
}
