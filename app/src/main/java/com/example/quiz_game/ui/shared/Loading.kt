package com.example.quiz_game.ui.shared

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.quiz_game.R
import kotlinx.coroutines.delay

@Composable
fun LoadingInfiniteLine(modifier: Modifier = Modifier, vararg subject: String) {
    var subjectState by rememberSaveable { mutableStateOf(subject.random()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            subjectState = subject.random()
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier.scale(.8f)
        ) {
            TextBig(text = stringResource(R.string.loading_title))
            Spacer(Modifier.width(8.dp))
            AnimatedContent(
                targetState = subjectState,
                transitionSpec = {
                    (fadeIn(tween(200)) + slideInVertically { height -> height }).togetherWith(
                        fadeOut(
                            tween(200)
                        ) + slideOutVertically { height -> -height })
                }
            ) { target ->
                TextBig(text = target)
            }
        }

        LinearProgressIndicator()
    }
}