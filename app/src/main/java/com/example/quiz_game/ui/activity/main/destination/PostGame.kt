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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.App
import com.example.quiz_game.R
import com.example.quiz_game.other.Utils.achievementIcon
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.TextButton
import com.example.quiz_game.ui.shared.component.TextFancy
import com.example.quiz_game.ui.shared.component.TextSmol
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.SessionAction
import com.example.quiz_game.ui.viewmodel.SessionState
import com.example.quiz_game.ui.viewmodel.SharedAction
import kotlin.random.Random

/** showcasing game terminated session stats */
@Composable
fun PostGame(
        modifier: Modifier = Modifier,
        sharedAction: (SharedAction) -> Unit = {},
        quizAction: (QuizAction) -> Unit = {},
        quizState: QuizState = QuizState(),
        sessionState: SessionState = SessionState(),
        sessionAction: (SessionAction) -> Unit = {},
        navController: NavController = rememberNavController()
) {
    val sweepAngle = remember { Animatable(0f) }

    val score = sessionState.session?.score ?: -1
    val maxScore = sessionState.session?.maxScore ?: -1
    val achievements = sessionState.session?.achievements ?: emptyList()
    val nickname = App.userPrefs.getString("nickname", null) ?: "Player #0000"

    LaunchedEffect(sessionState) {
        val target =
                if (maxScore > 0) {
                    ((score.toFloat().coerceAtLeast(0f) / maxScore.toFloat()) * 360f).coerceIn(
                            0f,
                            360f
                    )
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
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TextFancy(text = nickname, color = MaterialTheme.colorScheme.primary)
                TextSmol(text = "$score / $maxScore")
            }

            Canvas(modifier = Modifier.size(200.dp).align(Alignment.BottomCenter)) {
                val correctAngle = sweepAngle.value

                // Draw background (gray) arc first - full circle
                drawArc(
                        color = Color.Gray,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(10f)
                )

                // Draw foreground (green) arc on top - representing correct score
                drawArc(
                        color = Color.Green,
                        startAngle = -90f,
                        sweepAngle = correctAngle,
                        useCenter = false,
                        style = Stroke(10f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.Gray, thickness = 2.dp)
        Spacer(modifier = Modifier.height(16.dp))

        Column {
            achievements.forEach { achievement ->
                AnimatedVisibility(
                        visible = true,
                        enter =
                                scaleIn(
                                        animationSpec =
                                                spring(
                                                        Spring.DampingRatioLowBouncy,
                                                        stiffness = Spring.StiffnessMedium
                                                ),
                                        initialScale = 0f
                                )
                ) {
                    TrophyCard(achievement = achievement)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.Gray, thickness = 2.dp)
        Spacer(modifier = Modifier.height(16.dp))

        ButtonPrimary(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    sharedAction(SharedAction.Navigate(MainDestination.Home, navController))
                }
        ) { TextButton(text = stringResource(R.string.postgame_button_navigate_home)) }
    }
}

@Composable
fun TrophyCard(modifier: Modifier = Modifier, achievement: Int) {
    val (iconRes, descriptionRes) = achievementIcon(achievement)

    val scale by
            animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )

    val rotation by
            animateFloatAsState(
                    targetValue =
                            arrayOf(Random.nextFloat() * 2f + 3f, Random.nextFloat() * 2f - 3f)
                                    .random(),
                    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )

    Card(
            modifier =
                    modifier.fillMaxWidth()
                            .padding(8.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale, rotationZ = rotation),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp),
    ) {
        Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = stringResource(descriptionRes),
                    modifier = Modifier.size(40.dp).clip(CircleShape).padding(8.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                TextButton(text = stringResource(achievement))
                TextSmol(text = stringResource(descriptionRes))
            }
        }
    }
}
