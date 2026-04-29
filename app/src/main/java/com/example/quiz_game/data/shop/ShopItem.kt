package com.example.quiz_game.data.shop

import androidx.annotation.StringRes
import com.example.quiz_game.R

enum class ShopItemType { SKIP, TIME_BONUS, HINT, SWAP }

data class ShopItem(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descRes: Int,
    val icon: String,          // emoji used as visual
    val price: Int,
    val type: ShopItemType,
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
        )
    )
}
