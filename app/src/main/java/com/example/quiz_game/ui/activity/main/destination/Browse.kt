package com.example.quiz_game.ui.activity.main.destination

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.other.scaleDownOnPress
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.viewmodel.CategoryAction
import com.example.quiz_game.ui.viewmodel.CategoryState
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedState

@Composable
fun Browse(
    modifier: Modifier = Modifier,
    categoryState: CategoryState = CategoryState(),
    sharedState: SharedState = SharedState(),
    categoryAction: (CategoryAction) -> Unit = {},
    quizAction: (QuizAction) -> Unit = {},
    sharedAction: (SharedAction) -> Unit = {},
    navController: NavController = rememberNavController()
) {
    val interactionSource = remember { MutableInteractionSource() }

    LazyColumn {
        items(items = categoryState.categories, key = { it.uid }) { category ->
            ListItem(
                headlineContent = {
                    Column {
                        Text(
                            category.name ?: "Undefined"
                        )
                        HorizontalDivider()
                    }
                },
                modifier = modifier
                    .fillMaxWidth()
                    .scaleDownOnPress(
                        interactionSource = interactionSource,
                        scaleRatio = .3f,
                    )
                    .clickable(
                        enabled = true,
                        onClick = {
                            category.name?.let {
                                quizAction(QuizAction.GetByCategory(it))
                                sharedAction(SharedAction.Navigate(MainDestination.Game(it), navController))
                            }
                        }
                    )
            )
        }
    }
}