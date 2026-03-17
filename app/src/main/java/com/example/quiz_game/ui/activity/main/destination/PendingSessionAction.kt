package com.example.quiz_game.ui.activity.main.destination

import com.example.quiz_game.data.category.Category

sealed interface PendingSessionAction {
    data object StartNewGame : PendingSessionAction
    data class StartCategory(val category: Category) : PendingSessionAction
    data object BrowseAll : PendingSessionAction
}
