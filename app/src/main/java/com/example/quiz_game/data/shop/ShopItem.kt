package com.example.quiz_game.data.shop

import androidx.annotation.StringRes
import com.example.quiz_game.R

/**
 * Power-up category. Each value drives a distinct in-quiz behaviour.
 *
 * [SCORE_MULTIPLIER] is shared by the x2 / x3 / x5 catalog entries — the exact
 * multiplier value lives on the [ShopItem.multiplier] field below so a single
 * `when` branch in Game.kt handles all three tiers.
 */
enum class ShopItemType { SKIP, TIME_BONUS, HINT, SWAP, SCORE_MULTIPLIER }

data class ShopItem(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descRes: Int,
    val icon: String,          // emoji used as visual
    val price: Int,
    val type: ShopItemType,
    /**
     * Score-multiplier value when [type] == [ShopItemType.SCORE_MULTIPLIER]
     * (e.g. 2, 3, 5). Ignored for every other type — left at 1 as a no-op
     * sentinel so reward maths can multiply unconditionally.
     */
    val multiplier: Int = 1,
    val sellPrice: Int = (price * 0.6f).toInt() // sell back for 60% of buy price
)

object ShopCatalog {
    val items: List<ShopItem> = listOf(
        ShopItem(
            id = "skip_question",
            nameRes = R.string.shop_item_skip_name,
            descRes = R.string.shop_item_skip_desc,
            icon = "\u23ED\uFE0F", // ⏭️
            price = 50,
            type = ShopItemType.SKIP
        ),
        ShopItem(
            id = "time_bonus",
            nameRes = R.string.shop_item_time_name,
            descRes = R.string.shop_item_time_desc,
            icon = "\u23F1\uFE0F", // ⏱️
            price = 30,
            type = ShopItemType.TIME_BONUS
        ),
        ShopItem(
            id = "hint_card",
            nameRes = R.string.shop_item_hint_name,
            descRes = R.string.shop_item_hint_desc,
            icon = "\uD83D\uDCA1", // 💡
            price = 40,
            type = ShopItemType.HINT
        ),
        ShopItem(
            id = "swap_question",
            nameRes = R.string.shop_item_swap_name,
            descRes = R.string.shop_item_swap_desc,
            icon = "\uD83D\uDD04", // 🔄
            price = 45,
            type = ShopItemType.SWAP
        ),
        // ── Score multipliers (risk/reward) ───────────────────────────────
        // Activated BEFORE picking an answer. If correct, the question's
        // mark is multiplied by `multiplier`. If wrong / unanswered, the
        // power-up is consumed with no benefit and no coin penalty.
        ShopItem(
            id = "multiplier_x2",
            nameRes = R.string.shop_item_mult_x2_name,
            descRes = R.string.shop_item_mult_x2_desc,
            icon = "\u2728", // ✨
            price = 60,
            type = ShopItemType.SCORE_MULTIPLIER,
            multiplier = 2
        ),
        ShopItem(
            id = "multiplier_x3",
            nameRes = R.string.shop_item_mult_x3_name,
            descRes = R.string.shop_item_mult_x3_desc,
            icon = "\uD83D\uDD25", // 🔥
            price = 110,
            type = ShopItemType.SCORE_MULTIPLIER,
            multiplier = 3
        ),
        ShopItem(
            id = "multiplier_x5",
            nameRes = R.string.shop_item_mult_x5_name,
            descRes = R.string.shop_item_mult_x5_desc,
            icon = "\uD83D\uDC8E", // 💎
            price = 220,
            type = ShopItemType.SCORE_MULTIPLIER,
            multiplier = 5
        ),
    )
}
