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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastMap
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.data.category.Category
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.Utils
import com.example.quiz_game.other.withTap
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.BannerAd
import com.example.quiz_game.ui.shared.component.BrowseSkeletonLoader
import com.example.quiz_game.ui.shared.component.DialogYesOrNo
import com.example.quiz_game.ui.shared.effect.scaleDownOnPress
import com.example.quiz_game.ui.theme.Violet500
import com.example.quiz_game.ui.theme.Violet600
import com.example.quiz_game.ui.theme.Violet700
import com.example.quiz_game.ui.viewmodel.CategoryAction
import com.example.quiz_game.ui.viewmodel.CategoryState
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.SessionAction
import com.example.quiz_game.ui.viewmodel.SessionState
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedState
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first


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
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var loading by rememberSaveable { mutableStateOf(false) }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortOrder by rememberSaveable { mutableStateOf(SortOrder.A_Z) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val dark = isSystemInDarkTheme()

    val currentSession = sessionState.session
    var pendingSessionAction by remember { mutableStateOf<PendingSessionAction?>(null) }

    val currentQuizState by rememberUpdatedState(quizState)

    LaunchedEffect(Unit) {
        quizAction(QuizAction.GetUnexpiredCounts)
    }

    LaunchedEffect(selectedCategory) {
        val category = selectedCategory ?: return@LaunchedEffect
        loading = true

        quizAction(QuizAction.GetByCategory(categoryUid = category.uid))

        // Wait for the executing flag to flip true (fetch started) then false
        // (fetch completed). A fixed timeout here was too short for network
        // fetches and caused the stale GetAll quizzes to leak through.
        val result = snapshotFlow { currentQuizState }
            .drop(1)
            .first { !it.executing }

        loading = false

        if (result.errors.isNotEmpty()) {
            onError("${context.getString(R.string.browse_screen)}: ${if (!Utils.hasInternet()) context.getString(R.string.generic_internet_loss_message) else context.getString(R.string.generic_error_message)}.")
            selectedCategory = null
            return@LaunchedEffect
        }

        val sessionQuizzes =
            result.quizzes
                .fastFilter { !it.expired }
                .take(Constants.DEFAULT_QUIZ_SESSION_AMOUNT)

        if (sessionQuizzes.isEmpty()) {
            onError(context.getString(R.string.browse_screen) + ": " + context.getString(R.string.browse_quizzes_list_empty))
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

    // Pre-calculate names in Composable context
    val categoryData = mutableListOf<Pair<Category, String>>()
    for (category in categoryState.categories) {
        val shortName = category.toShortName()
        categoryData.add(category to shortName)
    }

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
        BrowseSkeletonLoader()
    } else {
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

            // Gradient scrim
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

            @OptIn(ExperimentalFoundationApi::class)
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize()
            ) {
                // Hero spacer
                item { Spacer(modifier = Modifier.height(240.dp)) }

                // Sticky header: title + search + sort + resume banner
                stickyHeader {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                            // ── Section heading ──
                            Text(
                                text = stringResource(R.string.browse_title),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = stringResource(R.string.browse_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))

                            // ── Search + sort row ──
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                                        Icon(Icons.Default.Search, contentDescription = null)
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
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
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
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
                                            .background(
                                                brush = Brush.linearGradient(
                                                    listOf(
                                                        if (dark) Violet500 else Violet600,
                                                        if (dark) Violet700 else Violet500
                                                    )
                                                ),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { sortMenuExpanded = true }
                                            .padding(horizontal = 14.dp, vertical = 14.dp)
                                    ) {
                                        Text(
                                            text = activeSortLabel,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White,
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
                                                sortOrder =
                                                    SortOrder.COUNT_HIGH_LOW; sortMenuExpanded =
                                                false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.browse_sort_count_low)) },
                                            onClick = {
                                                sortOrder =
                                                    SortOrder.COUNT_LOW_HIGH; sortMenuExpanded =
                                                false
                                            }
                                        )
                                    }
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
                                    .background(
                                        brush = Brush.linearGradient(
                                            listOf(
                                                if (dark) Violet500 else Violet600,
                                                if (dark) Violet700 else Violet500
                                            )
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clip(RoundedCornerShape(16.dp))
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
                                        color = Color.White
                                    )
                                    Text(
                                        text = stringResource(R.string.browse_resume_button),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
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
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        for (category in rowItems) {
                            val availableCount = quizState.unexpiredCounts[category.uid] ?: 0
                            BrowseCategoryCard(
                                category = category,
                                availableCount = availableCount,
                                modifier = Modifier.weight(1f),
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
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
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

            // ── Confirmation Dialog for Active Session ──
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

            // ── Banner ad pinned to the bottom edge. The LazyColumn already
            // reserves a 100.dp bottom spacer (line ~497) so the last list
            // item never disappears under it. ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                BannerAd()
            }
        }
    }
}

// ── Full-bleed image category card matching Home.kt FeaturedCategoryCard style ──
@Composable
private fun BrowseCategoryCard(
    category: Category,
    availableCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .aspectRatio(0.78f)
            .scaleDownOnPress(.95f, interactionSource)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = withTap(onClick)
            )
    ) {
        Image(
            painter = painterResource(category.toIconRes()),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Bottom gradient scrim for label legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.78f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = category.toShortName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (availableCount > 0) {
                Text(
                    text = stringResource(R.string.browse_available_quizzes, availableCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1
                )
            } else {
                Text(
                    text = stringResource(R.string.browse_no_quizzes),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.60f),
                    maxLines = 1
                )
            }
        }
    }
}
