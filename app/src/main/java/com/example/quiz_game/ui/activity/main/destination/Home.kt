package com.example.quiz_game.ui.activity.main.destination

import android.icu.util.Calendar
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalConfiguration
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
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.TranslatorManager
import com.example.quiz_game.other.Utils
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.CardClickable
import com.example.quiz_game.ui.shared.component.DialogYesOrNo
import com.example.quiz_game.ui.shared.component.IconButton
import com.example.quiz_game.ui.shared.component.LoadingInfiniteLine
import com.example.quiz_game.ui.shared.effect.scaleDownOnPress
import com.example.quiz_game.ui.theme.NewGameGreen
import com.example.quiz_game.ui.theme.NewGameGreenContainer
import com.example.quiz_game.ui.theme.NewGameGreenDark
import com.example.quiz_game.ui.theme.NewGameGreenDarkContainer
import com.example.quiz_game.ui.viewmodel.CategoryAction
import com.example.quiz_game.ui.viewmodel.CategoryState
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.QuoteAction
import com.example.quiz_game.ui.viewmodel.QuoteState
import com.example.quiz_game.ui.viewmodel.SessionAction
import com.example.quiz_game.ui.viewmodel.SessionState
import com.example.quiz_game.ui.viewmodel.SharedAction



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
                painter = painterResource(if (isDarkTheme) R.drawable.img_illustration_home else R.drawable.img_illustration_home_light),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HeroImageHeight) // slightly taller to accommodate the parallax pull
                    .graphicsLayer {
                        // Move the image up at half the speed of the scroll
                        val scrollValue = scrollState.value.toFloat()
                        translationY = -scrollValue * 0.5f

                        // Calculate fade-out ratio (0f = fully visible, 1f = fully scrolled/darkened)
                        val maxScroll =
                            MaxScrollFading // Approx point where it should be almost black
                        val scrollRatio = (scrollValue / maxScroll).coerceIn(0f, 1f)

                        // Keep image visible, just darkened and blurred slightly
                        alpha = 1f - (scrollRatio * 0.5f)
                    }
            )

            // Foreground scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Transparent spacer to push content down so the image is visible
                Spacer(modifier = Modifier.height(ForegroundSpacerHeight))

                // Wrapper for all content with soft upper borders (~35% visual curve effect)
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
                        .padding(bottom = 90.dp) // Leave space for sticky language button
                ) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Spacer(Modifier.height(32.dp))

                        // ── 2. Greeting Section ──
                        GreetingSection()
                        Spacer(Modifier.height(32.dp))

                        // ── 3. Quote Section ──
                        QuoteSection(quoteState = quoteState)
                        Spacer(Modifier.height(32.dp))

                        // ── 4. Play Now Section ──
                        val hasActiveSession =
                            currentSession != null && currentSession.expiredAt == null
                        PlayNowSection(
                            hasActiveSession = hasActiveSession,
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
                        Spacer(Modifier.height(32.dp))
                    }

                    // ── 5. Featured Categories ──
                    val hasActiveSession =
                        currentSession != null && currentSession.expiredAt == null
                    FeaturedCategoriesSection(
                        categories = categoryState.categories.take(6),
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
                }
            }

            // ── 6. Sticky Language Button at Bottom ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                val interactionSource = remember { MutableInteractionSource() }
                val borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                Row(
                    modifier = Modifier
                        .scaleDownOnPress(.95f, interactionSource)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .drawBehind {
                            drawRoundRect(
                                color = borderColor,
                                cornerRadius = CornerRadius(24.dp.toPx()),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                        .clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current,
                            onClick = {
                                sharedAction(
                                    SharedAction.Navigate(
                                        MainDestination.Language,
                                        navController
                                    )
                                )
                            }
                        )
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        model = Utils.countryFlag(countryCode = selectedCountryCode),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = translated,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE SECTIONS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GreetingSection() {
    val translator by TranslatorManager.translator.collectAsStateWithLifecycle()
    var greetingMessage by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(translator) {
        greetingMessage = Utils.greetingBasedOnTimezone(translator = translator)
    }

    val username = Repository.getUser()?.username ?: ""

    // Reflect the time of day — mirrors the hour ranges in Utils.greetingBasedOnTimezone
    val currentHour =
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val timeOfDayEmoji = when (currentHour) {
        in 0..11 -> "🌤️"  // morning
        in 12..17 -> "☀️"  // afternoon
        in 18..20 -> "🌥️"  // early evening
        else -> "🌙"  // late evening & night
    }

    val greetingText = "$greetingMessage,\n$username $timeOfDayEmoji"

    // Gradient brush using theme colors
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = greetingText,
            style = TextStyle(
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                brush = gradientBrush,
                lineHeight = 44.sp,
                letterSpacing = (-1).sp
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun QuoteSection(quoteState: QuoteState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.home_quote_label), // Using exactly what's in the Mockup
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(14.dp))

        // Quote text with gradient left accent bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = RoundedCornerShape(percent = 50)
                    )
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (quoteState.quote?.quote != null)
                        "${quoteState.quote.quote}"
                    else
                        stringResource(R.string.home_quote_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (!quoteState.quote?.author.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "- ${quoteState.quote?.author}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayNowSection(
    hasActiveSession: Boolean,
    onResume: () -> Unit,
    onStartNew: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isDarkTheme = isSystemInDarkTheme()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val title =
            if (hasActiveSession) stringResource(R.string.home_title_resume_journey) else stringResource(
                R.string.home_title_play_now
            )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (hasActiveSession) {
            // Dual buttons when a session is active
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlayCardItem(
                    text = stringResource(R.string.home_action_resume),
                    onClick = onResume,
                    modifier = Modifier.weight(1f),
                    interactionSource = remember { MutableInteractionSource() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                PlayCardItem(
                    text = stringResource(R.string.home_action_new_game),
                    onClick = onStartNew,
                    modifier = Modifier.weight(1f),
                    interactionSource = remember { MutableInteractionSource() },
                    containerColor = if (isDarkTheme) NewGameGreenDarkContainer else NewGameGreenContainer,
                    contentColor = if (isDarkTheme) NewGameGreenDark else NewGameGreen
                )
            }
        } else {
            // Single large button when no session
            PlayCardItem(
                text = stringResource(R.string.home_action_play_now),
                onClick = onStartNew,
                modifier = Modifier.fillMaxWidth(),
                interactionSource = interactionSource,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
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
private fun FeaturedCategoriesSection(
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.home_action_browse_all),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBrowseAllClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        if (categories.isEmpty()) {
            Text(
                text = stringResource(R.string.home_categories_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items = categories, key = { it.uid }) { category ->
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
private fun FeaturedCategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWidth * 0.45f

    CardClickable(
        modifier = Modifier
            .width(cardWidth),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(9.dp)
        ) {
            Image(
                painter = painterResource(category.toIconRes()),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3 / 2.5f)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = category.toShortName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

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

fun Category.toIconRes(): Int {
    return when (id) {
        9 -> R.drawable.img_category_general
        10 -> R.drawable.img_category_books
        11 -> R.drawable.img_category_movies
        12 -> R.drawable.img_category_music
        13 -> R.drawable.img_category_musicals
        14 -> R.drawable.img_category_series
        15 -> R.drawable.img_category_video_games
        16 -> R.drawable.img_category_board_games
        17 -> R.drawable.img_category_nature
        18 -> R.drawable.img_category_computers
        19 -> R.drawable.img_category_math
        20 -> R.drawable.img_category_mythology
        21 -> R.drawable.img_category_sports
        22 -> R.drawable.img_category_geography
        23 -> R.drawable.img_category_history
        24 -> R.drawable.img_category_politics
        25 -> R.drawable.img_category_history
        26 -> R.drawable.img_category_movies
        27 -> R.drawable.img_category_animals
        28 -> R.drawable.img_category_general
        29 -> R.drawable.img_category_books
        30 -> R.drawable.img_category_computers
        31 -> R.drawable.img_category_movies
        32 -> R.drawable.img_category_movies
        else -> R.drawable.img_category_general
    }
}
