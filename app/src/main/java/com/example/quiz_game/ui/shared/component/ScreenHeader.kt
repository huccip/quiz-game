package com.example.quiz_game.ui.shared.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Reusable screen header with an optional large title, subtitle, and illustration.
 *
 * Use across all screens for a uniform look:
 * - [title] is the bold hero heading (e.g. "Welcome!", "Create Profile", "Start Quiz").
 * - [subtitle] is the softer instructional line underneath.
 * - [illustration] an optional premium illustration to show at the top of the header.
 * - [showTitle] controls whether the big title is rendered (e.g. only on first-time onboarding).
 */
@Composable
fun ScreenHeader(
        title: String,
        subtitle: String,
        modifier: Modifier = Modifier,
        @DrawableRes illustration: Int? = null,
        showTitle: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        illustration?.let { resId ->
            Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(180.dp)
            )
            Spacer(Modifier.height(28.dp))
        }

        // Title + subtitle are left-aligned
        Column(modifier = Modifier.fillMaxWidth()) {
            AnimatedVisibility(
                    visible = showTitle,
                    enter = fadeIn() + slideInVertically { -it / 2 }
            ) {
                Column {
                    Text(
                            text = title,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
