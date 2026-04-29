package com.example.quiz_game.ui.activity.onboard.destination

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quiz_game.R
import com.example.quiz_game.ui.activity.main.MainActivity
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.IconButton
import com.example.quiz_game.ui.shared.component.Preview
import com.example.quiz_game.ui.shared.component.ScreenHeader
import com.example.quiz_game.ui.shared.component.TextButton
import com.example.quiz_game.ui.viewmodel.OnboardAction
import com.example.quiz_game.ui.viewmodel.SharedAction

@Composable
fun Guide(
    modifier: Modifier = Modifier,
    onboardAction: (OnboardAction) -> Unit = {},
    sharedAction: (SharedAction) -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Scrollable content ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Edge-to-edge illustration ──
            Image(
                painter = painterResource(R.drawable.illustration_categories),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(24.dp))

                ScreenHeader(
                    title = stringResource(R.string.onboard_guide_title),
                    subtitle = stringResource(R.string.onboard_guide_subtitle)
                )

                Spacer(Modifier.height(28.dp))

                // ── Tip cards ──
                GuideTipCard(
                    icon = R.drawable.ic_book,
                    titleRes = R.string.onboard_guide_tip_play_title,
                    bodyRes = R.string.onboard_guide_tip_play_body
                )
                Spacer(Modifier.height(12.dp))
                GuideTipCard(
                    icon = R.drawable.ic_coin,
                    titleRes = R.string.onboard_guide_tip_coins_title,
                    bodyRes = R.string.onboard_guide_tip_coins_body
                )
                Spacer(Modifier.height(12.dp))
                GuideTipCard(
                    icon = R.drawable.ic_trophy,
                    titleRes = R.string.onboard_guide_tip_trophies_title,
                    bodyRes = R.string.onboard_guide_tip_trophies_body
                )
                Spacer(Modifier.height(12.dp))
                GuideTipCard(
                    icon = R.drawable.ic_fire,
                    titleRes = R.string.onboard_guide_tip_daily_title,
                    bodyRes = R.string.onboard_guide_tip_daily_body
                )

                Spacer(Modifier.height(24.dp))
            }
        }

        // ── Sticky bottom CTA ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            ButtonPrimary(
                onClick = {
                    onboardAction(OnboardAction.UpdateOnboarded)
                    sharedAction(
                        SharedAction.StartActivity(context, MainActivity::class.java)
                    )
                    (context as? android.app.Activity)?.finish()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(text = stringResource(R.string.onboard_guide_cta))
                IconButton(
                    painter = painterResource(R.drawable.ic_arrow_forward),
                )
            }
        }
    }
}

@Composable
private fun GuideTipCard(
    @DrawableRes icon: Int,
    @StringRes titleRes: Int,
    @StringRes bodyRes: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(bodyRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GuidePreview() {
    Preview { Guide() }
}
