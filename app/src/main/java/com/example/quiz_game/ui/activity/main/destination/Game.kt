package com.example.quiz_game.ui.activity.main.destination

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastFilter
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.App
import com.example.quiz_game.R
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.quiz.Quiz
import com.example.quiz_game.data.session.Session
import com.example.quiz_game.data.shop.ShopItem
import com.example.quiz_game.data.shop.ShopItemType
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.DailyRewards
import com.example.quiz_game.other.Sound
import com.example.quiz_game.other.SoundManager
import com.example.quiz_game.other.withTap
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.BannerAd
import com.example.quiz_game.ui.shared.component.GameSkeletonLoader
import com.example.quiz_game.ui.shared.component.TextBig
import com.example.quiz_game.ui.shared.component.TextButton
import com.example.quiz_game.ui.shared.component.TextFancy
import com.example.quiz_game.ui.shared.component.TextSmol
import com.example.quiz_game.ui.shared.effect.scaleDownOnPress
import com.example.quiz_game.ui.theme.Indigo600
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.SessionAction
import com.example.quiz_game.ui.viewmodel.SessionState
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedState
import com.example.quiz_game.ui.viewmodel.ShopAction
import com.example.quiz_game.ui.viewmodel.ShopState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun Game(
    modifier: Modifier = Modifier,
    quizzesUids: List<String> = emptyList(),
    quizState: StateFlow<QuizState>,
    quizAction: (QuizAction) -> Unit = {},
    sharedState: SharedState = SharedState(),
    sharedAction: (SharedAction) -> Unit = {},
    sessionState: StateFlow<SessionState>,
    sessionAction: (SessionAction) -> Unit = {},
    shopState: ShopState = ShopState(),
    shopAction: (ShopAction) -> Unit = {},
    navController: NavController = rememberNavController(),
) {

    var currentSession: Session? by remember { mutableStateOf(null) }
    var currentQuizzes: List<Quiz> by remember { mutableStateOf(emptyList()) }
    var currentQuizState: QuizState by remember { mutableStateOf(QuizState()) }
    var currentErrors by remember { mutableStateOf(emptyList<String>()) }
    var loading by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    DisposableEffect(currentSession) {
        if (currentSession != null) {
            loading = true
            scope.launch {
                quizState
                    .first { quizState ->
                        !quizState.executing && quizState.sessionQuizzes.isNotEmpty()
                    }
                    .let {
                        currentQuizzes = it.sessionQuizzes.fastFilter { quiz -> !quiz.expired }
                        currentQuizState = it
                        loading = false
                    }
            }
        }

        onDispose {
            currentQuizState = QuizState()
            currentQuizzes = emptyList()
        }
    }

    DisposableEffect(sessionState) {
        loading = true
        scope.launch {
            sessionState
                .first { session ->
                    val activeSession = session.session
                    activeSession != null &&
                            activeSession.uid.isNotEmpty() &&
                            activeSession.expiredAt == null
                }
                .let {
                    val activeSession = it.session!!
                    currentSession = activeSession
                    quizAction(QuizAction.GetBySession(activeSession.quizzesUids!!))
                    loading = false
                }
        }

        onDispose { currentSession = null }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    currentSession?.let { session ->
        var quizIndex by rememberSaveable(session.uid) { mutableIntStateOf(0) }
        var incorrectlyAnswered by rememberSaveable(session.uid) { mutableIntStateOf(0) }

        // First-quiz-of-the-day silent x2 multiplier. Computed ONCE at the
        // start of this session and frozen for its duration so progress
        // mid-session won't flip the bonus off after we've already updated
        // the lastQuizCompletedAt stamp on completion.
        val sessionFirstOfDay = rememberSaveable(session.uid) {
            DailyRewards.isFirstQuizOfDay(
                now = System.currentTimeMillis(),
                lastQuizCompletedAt = Repository.getUser()?.lastQuizCompletedAt ?: 0L,
            )
        }
        val sessionMultiplier = if (sessionFirstOfDay) DailyRewards.FIRST_QUIZ_OF_DAY_MULTIPLIER else 1

        val quiz = currentQuizzes.getOrNull(quizIndex)

        // Build list of owned collectibles for the tray
        val ownedCollectibles = remember(shopState.ownedCounts, shopState.items) {
            shopState.items.filter { (shopState.ownedCounts[it.id] ?: 0) > 0 }
        }

        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                loading -> {
                    GameSkeletonLoader()
                }

                currentQuizzes.isEmpty() -> {
                    Text(text = "No available quizzes")
                }

                quiz != null -> {
                    val choices =
                        remember(quiz.uid) {
                            buildList {
                                add(quiz.correctAnswer ?: "")
                                addAll(quiz.incorrectAnswers.orEmpty())
                                shuffle()
                            }
                        }

                    QuizCard(
                        quiz = quiz,
                        choices = choices,
                        correctChoice = quiz.correctAnswer,
                        questionIndex = quizIndex,
                        totalQuestions = currentQuizzes.size,
                        firstOfDayBonus = sessionFirstOfDay,
                        ownedCollectibles = ownedCollectibles,
                        ownedCounts = shopState.ownedCounts,
                        onUseCollectible = { item ->
                            shopAction(ShopAction.UseItem(item))
                        },
                        onRequestSwap = {
                            scope.launch {
                                try {
                                    // Fetch a pool of quizzes and pick one not in the current session
                                    val sessionUids = currentQuizzes.map { it.uid }.toSet()
                                    val pool = Repository.quizRepository.get()
                                        .filter { !it.expired && it.uid !in sessionUids }
                                    val replacement = pool.randomOrNull()
                                    if (replacement != null) {
                                        val newList = currentQuizzes.toMutableList()
                                        newList[quizIndex] = replacement
                                        currentQuizzes = newList
                                    }
                                } catch (_: Exception) {
                                    // silently ignore; user still has choice to answer
                                }
                            }
                        },
                        onAnswered = { answer, mark ->
                            if (answer != quiz.correctAnswer) {
                                incorrectlyAnswered += 1
                            }

                            // Apply the silent first-quiz-of-the-day x2 bonus
                            // on top of whatever (already-multiplied) mark the
                            // QuizCard reports. Wrong answers stay 0.
                            val finalMark = mark * sessionMultiplier

                            quizAction(QuizAction.UpdateExpired(quiz.uid))

                            if (quizIndex >= currentQuizzes.lastIndex) {
                                // Award 1-2 random collectibles as session-end reward
                                shopAction(ShopAction.GrantRandom((1..2).random()))

                                // Stamp lastQuizCompletedAt so subsequent
                                // sessions today no longer qualify for the
                                // first-quiz-of-day bonus, and tell SharedVM
                                // to re-emit the user snapshot.
                                App.ioScope.launch {
                                    Repository.updateUser { it.copy(lastQuizCompletedAt = System.currentTimeMillis()) }
                                    sharedAction(SharedAction.RefreshUser)
                                }

                                // Funnel the final mark through CompleteSession
                                // so score, expiredAt, and achievements are
                                // persisted atomically and in the right order.
                                sessionAction(
                                    SessionAction.CompleteSession(
                                        uid = session.uid,
                                        finalMark = finalMark,
                                        incorrectlyAnswered = incorrectlyAnswered
                                    )
                                )

                                if (activity != null) {
                                    com.example.quiz_game.other.AdManager.onQuizCompleted(activity)
                                }

                                sharedAction(
                                    SharedAction.Navigate(
                                        MainDestination.PostGame,
                                        navController
                                    )
                                )
                            } else {
                                sessionAction(SessionAction.UpdateScore(session.uid, finalMark))
                                if (activity != null) {
                                    com.example.quiz_game.other.AdManager.onQuizCompleted(activity)
                                }
                                quizIndex += 1
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuizCard(
    modifier: Modifier = Modifier,
    quiz: Quiz = Quiz(),
    choices: List<String> = emptyList(),
    correctChoice: String? = null,
    questionIndex: Int = 0,
    totalQuestions: Int = 1,
    firstOfDayBonus: Boolean = false,
    ownedCollectibles: List<ShopItem> = emptyList(),
    ownedCounts: Map<String, Int> = emptyMap(),
    onUseCollectible: (ShopItem) -> Unit = {},
    onRequestSwap: () -> Unit = {},
    onAnswered: (String, Int) -> Unit = { _, _ -> },
) {
    var answer by rememberSaveable(quiz.uid) { mutableStateOf("") }
    var enabled by rememberSaveable(quiz.uid) { mutableStateOf(true) }
    var answeredState by rememberSaveable(quiz.uid) { mutableStateOf(AnsweredState.IDLE) }
    var timer by rememberSaveable(quiz.uid) { mutableIntStateOf(Constants.DEFAULT_QUIZ_TIMER) }
    // Choices eliminated by HINT power-up (stored as indices into `choices`)
    var eliminatedChoices by rememberSaveable(quiz.uid) { mutableStateOf(setOf<String>()) }
    // Per-question flags so each power-up can only be used once per question
    var hintUsed by rememberSaveable(quiz.uid) { mutableStateOf(false) }
    var timeUsed by rememberSaveable(quiz.uid) { mutableStateOf(false) }
    var swapUsed by rememberSaveable(quiz.uid) { mutableStateOf(false) }
    // Score-multiplier state. `pendingMultiplier` is the active risk-bet for
    // this question (1 == no multiplier). It is committed BEFORE the user
    // picks any answer; once `answeredState` leaves IDLE, the multiplier is
    // locked. On a correct lock-in the question's mark is multiplied; on a
    // wrong / unanswered lock-in the multiplier is consumed for nothing
    // (soft-penalty model — no coin loss, just a wasted power-up).
    var pendingMultiplier by rememberSaveable(quiz.uid) { mutableIntStateOf(1) }
    var multiplierUsed by rememberSaveable(quiz.uid) { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Tracks the SoundPool stream id of the most recent countdown tick so we
    // can stop it before starting the next one. The supplied tick samples may
    // be longer than 1 second, which would otherwise cause overlapping ticks
    // to pile up, bleed across questions, and even survive into the PostGame
    // screen.
    var tickStreamId by remember(quiz.uid) { mutableIntStateOf(0) }

    // Always silence any in-flight tick if this composable leaves the
    // composition (e.g. user navigates to PostGame, Home, or rotates away).
    DisposableEffect(quiz.uid) {
        onDispose {
            SoundManager.stop(tickStreamId)
            tickStreamId = 0
        }
    }

    LaunchedEffect(answeredState) {
        when (answeredState) {
            AnsweredState.PICKED -> {
                scope.launch { scrollState.animateScrollTo(scrollState.maxValue) }
            }
            AnsweredState.LOCKED -> {
                // Stop any countdown tick the moment an answer is locked in
                // (either by user confirm or timeout) so the result cue plays
                // cleanly without the previous tick still ringing on top.
                SoundManager.stop(tickStreamId)
                tickStreamId = 0

                // Result SFX:
                //   • UNANSWERED — only when the timer ran out and the user
                //     never picked any choice (`answer` stays at its initial
                //     empty string in that path).
                //   • CORRECT / WRONG — fired in every other LOCKED case
                //     (user confirmed a pick, or timer expired after a pick
                //     was made but not confirmed).
                val isUnanswered = answer.isBlank()
                val isCorrect = !isUnanswered && answer == correctChoice
                SoundManager.play(
                    when {
                        isUnanswered -> Sound.ANSWER_UNANSWERED
                        isCorrect -> Sound.ANSWER_CORRECT
                        else -> Sound.ANSWER_WRONG
                    }
                )
                delay(1500L)
                onAnswered(answer, if (isCorrect) (quiz.mark!! * pendingMultiplier) else 0)
            }
            else -> Unit
        }
    }

    LaunchedEffect(quiz.uid) {
        while (timer > 0 && answeredState != AnsweredState.LOCKED) {
            delay(100L)
            if (com.example.quiz_game.other.AdManager.isAdShowing.value) {
                continue
            }
            delay(900L)
            if (com.example.quiz_game.other.AdManager.isAdShowing.value) {
                continue
            }
            if (answeredState != AnsweredState.LOCKED) {
                timer -= 1
                // Per-second tick. Switch to the intense variant once the
                // remaining time has dropped into the "red" range (matches
                // the timer-color threshold below). Stop the previous tick
                // first so back-to-back longer samples don't overlap.
                if (timer > 0) {
                    val intense = timer < Constants.DEFAULT_QUIZ_TIMER / 3
                    SoundManager.stop(tickStreamId)
                    tickStreamId = SoundManager.play(
                        if (intense) Sound.COUNTDOWN_TICK_INTENSE
                        else Sound.COUNTDOWN_TICK
                    )
                }
            }
        }
        // Final cleanup once the loop exits — answer-locked path goes through
        // the LOCKED handler above, but this also covers any other exit.
        SoundManager.stop(tickStreamId)
        tickStreamId = 0
        if (timer <= 0 && answeredState != AnsweredState.LOCKED) {
            enabled = false
            answeredState = AnsweredState.LOCKED
        }
    }

    val timerColor = when (timer) {
        in Constants.DEFAULT_QUIZ_TIMER / 2..Constants.DEFAULT_QUIZ_TIMER ->
            MaterialTheme.colorScheme.primary
        in Constants.DEFAULT_QUIZ_TIMER / 3..Constants.DEFAULT_QUIZ_TIMER / 2 -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.error
    }

    val timerProgress by animateFloatAsState(
        targetValue = timer.toFloat() / Constants.DEFAULT_QUIZ_TIMER,
        animationSpec = tween(durationMillis = 800),
        label = "timerProgress"
    )

    val categoryImageRes = quiz.category.toCategoryImageRes()
    val surfaceColor = MaterialTheme.colorScheme.background

    // Centralised power-up handler — invoked from the bottom-overlay deck.
    val useCollectible: (ShopItem) -> Unit = { item ->
        when (item.type) {
            ShopItemType.SKIP -> {
                onUseCollectible(item)
                onAnswered(correctChoice ?: "", quiz.mark ?: 0)
            }
            ShopItemType.TIME_BONUS -> {
                if (!timeUsed) {
                    timeUsed = true
                    onUseCollectible(item)
                    timer += 10
                }
            }
            ShopItemType.HINT -> {
                if (!hintUsed) {
                    hintUsed = true
                    onUseCollectible(item)
                    val wrong = choices.filter { it != correctChoice }
                    eliminatedChoices = wrong.shuffled().take(2).toSet()
                }
            }
            ShopItemType.SWAP -> {
                if (!swapUsed) {
                    swapUsed = true
                    onUseCollectible(item)
                    onRequestSwap()
                }
            }
            ShopItemType.SCORE_MULTIPLIER -> {
                // Only armable while the user has not yet picked an answer
                // (per the design: the multiplier is a confidence bet placed
                // BEFORE seeing your own commit). Also, one-shot per question.
                if (!multiplierUsed && answeredState == AnsweredState.IDLE) {
                    multiplierUsed = true
                    pendingMultiplier = item.multiplier.coerceAtLeast(1)
                    onUseCollectible(item)
                }
            }
        }
    }

    // ── Full-screen layout ──────────────────────────────────────────────────────
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {
        // ── Hero image — full width, top 42% of screen ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(0.42f)
        ) {
            Image(
                painter = painterResource(categoryImageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(
                    ColorMatrix().apply { setToSaturation(0.35f) }
                ),
                modifier = Modifier.fillMaxSize()
            )
            // Top-to-bottom gradient: translucent primary tint → transparent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            0.5f to Color.Transparent,
                            1f to surfaceColor.copy(alpha = 0.85f)
                        )
                    )
            )
        }

        // ── Scrollable content panel ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Push content down below the image (slightly overlapping for seamless merge)
            Spacer(Modifier.fillMaxWidth().height(200.dp))

            // Content card — rounded top corners, sits on background
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(surfaceColor)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(20.dp))

                // ── Header row: category label + counter + timer ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    quiz.category?.let {
                        TextFancy(
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Question counter chip
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 2.dp
                        ) {
                            Text(
                                text = "${questionIndex + 1} / $totalQuestions",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        // Active score-multiplier badge — only visible while
                        // a SCORE_MULTIPLIER power-up is armed for this
                        // question. Subtle pulsing glow advertises the risk.
                        if (pendingMultiplier > 1) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFFFFC107),
                                tonalElevation = 4.dp,
                                shadowElevation = 4.dp,
                            ) {
                                Text(
                                    text = "x$pendingMultiplier",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF3E2723),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }

                        // Circular timer
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { timerProgress },
                                modifier = Modifier.size(44.dp),
                                color = timerColor,
                                trackColor = timerColor.copy(alpha = 0.15f),
                                strokeWidth = 3.5.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = "$timer",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = timerColor
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── First-quiz-of-the-day silent bonus banner ──
                // Only shown on the very first question of the very first
                // session of the calendar day, as a soft "go for it" cue.
                if (firstOfDayBonus) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFF3CD),
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(R.string.game_first_of_day_banner),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7A5A00),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                }

                // ── Question text ──
                quiz.question?.let {
                    TextBig(
                        text = it,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(20.dp))
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 1.dp
                )

                Spacer(Modifier.height(16.dp))

                // ── Answer choices ──
                if (choices.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        choices.forEach { choice ->
                            val eliminated = choice in eliminatedChoices
                            if (!eliminated) {
                                AnswerChoiceCard(
                                    text = choice,
                                    isSelected = answer == choice,
                                    isCorrect = correctChoice == choice,
                                    answeredState = answeredState,
                                    enabled = enabled || answer == choice || correctChoice == choice,
                                    showMark = answeredState == AnsweredState.LOCKED && correctChoice == choice,
                                    mark = quiz.mark ?: 0,
                                    wasAnswered = answer == choice,
                                    onClick = {
                                        if (answer == choice) {
                                            answer = ""
                                            answeredState = AnsweredState.IDLE
                                        } else {
                                            answer = choice
                                            answeredState = AnsweredState.PICKED
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── Confirm / Next button ──
                AnimatedVisibility(
                    visible = answeredState != AnsweredState.IDLE,
                    enter = fadeIn(tween(250)) + slideInVertically(
                        animationSpec = tween(350),
                        initialOffsetY = { it }
                    ),
                    exit = fadeOut()
                ) {
                    Column {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 1.dp
                        )

                        Spacer(Modifier.height(16.dp))

                        ButtonPrimary(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                enabled = false
                                answeredState = AnsweredState.LOCKED
                            },
                            enabled = answeredState == AnsweredState.PICKED
                        ) {
                            TextButton(
                                text = when (answeredState) {
                                    AnsweredState.IDLE -> "Waiting..."
                                    AnsweredState.PICKED -> "Confirm answer"
                                    AnsweredState.LOCKED -> "Next question"
                                }
                            )
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }

                // Reserve extra bottom space so the bottom-overlay deck never
                // hides the last answer choice while collapsed. Always reserve
                // an additional BANNER_AD_HEIGHT so the banner ad pinned at
                // the very bottom of the screen never overlaps the scrollable
                // content either.
                if (ownedCollectibles.isNotEmpty() && answeredState == AnsweredState.IDLE) {
                    Spacer(Modifier.height(96.dp + BANNER_AD_HEIGHT))
                } else {
                    Spacer(Modifier.height(BANNER_AD_HEIGHT))
                }
            }
        }

        // ── Bottom-anchored collectibles deck (overlay) ──
        // The deck lifts itself by BANNER_AD_HEIGHT so neither its collapsed
        // fan nor its expanded row overlaps the banner ad pinned below. The
        // scrim still covers the entire screen because it lives inside the
        // outer Box (not the deck's own offset wrapper).
        if (ownedCollectibles.isNotEmpty() && answeredState == AnsweredState.IDLE) {
            CollectiblesDeck(
                items = ownedCollectibles,
                ownedCounts = ownedCounts,
                onUse = useCollectible,
                bottomInset = BANNER_AD_HEIGHT
            )
        }

        // ── Banner ad pinned at the very bottom of the screen ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(MaterialTheme.colorScheme.background)
        ) {
            BannerAd()
        }
    }
}

/** Standard AdMob BANNER size is 320×50dp; we reserve 50dp for layout. */
private val BANNER_AD_HEIGHT = 50.dp

@Composable
private fun AnswerChoiceCard(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    answeredState: AnsweredState,
    enabled: Boolean,
    showMark: Boolean,
    mark: Int,
    wasAnswered: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource =
        remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    val locked = answeredState == AnsweredState.LOCKED

    val borderColor: Color
    val containerColor: Color
    val textColor: Color

    when {
        locked && isCorrect -> {
            borderColor = Color(0xFF16A34A)
            containerColor = Color(0xFF16A34A).copy(alpha = 0.12f)
            textColor = Color(0xFF16A34A)
        }
        locked && wasAnswered -> {
            borderColor = MaterialTheme.colorScheme.error
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
            textColor = MaterialTheme.colorScheme.error
        }
        isSelected && !locked -> {
            borderColor = Indigo600
            containerColor = Indigo600.copy(alpha = 0.10f)
            textColor = MaterialTheme.colorScheme.onSurface
        }
        else -> {
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            containerColor = MaterialTheme.colorScheme.surface
            textColor = MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (locked) 0.4f else 1f
            )
        }
    }

    Box {
        OutlinedCard(
            onClick = onClick,
            enabled = !locked && enabled,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = if ((isSelected && !locked) || (locked && isCorrect) || (locked && wasAnswered)) 2.dp else 1.dp,
                color = borderColor
            ),
            colors = CardDefaults.outlinedCardColors(
                containerColor = containerColor,
                contentColor = textColor,
                disabledContainerColor = containerColor,
                disabledContentColor = textColor
            ),
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .scaleDownOnPress(0.97f, interactionSource)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected || (locked && isCorrect)) FontWeight.SemiBold else FontWeight.Normal,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Score badge
        AnimatedVisibility(
            visible = showMark,
            enter = scaleIn(
                animationSpec = tween(400, easing = EaseInExpo),
                initialScale = 0.4f
            ) + fadeIn(tween(300)),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 10.dp, y = (-10).dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (wasAnswered && isCorrect) Color(0xFF16A34A)
                else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Text(
                    text = if (wasAnswered && isCorrect) "+$mark" else "+0",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (wasAnswered && isCorrect) Color.White
                    else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ── Category name → image resource ────────────────────────────────────────────
@DrawableRes
fun String?.toCategoryImageRes(): Int = when {
    this == null -> R.drawable.img_category_general
    contains("General", ignoreCase = true) -> R.drawable.img_category_general
    contains("Book", ignoreCase = true) -> R.drawable.img_category_books
    contains("Film", ignoreCase = true) || contains("Movie", ignoreCase = true) -> R.drawable.img_category_film
    contains("Musical", ignoreCase = true) || contains("Theatre", ignoreCase = true) -> R.drawable.img_category_musicals_theatres
    contains("Music", ignoreCase = true) -> R.drawable.img_category_music
    contains("Television", ignoreCase = true) || contains("TV", ignoreCase = true) || contains("Series", ignoreCase = true) -> R.drawable.img_category_television
    contains("Video Game", ignoreCase = true) -> R.drawable.img_category_videogames
    contains("Board Game", ignoreCase = true) -> R.drawable.img_category_boardgames
    contains("Science", ignoreCase = true) || contains("Nature", ignoreCase = true) -> R.drawable.img_category_science_nature
    contains("Computer", ignoreCase = true) -> R.drawable.img_category_computers
    contains("Gadget", ignoreCase = true) -> R.drawable.img_category_gadgets
    contains("Math", ignoreCase = true) -> R.drawable.img_category_mathematics
    contains("Mythology", ignoreCase = true) -> R.drawable.img_category_mythology
    contains("Sport", ignoreCase = true) -> R.drawable.img_category_sports
    contains("Geography", ignoreCase = true) -> R.drawable.img_category_geography
    contains("History", ignoreCase = true) -> R.drawable.img_category_history
    contains("Politics", ignoreCase = true) -> R.drawable.img_category_politics
    contains("Art", ignoreCase = true) -> R.drawable.img_category_art
    contains("Celebrit", ignoreCase = true) -> R.drawable.img_category_celebrities
    contains("Animal", ignoreCase = true) -> R.drawable.img_category_animals
    contains("Vehicle", ignoreCase = true) -> R.drawable.img_category_vehicles
    contains("Comic", ignoreCase = true) -> R.drawable.img_category_comics
    contains("Anime", ignoreCase = true) || contains("Manga", ignoreCase = true) -> R.drawable.img_category_japaneseanime_manga
    contains("Cartoon", ignoreCase = true) || contains("Animation", ignoreCase = true) -> R.drawable.img_category_cartoon_animations
    else -> R.drawable.img_category_general
}

enum class AnsweredState {
    IDLE,
    PICKED,
    LOCKED
}

/**
 * A bottom-anchored deck of stylised power-up cards. While collapsed the deck
 * peeks out from the bottom of the screen with low opacity in a fanned stack;
 * tapping any card expands the deck into a fan of full-information cards which
 * the user can pick from. The expanded row is horizontally scrollable so all
 * cards remain reachable on small screens. A tap on the scrim or a back-press
 * collapses the deck without leaving the quiz.
 */
@Composable
private fun BoxScope.CollectiblesDeck(
    items: List<ShopItem>,
    ownedCounts: Map<String, Int>,
    onUse: (ShopItem) -> Unit,
    bottomInset: androidx.compose.ui.unit.Dp = 0.dp,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    if (expanded) {
        BackHandler { expanded = false }
    }

    val scrimAlpha by animateFloatAsState(
        targetValue = if (expanded) 0.55f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "collectibles_scrim"
    )

    // Scrim — only intercepts touches when expanded.
    if (expanded) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { SoundManager.play(Sound.TAP); expanded = false }
        )
    }

    val collapsedHeight = 110.dp
    val expandedHeight = 260.dp
    val containerHeight by animateDpAsState(
        targetValue = if (expanded) expandedHeight else collapsedHeight,
        animationSpec = tween(durationMillis = 320),
        label = "collectibles_height"
    )

    // Width of the inner card-canvas when expanded — a generous span so the
    // last card never gets clipped on phones, with horizontal scroll picking
    // up the slack on narrow displays. Collapsed simply fills the parent.
    val expandedSpanDp = ((items.size + 1) * 140).dp
    val scrollState = rememberScrollState()

    // Centre the cards row inside the viewport on expand; reset on collapse so
    // the next expansion animates from the same starting point.
    LaunchedEffect(expanded) {
        if (expanded) {
            // Wait for the scroll container to be laid out (maxValue becomes
            // > 0 once the wide inner canvas is measured), then jump to the
            // middle so the spread is visually centred on screen.
            snapshotFlow { scrollState.maxValue }.first { it > 0 }
            scrollState.scrollTo(scrollState.maxValue / 2)
        } else {
            scrollState.scrollTo(0)
        }
    }

    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(bottom = bottomInset)
            .height(containerHeight)
    ) {
        // Title strip — fixed at the top of the deck, NOT inside the scroll
        // container, so it stays perfectly centred regardless of scroll
        // position. Only rendered when expanded.
        if (expanded) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.collectibles_tray_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.collectibles_use_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.78f)
                )
            }
        }

        // Cards row — the only horizontally-scrollable element. Anchored to
        // the bottom of the deck so the title can sit above it without
        // overlap.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(180.dp)
                .then(if (expanded) Modifier.horizontalScroll(scrollState) else Modifier)
        ) {
            // Inner canvas: fixed wide when expanded (so horizontalScroll has
            // something to scroll), fills the parent when collapsed (so the
            // fanned cards centre naturally on screen).
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .then(
                        if (expanded) Modifier.width(expandedSpanDp)
                        else Modifier.fillMaxWidth()
                    )
            ) {
                items.forEachIndexed { index, item ->
                    val count = ownedCounts[item.id] ?: 0
                    val centered = index - (items.size - 1) / 2f

                    // Collapsed: cards stack into a fan close to the bottom
                    // edge with a subtle horizontal spread + rotation.
                    // Expanded: they spread out evenly across the (scrollable)
                    // wide canvas.
                    val targetX = if (expanded) (centered * 140f).dp else (centered * 18f).dp
                    val absCentered = if (centered < 0f) -centered else centered
                    val targetY = if (expanded) 0.dp else (absCentered * 4f).dp
                    val targetRot = if (expanded) 0f else centered * 6f
                    val targetAlpha = if (expanded) 1f else 0.55f
                    val targetScale = if (expanded) 1f else 0.78f

                    val animX by animateDpAsState(targetX, tween(320), label = "card_x_$index")
                    val animY by animateDpAsState(targetY, tween(320), label = "card_y_$index")
                    val animRot by animateFloatAsState(targetRot, tween(320), label = "card_rot_$index")
                    val animAlpha by animateFloatAsState(targetAlpha, tween(320), label = "card_alpha_$index")
                    val animScale by animateFloatAsState(targetScale, tween(320), label = "card_scale_$index")

                    CollectibleBigCard(
                        item = item,
                        count = count,
                        onClick = {
                            if (expanded) {
                                onUse(item)
                                expanded = false
                            } else {
                                expanded = true
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset { IntOffset(animX.roundToPx(), animY.roundToPx()) }
                            .graphicsLayer {
                                rotationZ = animRot
                                alpha = animAlpha
                                scaleX = animScale
                                scaleY = animScale
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectibleBigCard(
    item: ShopItem,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(width = 124.dp, height = 168.dp)
            .scaleDownOnPress(0.95f, interactionSource)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        Indigo600,
                        Indigo600.copy(alpha = 0.78f)
                    )
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = withTap(onClick)
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                androidx.compose.material3.Icon(
                    painter = painterResource(item.icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.22f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "x$count",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Column {
                Text(
                    text = stringResource(item.nameRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(item.descRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 9.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }
}
