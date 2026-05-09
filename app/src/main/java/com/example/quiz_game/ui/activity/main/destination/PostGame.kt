package com.example.quiz_game.ui.activity.main.destination

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.other.AdManager
import com.example.quiz_game.other.InAppReviewManager
import com.example.quiz_game.other.Sound
import com.example.quiz_game.other.SoundManager
import com.example.quiz_game.other.Utils.achievementIcon
import com.example.quiz_game.other.withTap
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.BannerAd
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.IconButton
import com.example.quiz_game.ui.shared.component.TextBerySmol
import com.example.quiz_game.ui.shared.component.TextBig
import com.example.quiz_game.ui.shared.component.TextButton
import com.example.quiz_game.ui.shared.component.TextFancy
import com.example.quiz_game.ui.theme.Violet100
import com.example.quiz_game.ui.theme.Violet600
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.SessionAction
import com.example.quiz_game.ui.viewmodel.SessionState
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.ShopAction
import com.example.quiz_game.ui.viewmodel.ShopState
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

// Arc color is always green regardless of score
private val ArcGreen = Color(0xFF22C55E)

/** showcasing game terminated session stats */
@Composable
fun PostGame(
    modifier: Modifier = Modifier,
    sharedAction: (SharedAction) -> Unit = {},
    quizAction: (QuizAction) -> Unit = {},
    quizState: QuizState = QuizState(),
    sessionState: SessionState = SessionState(),
    sessionAction: (SessionAction) -> Unit = {},
    shopState: ShopState = ShopState(),
    shopAction: (ShopAction) -> Unit = {},
    navController: NavController = rememberNavController()
) {
    val sweepAngle = remember { Animatable(0f) }

    val score = sessionState.session?.score ?: 0
    val maxScore = sessionState.session?.maxScore ?: 0
    val achievements = sessionState.session?.achievements ?: emptyList()

    val scorePercentage =
        if (maxScore > 0) ((score.coerceAtLeast(0).toFloat() / maxScore.toFloat()) * 100).toInt()
        else 0

    // Percentage text is colored by performance
    val scoreTextColor = when {
        scorePercentage >= 80 -> Color(0xFF22C55E)
        scorePercentage >= 50 -> Violet600
        else -> Color(0xFFEF4444)
    }

    // Distinct categories from session quizzes — name + image resource
    val categoryChips = remember(quizState.sessionQuizzes) {
        quizState.sessionQuizzes
            .mapNotNull { quiz -> quiz.category }
            .distinct()
            .take(8)
            .map { name -> name to categoryNameToImageRes(name) }
    }

    // Stable scatter params per chip — seeded by index so they never recompose-jitter
    // Each entry: (xOffsetDp, yOffsetDp, rotation, sizeDp, alpha)
    val scatterParams = remember(categoryChips.size) {
        List(categoryChips.size) { i ->
            val rng = Random(seed = i * 31 + 7)
            // Split items: even indices go left, odd go right
            val isRight = (i % 2 == 1)
            // Horizontal offset: 90–130dp from center, capped so chips stay inbound
            // Screen is ~360dp wide, ring is 190dp → each side has ~85dp to the edge.
            // Using 90–120dp keeps chips clearly inside with a ~20dp inset from the edge.
            val xMagnitude = rng.nextFloat() * 30f + 90f
            val xOffset = if (isRight) xMagnitude else -xMagnitude
            // Vertical offset: spread vertically, first chips near top, last near bottom
            // Map i to a vertical band across the ring height (~200dp), add jitter
            val fraction =
                if (categoryChips.size > 1) i.toFloat() / (categoryChips.size - 1) else 0.5f
            val yCenter = (fraction - 0.5f) * 160f  // -80..+80 dp
            val yJitter = (rng.nextFloat() - 0.5f) * 40f
            val yOffset = yCenter + yJitter
            // Rotation: positive for right side, negative for left
            val rotMagnitude = rng.nextFloat() * 10f + 4f
            val rotation = if (isRight) rotMagnitude else -rotMagnitude
            // Image size: 32–46dp
            val size = rng.nextFloat() * 14f + 32f
            // Alpha: 0.65–1.0
            val alpha = rng.nextFloat() * 0.35f + 0.65f
            ScatterParams(xOffset, yOffset, rotation, size, alpha)
        }
    }

    val context = LocalContext.current
    BackHandler {
        AdManager.onQuizCompleted(context as Activity)
        sharedAction(SharedAction.Navigate(MainDestination.Home, navController))
    }

    // Silently prompt for an in-app review whenever the user sets a new
    // personal high score — the most positive moment in the session.
    LaunchedEffect(achievements) {
        if (R.string.achievements_new_record in achievements) {
            InAppReviewManager.requestReview(context as Activity)
        }
    }

    LaunchedEffect(sessionState) {
        val target =
            if (maxScore > 0) {
                ((score.toFloat().coerceAtLeast(0f) / maxScore.toFloat()) * 360f)
                    .coerceIn(0f, 360f)
            } else 0f
        // Looping "counting up" tone that plays for the duration of the
        // sweep animation, then is stopped once the arc finishes.
        val streamId = SoundManager.playLooped(Sound.POSTGAME_PROGRESS)
        try {
            sweepAngle.animateTo(
                target,
                animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
            )
        } finally {
            SoundManager.stop(streamId)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // ── Header — score ring + scattered category chips ────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    // Fixed height gives us room to scatter chips around the ring
                    .height(if (categoryChips.isNotEmpty()) 380.dp else 300.dp),
                contentAlignment = Alignment.Center
            ) {
                // Title sits at top of the box
                TextFancy(
                    text = stringResource(R.string.postgame_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 36.dp)
                )

                // Scattered category chips — drawn as absolute-offset children of the same Box,
                // centred on the ring centre, offset outward with random params
                categoryChips.forEachIndexed { index, (name, imageRes) ->
                    val p = scatterParams[index]
                    ScatteredCategoryChip(
                        name = name,
                        imageRes = imageRes,
                        rotation = p.rotation,
                        sizeDp = p.sizeDp,
                        chipAlpha = p.alpha,
                        delayMs = index * 100L,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset {
                                IntOffset(
                                    x = (p.xOffset * density).roundToInt(),
                                    y = (p.yOffset * density).roundToInt()
                                )
                            }
                    )
                }

                // Score ring — centred in the box
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Canvas(modifier = Modifier.size(190.dp)) {
                        val strokeWidth = 16.dp.toPx()
                        // Track
                        drawArc(
                            color = Color.Gray.copy(alpha = 0.22f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        // Progress — always green
                        if (sweepAngle.value > 0f) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        ArcGreen.copy(alpha = 0.55f),
                                        ArcGreen
                                    )
                                ),
                                startAngle = -90f,
                                sweepAngle = sweepAngle.value,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Only the percentage inside the ring — big, bold, coloured by performance
                    androidx.compose.material3.Text(
                        text = "$scorePercentage%",
                        color = scoreTextColor,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ── Achievements section ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // ── Earned collectibles reward card ──
                val granted = shopState.lastGranted
                if (granted.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Violet100.copy(alpha = 0.55f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 16.dp)
                        ) {
                            TextBig(
                                text = stringResource(
                                    R.string.collectibles_reward_title,
                                    granted.size
                                ),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            TextBerySmol(
                                text = stringResource(R.string.collectibles_reward_subtitle),
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                granted.forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MaterialTheme.colorScheme.surface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        androidx.compose.material3.Icon(
                                            painter = painterResource(id = item.icon),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    // Note: granted collectibles intentionally remain visible for
                    // the entire visit to PostGame — the user explicitly requested
                    // a permanent reward summary instead of a fleeting toast.
                }

                // Achievements are revealed exclusively via the Steam-style popup
                // overlay rendered at the top of the screen — no in-page section
                // here on purpose.

                Spacer(modifier = Modifier.height(32.dp))

                ButtonPrimary(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    onClick = {
                        AdManager.onQuizCompleted(context as Activity)
                        sharedAction(SharedAction.Navigate(MainDestination.Home, navController))
                    }
                ) {
                    TextButton(
                        text = stringResource(R.string.postgame_button_navigate_home),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
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

        // ── Steam-style achievement popups (overlay) ────────────────────────
        AchievementPopupHost(
            achievements = achievements,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
        )
    }
}

private data class ScatterParams(
    val xOffset: Float,   // dp
    val yOffset: Float,   // dp
    val rotation: Float,  // degrees
    val sizeDp: Float,    // dp — image circle diameter
    val alpha: Float
)

@Composable
private fun ScatteredCategoryChip(
    name: String,
    imageRes: Int,
    rotation: Float,
    sizeDp: Float,
    chipAlpha: Float,
    delayMs: Long = 0L,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(name) {
        delay(delayMs)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chip_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .wrapContentSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
                alpha = chipAlpha
            }
    ) {
        // Card container — monochromatic image inside a rounded surface card
        Card(
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.size(sizeDp.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(
                    ColorMatrix().apply { setToSaturation(0f) }
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        // Full name, no truncation
        androidx.compose.material3.Text(
            text = name,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp,
            modifier = Modifier.width(sizeDp.dp + 16.dp)
        )
    }
}

@Composable
private fun AchievementRecapRow(
    modifier: Modifier = Modifier,
    achievement: Int,
) {
    val (iconRes, descriptionRes) = achievementIcon(achievement)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Violet100.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    painter = painterResource(id = iconRes),
                    contentDescription = stringResource(descriptionRes),
                    style = MaterialTheme.typography.titleLarge,
                    tint = Violet600
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                TextBig(
                    text = stringResource(achievement),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                TextBerySmol(
                    text = stringResource(descriptionRes),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * Steam-style achievement popup host. Plays an animated, dismissable popup for
 * each unlocked achievement in [achievements], one at a time, reveling them
 * shortly after the screen enters and auto-dismissing each after a short
 * window. Tapping a popup dismisses it immediately.
 */
@Composable
private fun AchievementPopupHost(
    achievements: List<Int>,
    modifier: Modifier = Modifier,
    perPopupDurationMs: Long = 3500L,
    initialDelayMs: Long = 600L,
) {
    if (achievements.isEmpty()) return
    var currentIndex by remember(achievements) { mutableStateOf(0) }
    var showing by remember(achievements) { mutableStateOf(false) }

    LaunchedEffect(achievements) {
        delay(initialDelayMs)
        while (currentIndex < achievements.size) {
            // Steam-style "ding" right as each popup slides in.
            SoundManager.play(Sound.ACHIEVEMENT_POPUP)
            showing = true
            delay(perPopupDurationMs)
            showing = false
            delay(220L) // exit animation room
            currentIndex += 1
        }
    }

    val current = achievements.getOrNull(currentIndex)

    Box(modifier = modifier) {
        androidx.compose.animation.AnimatedVisibility(
            visible = showing && current != null,
            enter = slideInVertically(
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                initialOffsetY = { -it }
            ) + fadeIn(tween(220)),
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                targetOffsetY = { -it / 2 }
            ) + fadeOut(tween(180))
        ) {
            current?.let {
                AchievementPopup(
                    achievement = it,
                    onDismiss = { showing = false }
                )
            }
        }
    }
}

@Composable
private fun AchievementPopup(
    achievement: Int,
    onDismiss: () -> Unit,
) {
    val (iconRes, descriptionRes) = achievementIcon(achievement)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = withTap(onDismiss)
            ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Violet600.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Violet600, Violet600.copy(alpha = 0.75f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    painter = painterResource(id = iconRes),
                    contentDescription = stringResource(descriptionRes),
                    style = MaterialTheme.typography.titleLarge,
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                androidx.compose.material3.Text(
                    text = stringResource(R.string.achievement_popup_title),
                    style = MaterialTheme.typography.labelSmall,
                    color = Violet600,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                androidx.compose.material3.Text(
                    text = stringResource(achievement),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                androidx.compose.material3.Text(
                    text = stringResource(descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun AchievementCard(
    modifier: Modifier = Modifier,
    achievement: Int,
    revealDelayMs: Long = 0L
) {
    val (iconRes, descriptionRes) = achievementIcon(achievement)

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(achievement) {
        delay(revealDelayMs)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "achievement_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "achievement_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Violet100.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    painter = painterResource(id = iconRes),
                    contentDescription = stringResource(descriptionRes),
                    style = MaterialTheme.typography.titleLarge,
                    tint = Violet600
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                TextBig(
                    text = stringResource(achievement),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                TextBerySmol(
                    text = stringResource(descriptionRes),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * Maps a category name string (as stored in Quiz.category) to its drawable image resource.
 */
private fun categoryNameToImageRes(name: String): Int {
    return when {
        name.contains("General", ignoreCase = true) || name.contains("Knowledge", ignoreCase = true) ->
            R.drawable.img_category_general

        name.contains("Book", ignoreCase = true) || name.contains("Literature", ignoreCase = true) ->
            R.drawable.img_category_books

        name.contains("Film", ignoreCase = true) || name.contains("Movie", ignoreCase = true) ->
            R.drawable.img_category_film

        name.contains("Musical", ignoreCase = true) || name.contains("Theatre", ignoreCase = true) ->
            R.drawable.img_category_musicals_theatres

        name.contains("Music", ignoreCase = true) ->
            R.drawable.img_category_music

        name.contains("Television", ignoreCase = true) || name.contains("TV", ignoreCase = true) || name.contains("Series", ignoreCase = true) ->
            R.drawable.img_category_television

        name.contains("Video Game", ignoreCase = true) ->
            R.drawable.img_category_videogames

        name.contains("Board Game", ignoreCase = true) ->
            R.drawable.img_category_boardgames

        name.contains("Science", ignoreCase = true) || name.contains("Nature", ignoreCase = true) || name.contains("Biolog", ignoreCase = true) ->
            R.drawable.img_category_science_nature

        name.contains("Computer", ignoreCase = true) ->
            R.drawable.img_category_computers

        name.contains("Gadget", ignoreCase = true) ->
            R.drawable.img_category_gadgets

        name.contains("Math", ignoreCase = true) || name.contains("Mathemat", ignoreCase = true) ->
            R.drawable.img_category_mathematics

        name.contains("Mytholog", ignoreCase = true) ->
            R.drawable.img_category_mythology

        name.contains("Sport", ignoreCase = true) ->
            R.drawable.img_category_sports

        name.contains("Geograph", ignoreCase = true) ->
            R.drawable.img_category_geography

        name.contains("Histor", ignoreCase = true) ->
            R.drawable.img_category_history

        name.contains("Politic", ignoreCase = true) ->
            R.drawable.img_category_politics

        name.contains("Art", ignoreCase = true) ->
            R.drawable.img_category_art

        name.contains("Celebrit", ignoreCase = true) ->
            R.drawable.img_category_celebrities

        name.contains("Animal", ignoreCase = true) ->
            R.drawable.img_category_animals

        name.contains("Vehicle", ignoreCase = true) ->
            R.drawable.img_category_vehicles

        name.contains("Comic", ignoreCase = true) ->
            R.drawable.img_category_comics

        name.contains("Anime", ignoreCase = true) || name.contains("Manga", ignoreCase = true) ->
            R.drawable.img_category_japaneseanime_manga

        name.contains("Cartoon", ignoreCase = true) || name.contains("Animation", ignoreCase = true) ->
            R.drawable.img_category_cartoon_animations

        else -> R.drawable.img_category_general
    }
}
