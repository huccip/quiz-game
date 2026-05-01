package com.example.quiz_game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.shop.ShopCatalog
import com.example.quiz_game.data.shop.ShopItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.example.quiz_game.App

class ShopViewModel : ViewModel() {

    var state = MutableStateFlow(ShopState())
        private set

    init {
        refresh()
    }

    fun onAction(action: ShopAction) {
        viewModelScope.launch {
            when (action) {
                is ShopAction.Refresh -> refresh()
                is ShopAction.BuyItem -> buyItem(action.item)
                is ShopAction.SellItem -> sellItem(action.item)
                is ShopAction.UseItem -> useItem(action.item)
                is ShopAction.GrantRandom -> grantRandom(action.count)
                is ShopAction.GrantSpecific -> grantSpecific(action.item)
                is ShopAction.ClearLastEvent -> state.value = state.value.copy(lastPurchaseResult = null)
                is ShopAction.ClearLastGranted -> state.value = state.value.copy(lastGranted = emptyList())
            }
        }
    }

    private fun refresh() {
        val user = Repository.getUser()
        val ownedMap = loadOwnedMap()
        state.value = state.value.copy(
            items = ShopCatalog.items,
            userCoins = user?.coins ?: 0,
            ownedCounts = ownedMap
        )
    }

    private suspend fun buyItem(item: ShopItem) {
        var success = false
        Repository.updateUser { user ->
            if (user.coins < item.price) {
                success = false
                return@updateUser user
            }
            success = true
            user.copy(coins = user.coins - item.price)
        }

        if (!success) {
            state.value = state.value.copy(lastPurchaseResult = PurchaseResult.NOT_ENOUGH_COINS)
            return
        }

        incrementOwned(item.id, 1)
        refresh()
        state.value = state.value.copy(lastPurchaseResult = PurchaseResult.SUCCESS)
    }

    private suspend fun sellItem(item: ShopItem) {
        val owned = App.userPrefs.getInt("shop_owned_${item.id}", 0)
        if (owned <= 0) {
            state.value = state.value.copy(lastPurchaseResult = PurchaseResult.NONE_OWNED)
            return
        }
        Repository.updateUser { user ->
            user.copy(coins = user.coins + item.sellPrice)
        }
        incrementOwned(item.id, -1)
        refresh()
        state.value = state.value.copy(lastPurchaseResult = PurchaseResult.SOLD)
    }

    private fun useItem(item: ShopItem) {
        val owned = App.userPrefs.getInt("shop_owned_${item.id}", 0)
        if (owned <= 0) return
        incrementOwned(item.id, -1)
        refresh()
    }

    private fun grantRandom(count: Int) {
        val pool = ShopCatalog.items
        if (pool.isEmpty() || count <= 0) return
        val granted = (1..count).map { pool.random() }
        granted.forEach { incrementOwned(it.id, 1) }
        refresh()
        state.value = state.value.copy(lastGranted = granted)
    }

    /**
     * Grant a single, specific [ShopItem] to the user's inventory. Used by
     * deterministic-reward flows (e.g. the daily loot box's power-up tier)
     * where the random roll has already happened upstream.
     */
    private fun grantSpecific(item: ShopItem) {
        incrementOwned(item.id, 1)
        refresh()
        state.value = state.value.copy(lastGranted = listOf(item))
    }

    private fun incrementOwned(id: String, delta: Int) {
        val current = App.userPrefs.getInt("shop_owned_$id", 0)
        val next = (current + delta).coerceAtLeast(0)
        App.userPrefs.edit {
            putInt("shop_owned_$id", next)
            commit()
        }
    }

    private fun loadOwnedMap(): Map<String, Int> {
        return ShopCatalog.items.associate { item ->
            item.id to App.userPrefs.getInt("shop_owned_${item.id}", 0)
        }
    }
}

data class ShopState(
    val items: List<ShopItem> = emptyList(),
    val userCoins: Int = 0,
    val ownedCounts: Map<String, Int> = emptyMap(),
    val lastPurchaseResult: PurchaseResult? = null,
    val lastGranted: List<ShopItem> = emptyList()
)

enum class PurchaseResult { SUCCESS, NOT_ENOUGH_COINS, SOLD, NONE_OWNED }

sealed interface ShopAction {
    data object Refresh : ShopAction
    data class BuyItem(val item: ShopItem) : ShopAction
    data class SellItem(val item: ShopItem) : ShopAction
    data class UseItem(val item: ShopItem) : ShopAction
    data class GrantRandom(val count: Int) : ShopAction
    data class GrantSpecific(val item: ShopItem) : ShopAction
    data object ClearLastEvent : ShopAction
    data object ClearLastGranted : ShopAction
}
