package com.example.quiz_game.ui.activity.main.destination

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastMap
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import com.example.quiz_game.R
import com.example.quiz_game.data.category.Category
import com.example.quiz_game.other.Constants
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.CardClickable
import com.example.quiz_game.ui.shared.component.DialogYesOrNo
import com.example.quiz_game.ui.shared.component.LoadingFullScreenLowOpacityWithInfiniteSpinner
import com.example.quiz_game.ui.shared.effect.scaleDownOnPress
import com.example.quiz_game.ui.viewmodel.CategoryAction
import com.example.quiz_game.ui.viewmodel.CategoryState
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.SessionAction
import com.example.quiz_game.ui.viewmodel.SessionState
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedState


enum class SortOrder {
    A_Z, Z_A, COUNT_HIGH_LOW, COUNT_LOW_HIGH
}

@Composable
fun Browse(
    modifier: Modifier = Modifier,
    categoryState: CategoryState = CategoryState(),
    sessionState: SessionState = SessionState(),
    sharedState: SharedState = SharedState(),
    quizState: QuizState = QuizState(),
    categoryAction: (CategoryAction) -> Unit = {},
    quizAction: (QuizAction) -> Unit = {},
    sharedAction: (SharedAction) -> Unit = {},
    sessionAction: (SessionAction) -> Unit = {},
    navController: NavController = rememberNavController(),
    onError: (String) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var loading by rememberSaveable { mutableStateOf(false) }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortOrder by rememberSaveable { mutableStateOf(SortOrder.A_Z) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()

    val currentSession = sessionState.session
    var pendingSessionAction by remember { mutableStateOf<PendingSessionAction?>(null) }

    // Keep a snapshot-observable reference to quizState so snapshotFlow can track changes
    val currentQuizState by rememberUpdatedState(quizState)

    // Fetch unused quiz counts
    LaunchedEffect(Unit) {
        quizAction(QuizAction.GetUnexpiredCounts)
    }

    // Fetch quizzes and navigate when a category is selected.
    // Uses snapshotFlow to reliably detect completion, avoiding the race condition
    // where StateFlow conflates the executing=true intermediate state when the
    // repository returns cached data within a single frame.
    LaunchedEffect(selectedCategory) {
        val category = selectedCategory ?: return@LaunchedEffect
        loading = true

        quizAction(QuizAction.GetByCategory(categoryUid = category.uid))

        // Wait for the ViewModel to finish processing our request.
        // snapshotFlow observes the rememberUpdatedState-backed State, which updates
        // on each recomposition. drop(1) skips the pre-dispatch snapshot.
        // Timeout handles the edge case where the new QuizState is content-identical
        // to the previous one (StateFlow won't emit, so no recomposition occurs).
        val result = withTimeoutOrNull(500L) {
            snapshotFlow { currentQuizState }
                .drop(1)
                .first { !it.executing }
        } ?: currentQuizState

        loading = false

        if (result.errors.isNotEmpty()) {
            onError("Failed to load quizzes: ${result.errors.first().message}")
            selectedCategory = null
            return@LaunchedEffect
        }

        val sessionQuizzes =
            result.quizzes
                .fastFilter { !it.expired }
                .take(Constants.DEFAULT_QUIZ_SESSION_AMOUNT)

        if (sessionQuizzes.isEmpty()) {
            onError("Not enough quizzes for this category. Please try another.")
            selectedCategory = null
            return@LaunchedEffect
        }

        sessionAction(
            SessionAction.InitiateSession(
                quizzesUids = sessionQuizzes.fastMap { it.uid },
                maxScore = sessionQuizzes.sumOf { it.mark ?: 0 }
            )
        )

        sharedAction(
            SharedAction.Navigate(
                MainDestination.Game(quizzesUids = sessionQuizzes.map { eachQuiz -> eachQuiz.uid }),
                navController
            )
        )
        selectedCategory = null
    }

    // Pre-calculate names in Composable context (toShortName is @Composable)
    val categoryData = mutableListOf<Pair<Category, String>>()
    for (category in categoryState.categories) {
        val shortName = category.toShortName()
        categoryData.add(category to shortName)
    }

    // Prepare Categories List
    val processedCategories = categoryData
        .filter { (category, shortName) ->
            val fullName = category.name ?: ""
            searchQuery.isEmpty() ||
                    shortName.contains(searchQuery, ignoreCase = true) ||
                    fullName.contains(searchQuery, ignoreCase = true)
        }
        .let { list ->
            when (sortOrder) {
                SortOrder.A_Z -> list.sortedBy { it.second }
                SortOrder.Z_A -> list.sortedByDescending { it.second }
                SortOrder.COUNT_HIGH_LOW -> list.sortedByDescending {
                    quizState.unexpiredCounts[it.first.uid] ?: 0
                }

                SortOrder.COUNT_LOW_HIGH -> list.sortedBy {
                    quizState.unexpiredCounts[it.first.uid] ?: 0
                }
            }
        }.map { it.first }

    val chunkedCategories = processedCategories.chunked(2)
    val hasActiveSession = currentSession != null && currentSession.expiredAt == null

    if (loading) {
        LoadingFullScreenLowOpacityWithInfiniteSpinner()
    } else {
        val lazyListState = rememberLazyListState()

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Parallax Hero Image using fallback illustration
            Image(
                painter = painterResource(R.drawable.img_illustration_home),
                contentDescription = "Browse Vault",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .graphicsLayer {
                        // Derive a pixel scroll offset from the LazyListState
                        val scrollValue = if (lazyListState.firstVisibleItemIndex == 0) {
                            lazyListState.firstVisibleItemScrollOffset.toFloat()
                        } else {
                            // Once the first item is fully scrolled past, treat as max
                            600f
                        }
                        translationY = -scrollValue * 0.5f
                        val maxScroll = 600f
                        val scrollRatio = (scrollValue / maxScroll).coerceIn(0f, 1f)
                        alpha = 1f - (scrollRatio * 0.5f)
                    }
            )

            // Foreground content with LazyColumn + stickyHeader
            @OptIn(ExperimentalFoundationApi::class)
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize()
            ) {
                // Hero visibility spacer
                item {
                    Spacer(modifier = Modifier.height(240.dp))
                }

                // Sticky header: search/sort row + resume banner
                stickyHeader {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // Search and Sort Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        stringResource(R.string.browse_search_placeholder),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            modifier = Modifier.clickable { searchQuery = "" }
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.5f
                                    ),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.5f
                                    ),
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )

                            Box {
                                val activeSortLabel = when (sortOrder) {
                                    SortOrder.A_Z -> stringResource(R.string.browse_sort_a_z)
                                    SortOrder.Z_A -> stringResource(R.string.browse_sort_z_a)
                                    SortOrder.COUNT_HIGH_LOW -> stringResource(R.string.browse_sort_count_high)
                                    SortOrder.COUNT_LOW_HIGH -> stringResource(R.string.browse_sort_count_low)
                                }

                                Box(
                                    modifier = Modifier
                                        .scaleDownOnPress()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { sortMenuExpanded = true }
                                        .padding(horizontal = 16.dp, vertical = 16.dp)
                                ) {
                                    Text(
                                        text = activeSortLabel,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                DropdownMenu(
                                    expanded = sortMenuExpanded,
                                    onDismissRequest = { sortMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.browse_sort_a_z)) },
                                        onClick = {
                                            sortOrder = SortOrder.A_Z; sortMenuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.browse_sort_z_a)) },
                                        onClick = {
                                            sortOrder = SortOrder.Z_A; sortMenuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.browse_sort_count_high)) },
                                        onClick = {
                                            sortOrder = SortOrder.COUNT_HIGH_LOW; sortMenuExpanded =
                                            false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.browse_sort_count_low)) },
                                        onClick = {
                                            sortOrder = SortOrder.COUNT_LOW_HIGH; sortMenuExpanded =
                                            false
                                        }
                                    )
                                }
                            }
                        }

                        // Resume Session Banner
                        if (hasActiveSession) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(bottom = 16.dp)
                                    .scaleDownOnPress()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable {
                                        sharedAction(
                                            SharedAction.Navigate(
                                                MainDestination.Game(
                                                    quizzesUids = currentSession.quizzesUids
                                                        ?: emptyList()
                                                ),
                                                navController
                                            )
                                        )
                                    }
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.browse_resume_title),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = stringResource(R.string.browse_resume_button),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline
                                    )
                                }
                            }
                        }
                    }
                }

                // No results message
                if (processedCategories.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 24.dp, vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.browse_no_results, searchQuery),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Category card rows
                items(chunkedCategories.size) { index ->
                    val rowItems = chunkedCategories[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (category in rowItems) {
                            Box(modifier = Modifier.weight(1f)) {
                                val availableCount =
                                    quizState.unexpiredCounts[category.uid] ?: 0
                                BrowseCategoryCard(
                                    category = category,
                                    availableCount = availableCount,
                                    onClick = {
                                        if (hasActiveSession) {
                                            pendingSessionAction =
                                                PendingSessionAction.StartCategory(category)
                                        } else {
                                            selectedCategory = category
                                        }
                                    }
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Bottom padding
                item {
                    Spacer(
                        modifier = Modifier
                            .height(100.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                    )
                }
            }

            // ── Confirmation Dialog for Active Session Interruption ──
            if (pendingSessionAction != null) {
                DialogYesOrNo(
                    modifier = Modifier.fillMaxWidth(),
                    title = R.string.dialog_discard_session_title,
                    text = R.string.dialog_discard_session_text,
                    icon = R.drawable.ic_warning,
                    buttonConfirmText = R.string.dialog_discard_session_confirm_button,
                    buttonDismissText = R.string.dialog_discard_session_dissmiss_button,
                    onConfirm = {
                        currentSession?.let { sessionAction(SessionAction.EndSession(it.uid)) }
                        when (val action = pendingSessionAction) {
                            is PendingSessionAction.StartCategory -> selectedCategory =
                                action.category

                            else -> {}
                        }
                        pendingSessionAction = null
                    },
                    onDismiss = { pendingSessionAction = null }
                )
            }
        }
    }
}

@Composable
private fun BrowseCategoryCard(category: Category, availableCount: Int, onClick: () -> Unit) {
    CardClickable(
        modifier = Modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon — full width, aspect ratio preserved, small padding
            Image(
                painter = painterResource(category.toIconRes()),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3 / 2.5f)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = category.toShortName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = if (availableCount > 0) {
                    stringResource(R.string.browse_available_quizzes, availableCount)
                } else {
                    stringResource(R.string.browse_no_quizzes)
                },
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = if (availableCount > 0) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                }
            )
        }
    }
}
