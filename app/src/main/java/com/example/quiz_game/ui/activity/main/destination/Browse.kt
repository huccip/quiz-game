package com.example.quiz_game.ui.activity.main.destination

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.quiz_game.ui.viewmodel.CategoryState
import com.example.quiz_game.ui.viewmodel.SharedState

@Composable
fun Browse(
    modifier: Modifier = Modifier,
    categoryState: CategoryState = CategoryState(),
    sharedState: SharedState = SharedState()
) {
    LazyColumn {
        items(items = categoryState.categories, key = { it.uid }) { category ->
            ListItem(
                headlineContent = {
                    Row {
                        Text(
                            "#${category.id} ",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            category.name ?: "Undefined"
                        )
                    }
                }
            )
        }
    }
}