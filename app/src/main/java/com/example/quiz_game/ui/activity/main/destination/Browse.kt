package com.example.quiz_game.ui.activity.main.destination

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastMap
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.data.category.Category
import com.example.quiz_game.other.Constants
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.LoadingFullScreenLowOpacityWithInfiniteSpinner
import com.example.quiz_game.ui.shared.component.DialogYesOrNo
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
    DEFAULT, A_Z, Z_A
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
    val interactionSource = remember { MutableInteractionSource() }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var loading by rememberSaveable { mutableStateOf(false) }
    var fetchStarted by remember { mutableStateOf(false) }
    
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortOrder by rememberSaveable { mutableStateOf(SortOrder.DEFAULT) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()

    val currentSession = sessionState.session
    var pendingSessionAction by remember { mutableStateOf<PendingSessionAction?>(null) }

    // Fetch unused quiz counts
    LaunchedEffect(Unit) {
        quizAction(QuizAction.GetUnexpiredCounts)
    }

    // Trigger fetch when category is selected
    LaunchedEffect(selectedCategory) {
        if (selectedCategory == null) return@LaunchedEffect
        loading = true
        fetchStarted = false
        quizAction(QuizAction.GetByCategory(categoryUid = selectedCategory!!.uid))
    }

    // Track when the VM actually starts executing
    LaunchedEffect(quizState.executing) {
        if (quizState.executing) {
            fetchStarted = true
        }
    }

    // React to fetch completion
    LaunchedEffect(selectedCategory, quizState.executing, fetchStarted) {
        if (selectedCategory == null) return@LaunchedEffect
        if (!fetchStarted) return@LaunchedEffect
        if (quizState.executing) return@LaunchedEffect

        loading = false
        if (quizState.errors.isNotEmpty()) {
            onError("Failed to load quizzes: ${quizState.errors.first().message}")
            selectedCategory = null
            return@LaunchedEffect
        }

        val sessionQuizzes =
            quizState.quizzes
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

    if (loading) {
        LoadingFullScreenLowOpacityWithInfiniteSpinner()
    } else {
        val scrollState = rememberScrollState()

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Parallax Hero Image using fallback illustration
            Image(
                painter = painterResource(if (isDarkTheme) R.drawable.img_illustration_home else R.drawable.img_illustration_home_light), // Placeholder hero!
                contentDescription = "Browse Vault",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .graphicsLayer {
                        val scrollValue = scrollState.value.toFloat()
                        translationY = -scrollValue * 0.5f
                        val maxScroll = 600f
                        val scrollRatio = (scrollValue / maxScroll).coerceIn(0f, 1f)
                        alpha = 1f - (scrollRatio * 0.5f)
                    }
            )

            // Foreground content layered over hero
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(240.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(bottom = 100.dp) // Scroll padding
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
                            placeholder = { Text(stringResource(R.string.browse_search_placeholder)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                        
                        Box {
                            val activeSortLabel = when(sortOrder) {
                                SortOrder.DEFAULT -> stringResource(R.string.browse_sort_default)
                                SortOrder.A_Z -> stringResource(R.string.browse_sort_a_z)
                                SortOrder.Z_A -> stringResource(R.string.browse_sort_z_a)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .scaleDownOnPress()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { sortMenuExpanded = true }
                                    .padding(horizontal = 16.dp, vertical = 16.dp) // Adjusted height to match Textfield approximately
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
                                    text = { Text(stringResource(R.string.browse_sort_default)) },
                                    onClick = { sortOrder = SortOrder.DEFAULT; sortMenuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.browse_sort_a_z)) },
                                    onClick = { sortOrder = SortOrder.A_Z; sortMenuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.browse_sort_z_a)) },
                                    onClick = { sortOrder = SortOrder.Z_A; sortMenuExpanded = false }
                                )
                            }
                        }
                    }

                    // Pre-calculate names in Composable context
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
                                SortOrder.DEFAULT -> list
                                SortOrder.A_Z -> list.sortedBy { it.second }
                                SortOrder.Z_A -> list.sortedByDescending { it.second }
                            }
                        }.map { it.first }

                    // Chunk to rows of 2
                    val chunkedCategories = processedCategories.chunked(2)

                    Column {
                        for (rowItems in chunkedCategories) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                for (category in rowItems) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        val availableCount = quizState.unexpiredCounts[category.uid] ?: 0
                                        BrowseCategoryCard(
                                            category = category,
                                            availableCount = availableCount,
                                            onClick = {
                                                if (currentSession != null) {
                                                    pendingSessionAction = PendingSessionAction.StartCategory(category)
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
                    }
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
                            is PendingSessionAction.StartCategory -> selectedCategory = action.category
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
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scaleDownOnPress(interactionSource = interactionSource)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(category.toIconRes()),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = category.toShortName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = stringResource(R.string.browse_available_quizzes, availableCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
