package com.example.quiz_game.ui.activity.main.destination

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastSumBy
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.App
import com.example.quiz_game.R
import com.example.quiz_game.other.Utils.achievementIcon
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.ButtonPrimary
import com.example.quiz_game.ui.shared.TextBig
import com.example.quiz_game.ui.shared.TextButton
import com.example.quiz_game.ui.shared.TextFancy
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.SharedAction
import kotlin.random.Random

/**
 * showcasing game terminated session stats
 * */

@Composable
fun PostGame(
    modifier: Modifier = Modifier,
    sharedAction: (SharedAction) -> Unit = {},
    quizAction: (QuizAction) -> Unit,
    quizState: QuizState = QuizState(),
    expiredUids: List<String> = emptyList(),
    navController: NavController = rememberNavController()
) {
    val maximumScore by rememberSaveable {
        mutableIntStateOf(
            quizState.quizzes
                .filter { it.expired }
                .fastSumBy { it.mark ?: 0 }
        )
    }
    val userScore by rememberSaveable {
        mutableIntStateOf(
            App.userPrefs.getInt("score", 0)
        )
    }
    val achievements = App.userPrefs.getStringSet("achievements", emptySet())
    val username by rememberSaveable {
        mutableStateOf(App.userPrefs.getString("nickname", null))
    }

    LaunchedEffect(Unit) {
        expiredUids.onEach {
            quizAction(QuizAction.DeleteByUid(it))
        }
    }

    // Animate the sweep angle of the arc from 0 to the current score
    val sweepAngle by animateFloatAsState(
        targetValue = (userScore.toFloat() / maximumScore.toFloat()) * 360f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header section with user info
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TextFancy(
                text = username ?: "Undefined"
            )

            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomCenter)
            ) {
                drawArc(
                    color = Color.Green,
                    startAngle = 0f,
                    sweepAngle = sweepAngle, // Animated sweepAngle
                    useCenter = true,
                    size = Size(100f, 100f),
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
                    TrophyCard(achievement = achievement)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.Gray, thickness = 2.dp)
        Spacer(modifier = Modifier.height(16.dp))

        ButtonPrimary(
            onClick = {
                sharedAction(SharedAction.Navigate(MainDestination.Home, navController))
            }
        ) {
            TextButton(
                text = stringResource(R.string.postgame_button_navigate_home)
            )
        }
    }
}

@Composable
fun TrophyCard(modifier: Modifier = Modifier, achievement: String) {
    val context = LocalContext.current
    val iconRes = achievementIcon(achievement, context)

    // Scale and Rotation Animation
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )

    val rotation by animateFloatAsState(
        targetValue = Random.nextFloat() * 10f + 20f,  // Random between 20° and 30°
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .graphicsLayer(
                scaleX = scale,  // Apply scale animation
                scaleY = scale,  // Apply scale animation
                rotationZ = rotation // Apply rotation animation
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon for the achievement
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = achievement,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFB6E5B))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Achievement Text
            TextBig(
                text = achievement,
            )
        }
    }
}
