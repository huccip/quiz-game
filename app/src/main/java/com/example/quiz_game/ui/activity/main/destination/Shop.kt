package com.example.quiz_game.ui.activity.main.destination

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.data.shop.ShopItem
import com.example.quiz_game.other.AdManager.showRewardedAd
import com.example.quiz_game.other.Sound
import com.example.quiz_game.other.SoundManager
import com.example.quiz_game.other.withTap
import com.example.quiz_game.ui.shared.component.BannerAd
import com.example.quiz_game.ui.shared.effect.scaleDownOnPress
import com.example.quiz_game.ui.theme.GemCyan
import com.example.quiz_game.ui.theme.GemCyanDark
import com.example.quiz_game.ui.theme.Indigo500
import com.example.quiz_game.ui.theme.Indigo600
import com.example.quiz_game.ui.theme.Indigo700
import com.example.quiz_game.ui.viewmodel.PurchaseResult
import com.example.quiz_game.ui.viewmodel.ShopAction
import com.example.quiz_game.ui.viewmodel.ShopState
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Shop(
    modifier: Modifier = Modifier,
    shopState: ShopState = ShopState(),
    shopAction: (ShopAction) -> Unit = {},
    sharedAction: (com.example.quiz_game.ui.viewmodel.SharedAction) -> Unit = {},
    navController: NavController = rememberNavController()
) {
    LaunchedEffect(Unit) {
        shopAction(ShopAction.Refresh)
    }

    val dark = isSystemInDarkTheme()
    val coinTint = if (dark) GemCyanDark else GemCyan

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val successMsg = stringResource(R.string.shop_purchase_success)
    val failMsg = stringResource(R.string.shop_not_enough_coins)
    val soldMsg = stringResource(R.string.shop_sold_success).replace("%d", "")

    // Show snackbar on purchase result. The state field is treated as a
    // one-shot event: we play SFX + show the snackbar then immediately clear
    // it via ClearLastEvent so consecutive identical results (e.g. two buys
    // in a row) re-trigger the effect every time. Without the clear, the key
    // never changes and the effect would only fire on the first occurrence.
    LaunchedEffect(shopState.lastPurchaseResult) {
        val result = shopState.lastPurchaseResult ?: return@LaunchedEffect

        // SFX: distinct cues for buy vs sell. Failed-buy / nothing-to-sell
        // remain silent (the snackbar message is feedback enough).
        when (result) {
            PurchaseResult.SUCCESS -> SoundManager.play(Sound.SHOP_BUY)
            PurchaseResult.SOLD -> SoundManager.play(Sound.SHOP_SELL)
            else -> Unit
        }
        val msg = when (result) {
            PurchaseResult.SUCCESS -> successMsg
            PurchaseResult.NOT_ENOUGH_COINS -> failMsg
            PurchaseResult.SOLD -> soldMsg
            PurchaseResult.NONE_OWNED -> null
        }
        // Clear the one-shot event BEFORE awaiting the snackbar so subsequent
        // mutations to lastPurchaseResult always emit a fresh null → value
        // transition (which is what re-triggers this LaunchedEffect).
        shopAction(ShopAction.ClearLastEvent)

        msg?.let {
            scope.launch { snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short) }
        }
    }

    val lazyListState = rememberLazyListState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Parallax hero image ──
        Image(
            painter = painterResource(if (dark) R.drawable.img_illustration_home_dark else R.drawable.img_illustration_home_light),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .graphicsLayer {
                    val scrollValue = if (lazyListState.firstVisibleItemIndex == 0)
                        lazyListState.firstVisibleItemScrollOffset.toFloat()
                    else 600f
                    translationY = -scrollValue * 0.5f
                    val scrollRatio = (scrollValue / 600f).coerceIn(0f, 1f)
                    alpha = 1f - (scrollRatio * 0.5f)
                }
        )

        // Gradient scrim over hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .graphicsLayer {
                    val scrollValue = if (lazyListState.firstVisibleItemIndex == 0)
                        lazyListState.firstVisibleItemScrollOffset.toFloat()
                    else 600f
                    translationY = -scrollValue * 0.5f
                }
                .background(
                    Brush.verticalGradient(
                        0.55f to Color.Transparent,
                        1f to MaterialTheme.colorScheme.background
                    )
                )
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero spacer
            item { Spacer(modifier = Modifier.height(240.dp)) }

            // Header content card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 24.dp)
                        .padding(top = 28.dp, bottom = 8.dp)
                ) {
                    // ── Back button row ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.shop_title),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = stringResource(R.string.shop_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // ── Balance pill ──
                    Row(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(14.dp)
                            )
                            .drawBehind {
                                drawRoundRect(
                                    color = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                                    cornerRadius = CornerRadius(14.dp.toPx()),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_coin),
                            contentDescription = null,
                            tint = coinTint,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.shop_your_balance),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${shopState.userCoins}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(Modifier.height(22.dp))

                    val context = androidx.compose.ui.platform.LocalContext.current
                    val activity = context as? android.app.Activity
                    val isRewardedLoaded by com.example.quiz_game.other.AdManager.isRewardedLoaded.collectAsStateWithLifecycle()

                    if (isRewardedLoaded && activity != null) {
                        val interactionSource =
                            remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .scaleDownOnPress(.96f, interactionSource)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFF9800),
                                            Color(0xFFFF5722)
                                        )
                                    )
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = androidx.compose.foundation.LocalIndication.current,
                                    onClick = withTap {
                                        showRewardedAd(
                                            activity
                                        ) {
                                            com.example.quiz_game.App.ioScope.launch {
                                                val user =
                                                    com.example.quiz_game.data.Repository.getUser()
                                                if (user != null) {
                                                    com.example.quiz_game.data.Repository.updateUser {
                                                        it.copy(
                                                            coins = it.coins + 10
                                                        )
                                                    }
                                                    sharedAction(com.example.quiz_game.ui.viewmodel.SharedAction.RefreshUser)
                                                    shopAction(ShopAction.Refresh)
                                                }
                                            }
                                        }
                                    }
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_arrow_forward),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.shop_watch_ad_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = stringResource(R.string.shop_watch_ad_subtitle),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .background(
                                        Color.White.copy(alpha = 0.25f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "+10",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(R.drawable.ic_coin),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(22.dp))
                    }

                    Text(
                        text = stringResource(R.string.shop_section_items),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(14.dp))
                }
            }

            // ── Shop items ──
            items(items = shopState.items, key = { it.id }) { item ->
                val owned = shopState.ownedCounts[item.id] ?: 0
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    ShopItemCard(
                        item = item,
                        ownedCount = owned,
                        userCoins = shopState.userCoins,
                        onBuy = { shopAction(ShopAction.BuyItem(item)) },
                        onSell = { shopAction(ShopAction.SellItem(item)) },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Bottom padding
            item {
                Spacer(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }

        // ── 8. Banner Ad ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(MaterialTheme.colorScheme.background)
        ) {
            BannerAd()
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    ownedCount: Int,
    userCoins: Int,
    onBuy: () -> Unit,
    onSell: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val canAfford = userCoins >= item.price
    val canSell = ownedCount > 0
    val buyInteractionSource = remember { MutableInteractionSource() }
    val sellInteractionSource = remember { MutableInteractionSource() }
    val coinTint = if (dark) GemCyanDark else GemCyan

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                    cornerRadius = CornerRadius(18.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icon badge
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    if (dark) Indigo700.copy(alpha = 0.4f)
                    else Indigo500.copy(alpha = 0.10f),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
        }

        // Name + description
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(item.nameRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(item.descRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (ownedCount > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.shop_owned_label, ownedCount),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Price + buy button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Price chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        if (canAfford) coinTint.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                        RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_coin),
                    contentDescription = null,
                    tint = if (canAfford) coinTint else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${item.price}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (canAfford) coinTint else MaterialTheme.colorScheme.error
                )
            }

            // Buy button
            Box(
                modifier = Modifier
                    .scaleDownOnPress(.94f, buyInteractionSource)
                    .background(
                        brush = if (canAfford) Brush.linearGradient(
                            listOf(
                                if (dark) Indigo500 else Indigo600,
                                if (dark) Indigo700 else Indigo500
                            )
                        ) else Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = buyInteractionSource,
                        indication = LocalIndication.current,
                        enabled = canAfford,
                        onClick = withTap(onBuy)
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.shop_buy_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (canAfford) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            // Sell button — only visible when at least one is owned
            if (canSell) {
                Box(
                    modifier = Modifier
                        .scaleDownOnPress(.94f, sellInteractionSource)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(
                            interactionSource = sellInteractionSource,
                            indication = LocalIndication.current,
                            onClick = withTap(onSell)
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.shop_sell_button),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "+${item.sellPrice}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = coinTint
                        )
                    }
                }
            }
        }
    }
}
