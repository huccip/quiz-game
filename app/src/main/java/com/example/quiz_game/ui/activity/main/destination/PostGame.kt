package com.example.quiz_game.ui.activity.main.destination

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastSumBy
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.App
import com.example.quiz_game.R
import com.example.quiz_game.other.Utils.achievementIcon
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.ButtonPrimary
import com.example.quiz_game.ui.shared.TextButton
import com.example.quiz_game.ui.shared.TextFancy
import com.example.quiz_game.ui.shared.TextSmol
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.SharedAction
import kotlin.random.Random

/**
 * showcasing game terminated session stats
 * */

// TODO: Fix score shows 0 no matter what

@Composable
fun PostGame(
    modifier: Modifier = Modifier,
    sharedAction: (SharedAction) -> Unit = {},
    quizAction: (QuizAction) -> Unit = {},
    quizState: QuizState = QuizState(),
    navController: NavController = rememberNavController()
) {
    LaunchedEffect(Unit) {
        quizAction(QuizAction.GetAll)
    }

    val maximumScore = remember(quizState) {
        quizState.quizzes
            .filter { it.expired }
            .fastSumBy { it.mark ?: 0 }
    }

    val userScore by rememberUpdatedState(App.userPrefs.getInt("score", 0))
    val achievements by rememberUpdatedState(App.userPrefs.getStringSet("achievements", emptySet()))
    val username by rememberUpdatedState(App.userPrefs.getString("nickname", null))

    val sweepAngle = remember { Animatable(0f) }

    LaunchedEffect(maximumScore, userScore) {
        val target = if (maximumScore > 0) {
            (userScore.toFloat() / maximumScore.toFloat()) * 360f
        } else {
            0f
        }
        sweepAngle.animateTo(
            target,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TextFancy(
                    text = username ?: "Undefined",
                    color = MaterialTheme.colorScheme.primary
                )
                TextSmol(text = "$userScore / $maximumScore")
            }

            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.BottomCenter)
            ) {
                val correctAngle = sweepAngle.value
                val incorrectAngle = 360f - correctAngle

                drawArc(
                    color = Color.Gray,
                    startAngle = correctAngle,
                    sweepAngle = incorrectAngle,
                    useCenter = false,
                    style = Stroke(10f)
                )

                drawArc(
                    color = Color.Green,
                    startAngle = 90f,
                    sweepAngle = correctAngle,
                    useCenter = false,
                    style = Stroke(10f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.Gray, thickness = 2.dp)
        Spacer(modifier = Modifier.height(16.dp))

        achievements?.let {
            Column {
                it.forEach { achievement ->
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(
                            animationSpec = spring(
                                Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium
                            ), initialScale = 0f
                        )
                    ) {
                        TrophyCard(achievement = achievement)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.Gray, thickness = 2.dp)
        Spacer(modifier = Modifier.height(16.dp))

        ButtonPrimary(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                // delete expired quizzes
                quizState.quizzes
                    .fastFilter { it.expired }
                    .fastForEach { quizAction(QuizAction.DeleteByUid(it.uid)) }

                // back home we go
                sharedAction(SharedAction.Navigate(MainDestination.Home, navController))
            }
        ) {
            TextButton(text = stringResource(R.string.postgame_button_navigate_home))
        }
    }
}

@Composable
fun TrophyCard(modifier: Modifier = Modifier, achievement: String) {
    val context = LocalContext.current
    val iconRes = achievementIcon(achievement, context)

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )

    val rotation by animateFloatAsState(
        targetValue = arrayOf(
            Random.nextFloat() * 2f + 3f,
            Random.nextFloat() * 2f - 3f
        ).random(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                rotationZ = rotation
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = achievement,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            TextButton(text = achievement)
        }
    }
}