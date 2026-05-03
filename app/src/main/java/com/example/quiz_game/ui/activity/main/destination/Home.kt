package com.example.quiz_game.ui.activity.main.destination

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.category.Category
import com.example.quiz_game.data.session.Session
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.DailyRewards
import com.example.quiz_game.other.TranslatorManager
import com.example.quiz_game.other.Utils
import com.example.quiz_game.other.withTap
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.BannerAd
import com.example.quiz_game.ui.shared.component.DialogLootBoxReveal
import com.example.quiz_game.ui.shared.component.DialogStreakReward
import com.example.quiz_game.ui.shared.component.DialogYesOrNo
import com.example.quiz_game.ui.shared.component.LoadingInfiniteLine
import com.example.quiz_game.ui.shared.effect.scaleDownOnPress
import com.example.quiz_game.ui.theme.GemCyan
import com.example.quiz_game.ui.theme.GemCyanDark
import com.example.quiz_game.ui.theme.Indigo500
import com.example.quiz_game.ui.theme.Indigo600
import com.example.quiz_game.ui.theme.Indigo700
import com.example.quiz_game.ui.theme.PlayedTeal
import com.example.quiz_game.ui.theme.PlayedTealBg
import com.example.quiz_game.ui.theme.PlayedTealBgDark
import com.example.quiz_game.ui.theme.StreakOrange
import com.example.quiz_game.ui.theme.StreakOrangeBg
import com.example.quiz_game.ui.theme.StreakOrangeBgDark
import com.example.quiz_game.ui.theme.TrophyAmber
import com.example.quiz_game.ui.theme.TrophyAmberBg
import com.example.quiz_game.ui.theme.TrophyAmberBgDark
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
import com.example.quiz_game.ui.viewmodel.ShopAction
import java.util.Calendar
import java.util.concurrent.TimeUnit


// Magic number extraction
private val HeroImageHeight = 300.dp // Controls the height of the edge-to-edge illustration
private val MaxScrollFading =
    600f // Point where the image becomes almost fully darkened during scroll
private val ForegroundSpacerHeight =
    240.dp // Pushes foreground content down so background is visible
private val ForegroundCornerRadius =
    40.dp // Rounding applied to the top edges of the scrollable content

@Composable
fun Home(
    modifier: Modifier = Modifier,
    quizState: QuizState,
    quizAction: (QuizAction) -> Unit = {},
    categoryState: CategoryState = CategoryState(),
    quoteState: QuoteState = QuoteState(),
    quoteAction: (QuoteAction) -> Unit = {},
    sharedState: SharedState = SharedState(),
    sharedAction: (SharedAction) -> Unit = {},
    sessionState: SessionState = SessionState(),
    sessionAction: (SessionAction) -> Unit = {},
    categoryAction: (CategoryAction) -> Unit = {},
    shopAction: (ShopAction) -> Unit = {},
    navController: NavController = rememberNavController(),
    onError: (String) -> Unit = {}
) {
    var translated by remember { mutableStateOf("Undefined") }
    var selectedCountryCode by remember { mutableStateOf("Undefined") }
    var startGameTrigger by remember { mutableStateOf(false) }
    val currentSession = sessionState.session

    var pendingSessionAction by remember { mutableStateOf<PendingSessionAction?>(null) }

    // Category click states
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var isCategoryLoading by rememberSaveable { mutableStateOf(false) }
    var fetchStarted by remember { mutableStateOf(false) }

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

        // Fetch categories if empty
        if (categoryState.categories.isEmpty() && !categoryState.executing) {
            categoryAction(CategoryAction.GetAll)
        }

        // Daily-login streak: this is the single canonical entry-point. The
        // ViewModel itself short-circuits if the user already logged in today,
        // so it's safe to fire on every Home composition without spamming.
        sharedAction(SharedAction.EvaluateDailyLogin)
    }

    // ── Regular Start Game Flow ──
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

        sessionAction(
            SessionAction.InitiateSession(
                quizzesUids = sessionQuizzes.fastMap { it.uid },
                maxScore = sessionQuizzes.sumOf { it.mark ?: 0 }
            )
        )
        sharedAction(SharedAction.Navigate(MainDestination.Game(), navController))
        startGameTrigger = false
    }

    // ── Category Click Flow ──
    LaunchedEffect(selectedCategory) {
        if (selectedCategory == null) return@LaunchedEffect
        isCategoryLoading = true
        fetchStarted = false
        quizAction(QuizAction.GetByCategory(categoryUid = selectedCategory!!.uid))
    }

    LaunchedEffect(quizState.executing) {
        if (quizState.executing) fetchStarted = true
    }

    LaunchedEffect(selectedCategory, quizState.executing, fetchStarted) {
        if (selectedCategory == null) return@LaunchedEffect
        if (!fetchStarted) return@LaunchedEffect
        if (quizState.executing) return@LaunchedEffect

        isCategoryLoading = false

        if (quizState.errors.isNotEmpty()) {
            onError("Failed to load quizzes: ${quizState.errors.first().message}")
            selectedCategory = null
            return@LaunchedEffect
        }

        val sessionQuizzes =
            quizState.quizzes.fastFilter { !it.expired }.take(Constants.DEFAULT_QUIZ_SESSION_AMOUNT)

        if (sessionQuizzes.isEmpty()) {
            onError("Not enough quizzes for this category. Please try another.")
            selectedCategory = null
            return@LaunchedEffect
        }

        sessionAction(
            SessionAction.InitiateSession(
                quizzesUids = sessionQuizzes.fastMap { it.uid },
                maxScore = sessionQuizzes.sumOf { it.mark ?: 0 }
            )
        )
        sharedAction(
            SharedAction.Navigate(
                MainDestination.Game(quizzesUids = sessionQuizzes.map { it.uid }),
                navController
            )
        )
        selectedCategory = null
    }

    val isDarkTheme = isSystemInDarkTheme()

    if (quizState.executing || categoryState.executing || startGameTrigger || isCategoryLoading) {
        LoadingInfiniteLine(subject = stringArrayResource(R.array.home_loading_subjects))
    } else {
        // ── Confirmation Dialog for Active Session Interruption ──
        if (pendingSessionAction != null) {
            DialogYesOrNo(
                modifier = Modifier.fillMaxWidth(),
                title = R.string.dialog_discard_session_title,
                text = R.string.dialog_discard_session_text,
                icon = R.drawable.ic_warning,
                buttonConfirmText = R.string.dialog_discard_session_confirm_button,
                buttonDismissText = R.string.dialog_discard_session_dissmiss_button,
                onConfirm = {
                    currentSession?.let { sessionAction(SessionAction.EndSession(it.uid)) }
                    when (val action = pendingSessionAction) {
                        is PendingSessionAction.StartNewGame -> startGameTrigger = true
                        is PendingSessionAction.StartCategory -> selectedCategory = action.category
                        is PendingSessionAction.BrowseAll -> sharedAction(
                            SharedAction.Navigate(
                                MainDestination.Browse,
                                navController
                            )
                        )

                        null -> {}
                    }
                    pendingSessionAction = null
                },
                onDismiss = { pendingSessionAction = null }
            )
        }

        // ── Daily-login streak popup (one-shot, surfaced by SharedViewModel) ──
        sharedState.pendingStreakReward?.let { event ->
            DialogStreakReward(
                streakDays = event.streakDays,
                coinsGranted = event.coinsGranted,
                wasReset = event.wasReset,
                onDismiss = { sharedAction(SharedAction.ConsumeStreakReward) },
            )
        }

        // ── Daily loot box reveal (modal). The reward is rolled and persisted by
        //    [SharedViewModel.claimLootBox] under the same mutex as the daily-login
        //    flow, then surfaced here via [SharedState.pendingLootBoxReward]. The
        //    dialog itself is purely presentational. ──
        sharedState.pendingLootBoxReward?.let { reward ->
            // Power-up rewards still need to land in the inventory; do this once
            // when the reward first appears so the grant is exactly-once and is
            // not re-fired on recomposition.
            LaunchedEffect(reward) {
                if (reward is DailyRewards.LootBoxReward.PowerUp) {
                    shopAction(ShopAction.GrantSpecific(reward.item))
                }
            }
            DialogLootBoxReveal(
                reward = reward,
                onDismiss = { sharedAction(SharedAction.ConsumeLootBoxReward) },
            )
        }

        val scrollState = rememberScrollState()

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── 1. Edge-to-edge Illustration (Parallax) ──
            Image(
                painter = painterResource(if (isDarkTheme) R.drawable.img_illustration_home_dark else R.drawable.img_illustration_home_light),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HeroImageHeight)
                    .graphicsLayer {
                        val scrollValue = scrollState.value.toFloat()
                        translationY = -scrollValue * 0.5f
                        val scrollRatio = (scrollValue / MaxScrollFading).coerceIn(0f, 1f)
                        alpha = 1f - (scrollRatio * 0.5f)
                    }
            )

            // Soft gradient scrim so the hero blends into the content surface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HeroImageHeight)
                    .graphicsLayer {
                        val scrollValue = scrollState.value.toFloat()
                        translationY = -scrollValue * 0.5f
                    }
                    .background(
                        Brush.verticalGradient(
                            0.55f to Color.Transparent,
                            1f to MaterialTheme.colorScheme.background
                        )
                    )
            )

            // Foreground scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(ForegroundSpacerHeight))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                topStart = ForegroundCornerRadius,
                                topEnd = ForegroundCornerRadius
                            )
                        )
                        .background(MaterialTheme.colorScheme.background)
                        .padding(bottom = 40.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Spacer(Modifier.height(28.dp))

                        // ── 2. Greeting ──
                        GreetingSection()
                        Spacer(Modifier.height(18.dp))

                        // ── 3. Stats strip ──
                        StatsStrip(sessions = sessionState.sessions)
                        Spacer(Modifier.height(18.dp))

                        // ── 3b. Daily loot box CTA ──
                        // Visible only while the 24h cooldown has rolled
                        // over. Tapping dispatches a shared action that
                        // rolls + persists the reward atomically (same mutex
                        // as the daily-login streak grant) and stages the
                        // reveal dialog via [SharedState.pendingLootBoxReward].
                        val now = System.currentTimeMillis()
                        val lastClaim = sharedState.user?.lastLootBoxClaimAt ?: 0L
                        val streakDays = sharedState.user?.loginStreakDays ?: 0
                        val canClaim = DailyRewards.lootBoxAvailable(now, lastClaim)
                        DailyLootBoxSection(
                            available = canClaim,
                            streakDays = streakDays,
                            onClaim = {
                                if (!canClaim) return@DailyLootBoxSection
                                sharedAction(SharedAction.ClaimLootBox)
                            },
                        )
                        Spacer(Modifier.height(18.dp))

                        // ── 4. Wisdom card ──
                        QuoteSection(quoteState = quoteState)
                        Spacer(Modifier.height(22.dp))

                        // ── 5. Primary CTA ──
                        val hasActiveSession =
                            currentSession != null && currentSession.expiredAt == null
                        PlayNowSection(
                            hasActiveSession = hasActiveSession,
                            activeSession = currentSession,
                            onResume = {
                                currentSession?.let {
                                    sharedAction(
                                        SharedAction.Navigate(
                                            MainDestination.Game(
                                                quizzesUids = it.quizzesUids ?: emptyList()
                                            ),
                                            navController
                                        )
                                    )
                                }
                            },
                            onStartNew = {
                                if (hasActiveSession) {
                                    pendingSessionAction = PendingSessionAction.StartNewGame
                                } else {
                                    startGameTrigger = true
                                }
                            }
                        )
                        Spacer(Modifier.height(28.dp))
                    }

                    // ── 6. Explore Topics (LazyRow, edge-to-edge) ──
                    val hasActiveSession =
                        currentSession != null && currentSession.expiredAt == null
                    ExploreTopicsSection(
                        categories = categoryState.categories,
                        onCategoryClick = { category ->
                            if (hasActiveSession) {
                                pendingSessionAction = PendingSessionAction.StartCategory(category)
                            } else {
                                selectedCategory = category
                            }
                        },
                        onBrowseAllClick = {
                            if (hasActiveSession) {
                                pendingSessionAction = PendingSessionAction.BrowseAll
                            } else {
                                sharedAction(
                                    SharedAction.Navigate(
                                        MainDestination.Browse,
                                        navController
                                    )
                                )
                            }
                        }
                    )

                    Spacer(Modifier.height(24.dp))

                    // ── 7. Shop banner ──
                    ShopBannerSection(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        onShopClick = {
                            sharedAction(
                                SharedAction.Navigate(
                                    MainDestination.Shop,
                                    navController
                                )
                            )
                        }
                    )

                    Spacer(Modifier.height(80.dp)) // Extra padding at the bottom so scroll reaches below sticky banner
                }
            }

            // ── 8. Banner Ad ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                BannerAd()
            }

            // ── 9. Sticky top-right: gems + trophies + settings pills ──
            TopBar(
                navController = navController,
                sharedAction = sharedAction,
                sessions = sessionState.sessions,
                userCoins = sharedState.user?.coins ?: Repository.getUser()?.coins ?: 0,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE SECTIONS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(
    title: String,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier
                .size(33.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GreetingSection() {
    val translator by TranslatorManager.translator.collectAsStateWithLifecycle()
    var greetingMessage by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(translator) {
        greetingMessage = Utils.greetingBasedOnTimezone(translator = translator)
    }

    val username = Repository.getUser()?.username ?: ""
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val taglineRes = when (hour) {
        in 0..11 -> R.string.home_greeting_tagline_morning
        in 12..17 -> R.string.home_greeting_tagline_afternoon
        else -> R.string.home_greeting_tagline_evening
    }

    Column {
        Text(
            text = "$greetingMessage, $username",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(taglineRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun QuoteSection(quoteState: QuoteState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp)
            )
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFFE2E8F0).copy(alpha = 0.6f),
                    cornerRadius = CornerRadius(18.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_book),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_quote_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (quoteState.quote?.quote != null)
                    "\u201C${quoteState.quote.quote}\u201D"
                else
                    stringResource(R.string.home_quote_not_found),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
            )

            if (!quoteState.quote?.author.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "\u2014 ${quoteState.quote.author}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PlayNowSection(
    hasActiveSession: Boolean,
    activeSession: Session?,
    onResume: () -> Unit,
    onStartNew: () -> Unit
) {
    if (hasActiveSession) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ContinueHeroButton(
                title = stringResource(R.string.home_action_continue),
                subtitle = stringResource(R.string.home_action_continue_subtitle),
                onClick = onResume
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = withTap(onStartNew))
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_action_start_new),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    } else {
        PrimaryCtaButton(
            text = stringResource(R.string.home_action_play_now),
            onClick = onStartNew
        )
    }
}

@Composable
private fun ContinueHeroButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val dark = isSystemInDarkTheme()
    val gradientStart = if (dark) Indigo500 else Indigo600
    val gradientEnd = if (dark) Indigo700 else Indigo500
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .scaleDownOnPress(.97f, interactionSource)
            .background(
                brush = Brush.linearGradient(listOf(gradientStart, gradientEnd)),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = withTap(onClick)
            )
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u25B6",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PrimaryCtaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val dark = isSystemInDarkTheme()
    val gradientStart = if (dark) Indigo500 else Indigo600
    val gradientEnd = if (dark) Indigo700 else Indigo500
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .scaleDownOnPress(.97f, interactionSource)
            .background(
                brush = Brush.linearGradient(listOf(gradientStart, gradientEnd)),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = withTap(onClick)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun PlayCardItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    containerColor: Color,
    contentColor: Color
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .scaleDownOnPress(.95f, interactionSource)
            .background(
                color = containerColor,
                shape = RoundedCornerShape(12.dp)
            )
            .drawBehind {
                drawRoundRect(
                    color = contentColor.copy(alpha = 0.2f),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = withTap(onClick)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
private fun ExploreTopicsSection(
    categories: List<Category>,
    onCategoryClick: (Category) -> Unit,
    onBrowseAllClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.home_title_featured_categories),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBrowseAllClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_action_browse_all),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        if (categories.isEmpty()) {
            Text(
                text = stringResource(R.string.home_categories_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        } else {
            // Pick today's category deterministically (same all day)
            val dayOfYear = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
            val dailyCategory = remember(categories, dayOfYear) {
                categories[dayOfYear % categories.size]
            }
            val suggested = remember(categories, dailyCategory) {
                categories.filter { it.uid != dailyCategory.uid }.take(6)
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "daily-${dailyCategory.uid}") {
                    DailyChallengeCard(
                        category = dailyCategory,
                        onClick = { onCategoryClick(dailyCategory) }
                    )
                }
                items(items = suggested, key = { it.uid }) { category ->
                    FeaturedCategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyChallengeCard(
    category: Category,
    onClick: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWidth * 0.38f
    val interactionSource = remember { MutableInteractionSource() }
    val dark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .width(cardWidth)
            .aspectRatio(0.78f)
            .scaleDownOnPress(.95f, interactionSource)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        if (dark) Indigo500 else Indigo600,
                        if (dark) Indigo700 else Indigo700
                    )
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = withTap(onClick)
            )
    ) {
        // Decorative big icon in the corner
        Image(
            painter = painterResource(category.toIconRes()),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 18.dp, y = (-8).dp)
                .size(110.dp)
                .graphicsLayer { alpha = 0.55f }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                    .wrapContentWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_fire),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.home_daily_challenge_label),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }

            Column {
                Text(
                    text = category.toShortName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.home_daily_challenge_sub),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FeaturedCategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWidth * 0.38f
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .width(cardWidth)
            .aspectRatio(0.78f)
            .scaleDownOnPress(.95f, interactionSource)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = withTap(onClick)
            )
    ) {
        Image(
            painter = painterResource(category.toIconRes()),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Bottom gradient scrim for label legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.55f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.72f)
                    )
                )
        )
        Text(
            text = category.toShortName(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STATS STRIP
// ─────────────────────────────────────────────────────────────────────────────

private data class HomeStats(val streak: Int, val bestScorePct: Int, val played: Int)

private fun computeHomeStats(sessions: List<Session>): HomeStats {
    val completed = sessions.filter {
        it.expiredAt != null && it.score != null && it.maxScore != null && it.maxScore!! > 0
    }

    val bestPct = completed.maxOfOrNull {
        (it.score!!.toDouble() / it.maxScore!!.toDouble() * 100).toInt()
    } ?: 0

    val played = completed.size

    // Streak = consecutive days (ending today or yesterday) with at least one session
    val oneDayMillis = TimeUnit.DAYS.toMillis(1)
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val days = sessions
        .mapNotNull { it.createdAt ?: it.expiredAt }
        .map { ts ->
            val c = Calendar.getInstance().apply {
                timeInMillis = ts
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            c.timeInMillis
        }
        .toSet()

    var streak = 0
    var cursor = today
    if (cursor !in days) cursor -= oneDayMillis // allow streak to have ended yesterday
    while (cursor in days) {
        streak++
        cursor -= oneDayMillis
    }

    return HomeStats(streak = streak, bestScorePct = bestPct, played = played)
}

@Composable
private fun StatsStrip(sessions: List<Session>) {
    val stats = remember(sessions) { computeHomeStats(sessions) }
    val dark = isSystemInDarkTheme()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatPill(
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.ic_fire,
            value = stats.streak.toString(),
            label = stringResource(R.string.home_stat_streak),
            accent = StreakOrange,
            bg = if (dark) StreakOrangeBgDark else StreakOrangeBg
        )
        StatPill(
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.ic_trophy,
            value = if (stats.bestScorePct > 0) "${stats.bestScorePct}%" else "--",
            label = stringResource(R.string.home_stat_best),
            accent = TrophyAmber,
            bg = if (dark) TrophyAmberBgDark else TrophyAmberBg
        )
        StatPill(
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.ic_check,
            value = stats.played.toString(),
            label = stringResource(R.string.home_stat_played),
            accent = PlayedTeal,
            bg = if (dark) PlayedTealBgDark else PlayedTealBg
        )
    }
}

@Composable
private fun StatPill(
    modifier: Modifier = Modifier,
    iconRes: Int,
    value: String,
    label: String,
    accent: Color,
    bg: Color
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(bg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SHOP BANNER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ShopBannerSection(
    onShopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val dark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scaleDownOnPress(.97f, interactionSource)
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        if (dark) Indigo500 else Indigo600,
                        if (dark) Indigo700 else Indigo500
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = withTap(onShopClick)
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "\uD83C\uDFEA", fontSize = 26.sp) // 🏪
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.shop_banner_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.shop_banner_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(12.dp))

            // CTA chip
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = stringResource(R.string.shop_banner_cta),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────

private enum class SettingsDialog { ABOUT, PRIVACY, TERMS }

/**
 * Daily-loot-box CTA. Glowing call-to-action while [available]; collapses to
 * a calm "come back tomorrow" footer once today's claim has been made so the
 * card never disappears entirely (keeps the layout stable and reinforces the
 * habit cue).
 */
@Composable
private fun DailyLootBoxSection(
    available: Boolean,
    streakDays: Int,
    onClaim: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val gradient = if (available) {
        Brush.linearGradient(
            listOf(Color(0xFFFFB300), Color(0xFFFF7043))
        )
    } else {
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surfaceVariant,
            )
        )
    }
    val onColor = if (available) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scaleDownOnPress(.97f, interactionSource)
            .background(brush = gradient, shape = RoundedCornerShape(20.dp))
            .clickable(
                enabled = available,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = withTap(onClaim),
            )
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (available) Color.White.copy(alpha = 0.22f)
                        else Color.Black.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(14.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "\uD83C\uDF81", fontSize = 26.sp) // 🎁
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        if (available) R.string.loot_box_card_title_available
                        else R.string.loot_box_card_title_claimed
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = onColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (available) stringResource(R.string.loot_box_card_subtitle_available)
                    else stringResource(R.string.loot_box_card_subtitle_claimed),
                    style = MaterialTheme.typography.bodySmall,
                    color = onColor.copy(alpha = 0.88f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (streakDays > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.loot_box_card_streak, streakDays),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = onColor.copy(alpha = 0.92f),
                    )
                }
            }
            if (available) {
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.25f),
                            RoundedCornerShape(999.dp),
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = stringResource(R.string.loot_box_card_cta),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    navController: NavController,
    sharedAction: (SharedAction) -> Unit,
    sessions: List<Session> = emptyList(),
    userCoins: Int = 0,
) {
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val coinTint = if (isSystemInDarkTheme()) GemCyanDark else GemCyan

    var menuExpanded by remember { mutableStateOf(false) }
    var activeDialog by remember { mutableStateOf<SettingsDialog?>(null) }

    // ── Dialogs ──
    activeDialog?.let { dialog ->
        val (title, body) = when (dialog) {
            SettingsDialog.ABOUT -> stringResource(R.string.settings_about_title) to
                    stringResource(R.string.settings_about_body)

            SettingsDialog.PRIVACY -> stringResource(R.string.settings_menu_privacy) to
                    stringResource(R.string.settings_legal_coming_soon)

            SettingsDialog.TERMS -> stringResource(R.string.settings_menu_terms) to
                    stringResource(R.string.settings_legal_coming_soon)
        }
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text(title, fontWeight = FontWeight.Bold) },
            text = { Text(body, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { activeDialog = null }) {
                    Text(stringResource(R.string.settings_ok))
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gems pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(surface, RoundedCornerShape(999.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_coin),
                contentDescription = null,
                tint = coinTint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "$userCoins",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = onSurface
            )
        }

        Spacer(Modifier.width(10.dp))

        // Trophies pill — aggregates achievements across all completed
        // sessions. Each distinct achievement is shown once with a count
        // badge of how many times it was earned and the first ever unlock
        // date. Clicking opens a scrollable dialog listing every unique
        // achievement.
        val unlockedAchievements = remember(sessions) {
            sessions
                .flatMap { session ->
                    val ts = session.expiredAt ?: session.createdAt ?: 0L
                    (session.achievements ?: emptyList()).map { res -> res to ts }
                }
                .groupBy { it.first }
                .map { (res, list) ->
                    AchievementSummary(
                        res = res,
                        count = list.size,
                        firstUnlockedAt = list.minOf { it.second }
                    )
                }
                .sortedByDescending { it.firstUnlockedAt }
        }
        var showAchievementsDialog by remember { mutableStateOf(false) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(surface, RoundedCornerShape(999.dp))
                .clip(RoundedCornerShape(999.dp))
                .clickable { showAchievementsDialog = true }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_trophy),
                contentDescription = null,
                tint = if (isSystemInDarkTheme()) GemCyanDark else GemCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "${unlockedAchievements.size}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = onSurface
            )
        }

        if (showAchievementsDialog) {
            AchievementsDialog(
                unlocked = unlockedAchievements,
                onDismiss = { showAchievementsDialog = false }
            )
        }

        Spacer(Modifier.width(10.dp))

        // Settings button + dropdown anchor
        Box {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(surface, CircleShape)
                    .clip(CircleShape)
                    .clickable { menuExpanded = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings_menu_language)) },
                    onClick = {
                        menuExpanded = false
                        sharedAction(SharedAction.Navigate(MainDestination.Language, navController))
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings_menu_about)) },
                    onClick = {
                        menuExpanded = false
                        activeDialog = SettingsDialog.ABOUT
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings_menu_privacy)) },
                    onClick = {
                        menuExpanded = false
                        activeDialog = SettingsDialog.PRIVACY
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings_menu_terms)) },
                    onClick = {
                        menuExpanded = false
                        activeDialog = SettingsDialog.TERMS
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ACHIEVEMENTS DIALOG
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AchievementsDialog(
    unlocked: List<AchievementSummary>,
    onDismiss: () -> Unit
) {
    val dateFormatter = remember {
        java.text.SimpleDateFormat(
            "MMM d, yyyy \u2022 HH:mm",
            java.util.Locale.getDefault()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.achievements_dialog_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (unlocked.isEmpty()) {
                Text(
                    text = stringResource(R.string.achievements_dialog_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = unlocked,
                        key = { it.res }
                    ) { summary ->
                        AchievementRow(
                            achievementRes = summary.res,
                            count = summary.count,
                            firstUnlockedAt = summary.firstUnlockedAt,
                            dateFormatter = dateFormatter
                        )
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_ok))
            }
        }
    )
}

internal data class AchievementSummary(
    val res: Int,
    val count: Int,
    val firstUnlockedAt: Long
)

@Composable
private fun AchievementRow(
    achievementRes: Int,
    count: Int,
    firstUnlockedAt: Long,
    dateFormatter: java.text.SimpleDateFormat
) {
    val (iconRes, descriptionRes) = Utils.achievementIcon(achievementRes)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon with count badge overlay
        Box(modifier = Modifier.size(44.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopStart)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            if (count > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "x$count",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 9.sp
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(achievementRes),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (firstUnlockedAt > 0L) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.achievements_first_unlocked_prefix,
                        dateFormatter.format(java.util.Date(firstUnlockedAt))
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                        .copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CATEGORY NAME EXTENSIONS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun Category.toShortName(): String {
    return when (id) {
        9 -> stringResource(R.string.category_short_9)
        10 -> stringResource(R.string.category_short_10)
        11 -> stringResource(R.string.category_short_11)
        12 -> stringResource(R.string.category_short_12)
        13 -> stringResource(R.string.category_short_13)
        14 -> stringResource(R.string.category_short_14)
        15 -> stringResource(R.string.category_short_15)
        16 -> stringResource(R.string.category_short_16)
        17 -> stringResource(R.string.category_short_17)
        18 -> stringResource(R.string.category_short_18)
        19 -> stringResource(R.string.category_short_19)
        20 -> stringResource(R.string.category_short_20)
        21 -> stringResource(R.string.category_short_21)
        22 -> stringResource(R.string.category_short_22)
        23 -> stringResource(R.string.category_short_23)
        24 -> stringResource(R.string.category_short_24)
        25 -> stringResource(R.string.category_short_25)
        26 -> stringResource(R.string.category_short_26)
        27 -> stringResource(R.string.category_short_27)
        28 -> stringResource(R.string.category_short_28)
        29 -> stringResource(R.string.category_short_29)
        30 -> stringResource(R.string.category_short_30)
        31 -> stringResource(R.string.category_short_31)
        32 -> stringResource(R.string.category_short_32)
        else -> name ?: "Unknown"
    }
}

fun Category.toIconRes(): Int = Utils.categoryImageRes(id)
