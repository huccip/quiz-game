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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.category.Category
import com.example.quiz_game.data.session.Session
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.TranslatorManager
import com.example.quiz_game.other.Utils
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.CardClickable
import com.example.quiz_game.ui.shared.component.DialogYesOrNo
import com.example.quiz_game.ui.shared.component.InformativeChip
import com.example.quiz_game.ui.shared.component.LoadingInfiniteLine
import com.example.quiz_game.ui.shared.effect.scaleDownOnPress
import com.example.quiz_game.ui.theme.GemCyan
import com.example.quiz_game.ui.theme.GemCyanDark
import com.example.quiz_game.ui.theme.Indigo100
import com.example.quiz_game.ui.theme.Indigo500
import com.example.quiz_game.ui.theme.Indigo600
import com.example.quiz_game.ui.theme.Indigo700
import com.example.quiz_game.ui.theme.NewGameGreen
import com.example.quiz_game.ui.theme.NewGameGreenContainer
import com.example.quiz_game.ui.theme.NewGameGreenDark
import com.example.quiz_game.ui.theme.NewGameGreenDarkContainer
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
    sharedAction: (SharedAction) -> Unit = {},
    sessionState: SessionState = SessionState(),
    sessionAction: (SessionAction) -> Unit = {},
    categoryAction: (CategoryAction) -> Unit = {},
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

    val isDarkTheme = isSystemInDarkTheme()

    if (quizState.executing || categoryState.executing || startGameTrigger || isCategoryLoading) {
        LoadingInfiniteLine(subject = stringArrayResource(R.array.home_loading_subjects))
    } else {
        val scrollState = rememberScrollState()

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── 1. Edge-to-edge Illustration (Parallax) ──
            Image(
                painter = painterResource(R.drawable.img_illustration_home),
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

                    // ── 7. Quick actions ──
                    QuickActionsSection(
                        categories = categoryState.categories,
                        onRandomClick = { cat ->
                            if (hasActiveSession) {
                                pendingSessionAction = PendingSessionAction.StartCategory(cat)
                            } else {
                                selectedCategory = cat
                            }
                        },
                        onBrowseClick = {
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
                }
            }

            // ── 8. Sticky top-right: gems + settings pills ──
            TopBar(
                onSettingsClick = {
                    // TODO: sharedAction(SharedAction.Navigate(MainDestination.Settings, navController))
                }
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
            maxLines = 1,
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
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
                    .clickable(onClick = onStartNew)
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
                onClick = onClick
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
                onClick = onClick
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
                onClick = onClick
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
                onClick = onClick
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
                onClick = onClick
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
// QUICK ACTIONS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickActionsSection(
    categories: List<Category>,
    onRandomClick: (Category) -> Unit,
    onBrowseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.home_quick_random),
            subtitle = stringResource(R.string.home_quick_random_sub),
            icon = "\uD83C\uDFB2", // 🎲
            tint = MaterialTheme.colorScheme.primary,
            onClick = {
                if (categories.isNotEmpty()) onRandomClick(categories.random())
            }
        )
        QuickActionCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.home_quick_browse),
            subtitle = stringResource(R.string.home_quick_browse_sub),
            icon = "\uD83C\uDF10", // 🌐
            tint = if (isSystemInDarkTheme()) NewGameGreenDark else NewGameGreen,
            onClick = onBrowseClick
        )
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: String,
    tint: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .scaleDownOnPress(.96f, interactionSource)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(tint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(
    onSettingsClick: () -> Unit
) {
    val userCoins = Repository.getUser()?.coins ?: 0
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val coinTint = if (isSystemInDarkTheme()) GemCyanDark else GemCyan

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

        // Settings pill
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(surface, CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onSettingsClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
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
