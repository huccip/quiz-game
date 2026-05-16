package com.example.quiz_game.ui.shared.component

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer

/**
 * Reusable shimmer placeholder primitive. All screen-level skeletons compose
 * themselves out of these so the visual language stays consistent.
 *
 * The colour is theme-aware (subtly tuned for dark mode) and the shimmer
 * animation comes from the `compose-shimmer` library.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    val dark = isSystemInDarkTheme()
    val placeholderColor = if (dark) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    }
    Box(
        modifier = modifier
            .clip(shape)
            .shimmer()
            .background(placeholderColor, shape)
    )
}

/** Convenience: a single-line text shimmer of a given width. */
@Composable
private fun ShimmerLine(
    width: Dp? = null,
    height: Dp = 14.dp,
    modifier: Modifier = Modifier,
) {
    ShimmerBox(
        modifier = modifier
            .then(if (width != null) Modifier.size(width = width, height = height) else Modifier.fillMaxWidth().height(height)),
        shape = RoundedCornerShape(6.dp),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Home
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Skeleton placeholder mirroring the layout of the Home screen while the
 * categories / quizzes / quotes are loading.
 *
 *  ‑ Top hero / greeting block
 *  ‑ Stats row (3 chips)
 *  ‑ Loot-box card
 *  ‑ Quote card
 *  ‑ Play-Now button
 */
@Composable
fun HomeSkeletonLoader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Greeting + avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ShimmerBox(modifier = Modifier.size(56.dp), shape = CircleShape)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerLine(width = 180.dp, height = 18.dp)
                ShimmerLine(width = 120.dp, height = 12.dp)
            }
        }

        // Hero illustration block
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(20.dp)
        )

        // Stats row (streak, score, played)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        // Loot-box card
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            shape = RoundedCornerShape(16.dp)
        )

        // Quote card
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(4.dp))

        // Play-Now CTA
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Browse
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Skeleton placeholder for the Browse screen while quizzes for the selected
 * category are being fetched. Shows: title block, search bar, sort row, then
 * a 2-column grid of category card placeholders.
 */
@Composable
fun BrowseSkeletonLoader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title + subtitle
        ShimmerLine(width = 160.dp, height = 24.dp)
        ShimmerLine(width = 240.dp, height = 14.dp)

        Spacer(Modifier.height(4.dp))

        // Search + sort row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            )
            ShimmerBox(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(14.dp)
            )
        }

        // 3 rows × 2 columns of category cards
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                repeat(2) {
                    ShimmerBox(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.78f),
                        shape = RoundedCornerShape(18.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Game
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Skeleton placeholder for the Game / quiz card while the active session is
 * being hydrated and quizzes are being fetched.
 *
 *  ‑ Top hero image area
 *  ‑ Header row: category label + counter chip + circular timer
 *  ‑ Question text (3 lines)
 *  ‑ 4 answer choice cards
 */
@Composable
fun GameSkeletonLoader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hero image area
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerLine(width = 120.dp, height = 16.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ShimmerBox(
                        modifier = Modifier.size(width = 56.dp, height = 28.dp),
                        shape = RoundedCornerShape(50)
                    )
                    ShimmerBox(modifier = Modifier.size(44.dp), shape = CircleShape)
                }
            }

            // Question text — three lines
            ShimmerLine(width = null, height = 18.dp)
            ShimmerLine(width = null, height = 18.dp)
            ShimmerLine(width = 200.dp, height = 18.dp)

            Spacer(Modifier.height(4.dp))

            // 4 answer choices
            repeat(4) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}
