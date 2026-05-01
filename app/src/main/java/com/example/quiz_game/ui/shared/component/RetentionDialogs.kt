package com.example.quiz_game.ui.shared.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quiz_game.R
import com.example.quiz_game.data.shop.ShopItem
import com.example.quiz_game.other.DailyRewards
import kotlinx.coroutines.delay

/**
 * One-shot popup acknowledging a freshly-credited daily-login streak.
 * Animates in with a spring scale + fade so the reward feels "earned" rather
 * than dropped on the user. The caller is responsible for keeping it shown
 * (e.g. while [com.example.quiz_game.ui.viewmodel.SharedState.pendingStreakReward]
 * is non-null) and for firing [onDismiss] which clears that flag.
 */
@Composable
fun DialogStreakReward(
    streakDays: Int,
    coinsGranted: Int,
    wasReset: Boolean,
    onDismiss: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "streakScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 240),
        label = "streakAlpha",
    )

    AlertDialog(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            scaleX = scale
            scaleY = scale
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            ButtonPrimary(onClick = onDismiss) {
                Text(stringResource(R.string.streak_reward_button))
            }
        },
        title = {
            TextBig(
                text = stringResource(R.string.streak_reward_title),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Big "Day N" hero badge
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFF7A00),
                    modifier = Modifier.size(96.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.streak_reward_day_label),
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "$streakDays",
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.streak_reward_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                // Coin chip
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFFFF3CD),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_coin),
                            contentDescription = null,
                            tint = Color(0xFFB8860B),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "+$coinsGranted",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF7A5A00),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                if (wasReset) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.streak_reward_reset_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
    )
}

/**
 * Animated reveal popup for the daily loot box. Shows a spinning "?" while
 * the user-perceived suspense plays, then swaps to the actual reward with a
 * spring pop. Dismissing fires [onDismiss]; the caller is responsible for
 * having already persisted the reward and updated `lastLootBoxClaimAt`.
 */
@Composable
fun DialogLootBoxReveal(
    reward: DailyRewards.LootBoxReward,
    onDismiss: () -> Unit,
) {
    // Two-phase reveal: ~700ms suspense → flip to reward.
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(700L)
        revealed = true
    }
    val scale by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "lootScale",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ButtonPrimary(onClick = onDismiss) {
                Text(stringResource(R.string.loot_box_button_close))
            }
        },
        title = {
            TextBig(
                text = stringResource(
                    if (revealed) R.string.loot_box_revealed_title
                    else R.string.loot_box_opening_title,
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0xFFFFE082),
                                    Color(0xFFFFB300),
                                )
                            ),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!revealed) {
                        Text(
                            text = "?",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                        )
                    } else {
                        when (reward) {
                            is DailyRewards.LootBoxReward.Nothing -> Text(
                                text = "\uD83C\uDF2B",  // 🌫
                                fontSize = 56.sp,
                            )
                            is DailyRewards.LootBoxReward.Coins -> Text(
                                text = "\uD83D\uDCB0", // 💰
                                fontSize = 56.sp,
                            )
                            is DailyRewards.LootBoxReward.PowerUp -> Text(
                                text = reward.item.icon,
                                fontSize = 56.sp,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                if (revealed) {
                    val (label, accent) = when (reward) {
                        is DailyRewards.LootBoxReward.Nothing ->
                            stringResource(R.string.loot_box_reward_nothing) to
                                MaterialTheme.colorScheme.onSurfaceVariant
                        is DailyRewards.LootBoxReward.Coins ->
                            stringResource(R.string.loot_box_reward_coins, reward.amount) to
                                Color(0xFFB8860B)
                        is DailyRewards.LootBoxReward.PowerUp ->
                            stringResource(
                                R.string.loot_box_reward_power_up,
                                stringResource(reward.item.nameRes)
                            ) to MaterialTheme.colorScheme.primary
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = accent,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.loot_box_opening_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
    )
}
