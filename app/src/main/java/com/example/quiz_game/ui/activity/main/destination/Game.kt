package com.example.quiz_game.ui.activity.main.destination

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInExpo
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.data.quiz.Quiz
import com.example.quiz_game.data.session.Session
import com.example.quiz_game.other.Constants
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.LoadingFullScreenLowOpacityWithInfiniteSpinner
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

    currentSession?.let { session ->
        var quizIndex by rememberSaveable(session.uid) { mutableIntStateOf(0) }
        var incorrectlyAnswered by rememberSaveable(session.uid) { mutableIntStateOf(0) }

        val quiz = currentQuizzes.getOrNull(quizIndex)

        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                loading -> {
                    LoadingFullScreenLowOpacityWithInfiniteSpinner()
                }

                currentQuizzes.isEmpty() -> {
                    TextSmol(text = "No available quizzes")
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
                        onAnswered = { answer, mark ->
                            if (answer != quiz.correctAnswer) {
                                incorrectlyAnswered += 1
                            }

                            sessionAction(SessionAction.UpdateScore(session.uid, mark))
                            quizAction(QuizAction.UpdateExpired(quiz.uid))

                            if (quizIndex >= currentQuizzes.lastIndex) {
                                sessionAction(
                                    SessionAction.UpdateTrophies(
                                        session.uid,
                                        incorrectlyAnswered
                                    )
                                )
                                sessionAction(SessionAction.EndSession(session.uid))

                                sharedAction(
                                    SharedAction.Navigate(
                                        MainDestination.PostGame,
                                        navController
                                    )
                                )
                            } else {
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
    onAnswered: (String, Int) -> Unit = { _, _ -> },
) {
    var answer by rememberSaveable(quiz.uid) { mutableStateOf("") }
    var enabled by rememberSaveable(quiz.uid) { mutableStateOf(true) }
    var answeredState by rememberSaveable(quiz.uid) { mutableStateOf(AnsweredState.IDLE) }
    var timer by rememberSaveable(quiz.uid) { mutableIntStateOf(Constants.DEFAULT_QUIZ_TIMER) }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(answeredState) {
        when (answeredState) {
            AnsweredState.PICKED -> {
                // Auto-scroll to reveal the confirm button
                scope.launch { scrollState.animateScrollTo(scrollState.maxValue) }
            }
            AnsweredState.LOCKED -> {
                delay(1500L)
                onAnswered(answer, if (answer == correctChoice) quiz.mark!! else 0)
            }
            else -> Unit
        }
    }

    LaunchedEffect(quiz.uid) {
        while (timer > 0 && answeredState != AnsweredState.LOCKED) {
            delay(1000L)
            if (answeredState != AnsweredState.LOCKED) {
                timer -= 1
            }
        }
        if (timer <= 0 && answeredState != AnsweredState.LOCKED) {
            enabled = false
            answeredState = AnsweredState.LOCKED
        }
    }

    // Timer color logic
    val timerColor = when (timer) {
        in Constants.DEFAULT_QUIZ_TIMER / 2..Constants.DEFAULT_QUIZ_TIMER ->
            MaterialTheme.colorScheme.primary
        in Constants.DEFAULT_QUIZ_TIMER / 6..Constants.DEFAULT_QUIZ_TIMER / 2 -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.error
    }

    val timerProgress by animateFloatAsState(
        targetValue = timer.toFloat() / Constants.DEFAULT_QUIZ_TIMER,
        animationSpec = tween(durationMillis = 800),
        label = "timerProgress"
    )

    val categoryImageRes = quiz.category.toCategoryImageRes()
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.verticalScroll(scrollState)
        ) {

            // ── Category banner image ──────────────────────────────────────
            // Grayscale crop with a primary-tinted top gradient fading into the card surface,
            // so it blends into the content beneath without a hard edge.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Image(
                    painter = painterResource(categoryImageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(
                        ColorMatrix().apply { setToSaturation(0f) }
                    ),
                    modifier = Modifier.fillMaxSize()
                )
                // Top-to-bottom: primary tint → transparent → card surface fade
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to primaryColor.copy(alpha = 0.30f),
                                0.45f to Color.Transparent,
                                1f to surfaceColor.copy(alpha = 0.90f)
                            )
                        )
                )
            }

            // ── Card body ──────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(12.dp))

                // ── Header row: category label + counter chip + circular timer ──
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

                // ── Question text ──────────────────────────────────────────
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

                // ── Answer choices ─────────────────────────────────────────
                if (choices.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        choices.forEach { choice ->
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
                                    answer = choice
                                    answeredState = AnsweredState.PICKED
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── Confirm / Next button — hidden until an answer is picked ──
                // Mirrors the Language screen: slides up from the bottom the first time
                // the user taps a choice; invisible (and takes no space) while IDLE.
                AnimatedVisibility(
                    visible = answeredState != AnsweredState.IDLE,
                    enter = fadeIn(tween(250)) + slideInVertically(
                        animationSpec = tween(350),
                        initialOffsetY = { it }
                    ),
                    exit = fadeOut() + slideOutVertically { it }
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

                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

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
        locked && wasAnswered && !isCorrect -> {
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
                alpha = if (locked && !isCorrect && !wasAnswered) 0.4f else 1f
            )
        }
    }

    Box {
        OutlinedCard(
            onClick = onClick,
            enabled = !locked && enabled,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = if ((isSelected && !locked) || (locked && isCorrect) || (locked && wasAnswered && !isCorrect)) 2.dp else 1.dp,
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

        // Score badge — pops out of the top-end corner of the correct answer card
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
// Matches by keyword so it handles both short ("Film") and full ("Entertainment: Film")
// category name strings without needing to decode categoryUid.
@DrawableRes
fun String?.toCategoryImageRes(): Int = when {
    this == null -> R.drawable.img_category_general
    contains("General", ignoreCase = true) -> R.drawable.img_category_general
    contains("Book", ignoreCase = true) -> R.drawable.img_category_books
    contains("Film", ignoreCase = true) || contains("Movie", ignoreCase = true) -> R.drawable.img_category_movies
    contains("Musical", ignoreCase = true) || contains("Theatre", ignoreCase = true) -> R.drawable.img_category_musicals
    contains("Music", ignoreCase = true) -> R.drawable.img_category_music
    contains("Television", ignoreCase = true) || contains("TV", ignoreCase = true) || contains("Series", ignoreCase = true) -> R.drawable.img_category_series
    contains("Video Game", ignoreCase = true) -> R.drawable.img_category_video_games
    contains("Board Game", ignoreCase = true) -> R.drawable.img_category_board_games
    contains("Nature", ignoreCase = true) -> R.drawable.img_category_nature
    contains("Science", ignoreCase = true) -> R.drawable.img_category_science
    contains("Computer", ignoreCase = true) || contains("Gadget", ignoreCase = true) -> R.drawable.img_category_computers
    contains("Math", ignoreCase = true) -> R.drawable.img_category_math
    contains("Mythology", ignoreCase = true) -> R.drawable.img_category_mythology
    contains("Sport", ignoreCase = true) -> R.drawable.img_category_sports
    contains("Geography", ignoreCase = true) -> R.drawable.img_category_geography
    contains("History", ignoreCase = true) -> R.drawable.img_category_history
    contains("Politics", ignoreCase = true) -> R.drawable.img_category_politics
    contains("Animal", ignoreCase = true) -> R.drawable.img_category_animals
    else -> R.drawable.img_category_general
}

enum class AnsweredState {
    IDLE,
    PICKED,
    LOCKED
}
