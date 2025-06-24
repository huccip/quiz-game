package com.example.quiz_game.ui.activity.main.destination

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.category.Category
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.Utils
import com.example.quiz_game.ui.activity.main.MainDestination
import com.example.quiz_game.ui.shared.component.CardClickable
import com.example.quiz_game.ui.shared.component.DialogYesOrNo
import com.example.quiz_game.ui.shared.component.IconButton
import com.example.quiz_game.ui.shared.component.LayoutSectionHeadline
import com.example.quiz_game.ui.shared.component.LoadingInfiniteCircle
import com.example.quiz_game.ui.shared.component.TextBerySmol
import com.example.quiz_game.ui.shared.component.TextFancy
import com.example.quiz_game.ui.shared.component.TextRegular
import com.example.quiz_game.ui.viewmodel.CategoryAction
import com.example.quiz_game.ui.viewmodel.CategoryState
import com.example.quiz_game.ui.viewmodel.QuizAction
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.QuoteAction
import com.example.quiz_game.ui.viewmodel.QuoteState
import com.example.quiz_game.ui.viewmodel.SessionAction
import com.example.quiz_game.ui.viewmodel.SessionState
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedState

private const val TAG = "test1234 Home"

// FIXME: start/resume by category

@Composable
fun Home(
    modifier: Modifier = Modifier,
    sharedState: SharedState = SharedState(),
    quizState: QuizState = QuizState(),
    categoryState: CategoryState = CategoryState(),
    quoteState: QuoteState = QuoteState(),
    quoteAction: (QuoteAction) -> Unit = {},
    sharedAction: (SharedAction) -> Unit = {},
    sessionState: SessionState = SessionState(),
    sessionAction: (SessionAction) -> Unit = {},
    categoryAction: (CategoryAction) -> Unit = {},
    quizAction: (QuizAction) -> Unit = {},
    navController: NavController = rememberNavController()
) {
    val context = LocalContext.current

    var shouldShowStartNewGameDialog by rememberSaveable { mutableStateOf(false) }
    var confirmationDestination: MainDestination? by rememberSaveable { mutableStateOf(null) }
    var selectedCategory by remember {
        mutableStateOf<Category?>(null)
    }

    val onNavigate: (MainDestination) -> Unit = {
        sharedAction(SharedAction.Navigate(it, navController))
    }

    val onStartOverAndNavigate: (MainDestination) -> Unit = {
        sessionAction(SessionAction.EndSession(uid = sessionState.session.uid))
        sharedAction(SharedAction.Navigate(it, navController))
        shouldShowStartNewGameDialog = false
    }

    LaunchedEffect(categoryState) {
        if (categoryState.category.uid == selectedCategory?.uid) {
            quizAction(QuizAction.GetByCategory(categoryState.category.name!!))
        }
    }

    LaunchedEffect(categoryState.category, quizState.quizzes) {
        if (selectedCategory != null && categoryState.category.uid.isNotEmpty()) {
            val quizzesUids = quizState.quizzes
                .fastFilter { it.category == categoryState.category.name!! }
                .take(Constants.DEFAULT_QUIZ_SESSION_AMOUNT)
                .fastMap { it.uid }
            onNavigate(
                MainDestination.Game(
                    quizzesUids = quizzesUids,
                    categoryUid = selectedCategory!!.uid
                )
            )
        }
    }

    if (shouldShowStartNewGameDialog) {
        DialogYesOrNo(
            modifier = Modifier.fillMaxWidth(),
            title = R.string.dialog_discard_session_title,
            text = R.string.dialog_discard_session_text,
            icon = R.drawable.ic_warning,
            buttonConfirmText = R.string.dialog_discard_session_confirm_button,
            buttonDismissText = R.string.dialog_discard_session_dissmiss_button,
            onConfirm = { onStartOverAndNavigate(confirmationDestination!!) },
            onDismiss = { shouldShowStartNewGameDialog = false }
        )
    }

    Column {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .weight(.90f)
                .padding(horizontal = 16.dp)
        ) {
            GreetingSection(sharedState = sharedState)

            Spacer(Modifier.height(30.dp))

            QuoteCard(
                quoteState = quoteState,
                onAuthorClick = { author ->
                    if (!author.isNullOrBlank()) {
                        sharedAction(
                            SharedAction.Deeplink(
                                context,
                                "https://en.wikipedia.org/wiki/$author"
                            )
                        )
                    }
                }
            )

            Spacer(Modifier.height(30.dp))

            FeaturedCategoriesSection(
                activeSessionCategoryUid = sessionState.session.categoryUid,
                categories = categoryState.categories,
                onBrowseClick = {
                    if (sessionState.session.createdAt == null) {
                        onNavigate(MainDestination.Browse)
                        return@FeaturedCategoriesSection
                    }

                    confirmationDestination = MainDestination.Browse
                    shouldShowStartNewGameDialog = true
                },
                onCategoryClick = { category ->
                    if (sessionState.session.createdAt == null) { // new game
                        selectedCategory = category
                        categoryAction(CategoryAction.GetByUid(category.uid))
                    } else if (sessionState.session.expiredAt == null && sessionState.session.categoryUid == category.uid) { //resume
                        onNavigate(MainDestination.Game(quizzesUids = sessionState.session.quizzesUids!!))
                        return@FeaturedCategoriesSection
                    } else { // start over
                        val quizzesUids = quizState.quizzes
                            .fastFilter { it.category == category.name }
                            .take(Constants.DEFAULT_QUIZ_SESSION_AMOUNT)
                            .fastMap { it.uid }

                        confirmationDestination = MainDestination.Game(quizzesUids = quizzesUids)
                        shouldShowStartNewGameDialog = true
                    }
                }
            )
        }

        StartResumeButton(
            hasActiveSession = sessionState.session.createdAt != null && sessionState.session.expiredAt == null,
            onClick = {
                if (sessionState.session.createdAt == null) { // new game
                    val quizzesUids = quizState.quizzes
                        .take(Constants.DEFAULT_QUIZ_SESSION_AMOUNT)
                        .fastMap { it.uid }

                    onNavigate(MainDestination.Game(quizzesUids = quizzesUids))
                } else { // resume
                    onNavigate(MainDestination.Game(quizzesUids = sessionState.session.quizzesUids!!))
                }
            }
        )
    }
}

@Composable
private fun GreetingSection(sharedState: SharedState = SharedState()) {
    var greetingMessage by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(sharedState.translator) {
        greetingMessage = Utils.greetingBasedOnTimezone(translator = sharedState.translator)
    }
    LayoutSectionHeadline(
        title = "$greetingMessage ${Repository.getUser()?.username}",
        leadingIcon = R.drawable.ic_wave
    )
//    Spacer(Modifier.height(3.dp))
//    TextSmol(text = stringResource(R.string.home_greeting_subtitle))
}

@Composable
private fun QuoteCard(
    quoteState: QuoteState,
    onAuthorClick: (String?) -> Unit
) {
    val padding = 10.dp

    CardClickable(
        onClick = { onAuthorClick(quoteState.quote?.author) },
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (quoteState.executing) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingInfiniteCircle()
            }
            return@CardClickable
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = padding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.padding(horizontal = padding)
            ) {
                IconButton(
                    painter = painterResource(R.drawable.ic_feather),
                    tint = MaterialTheme.colorScheme.primary
                )

                TextBerySmol(
                    text = stringResource(R.string.home_quote_label),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = padding)
                )
            }

            TextRegular(
                text = if (quoteState.quote?.quote != null) "\"${quoteState.quote.quote}\"" else stringResource(
                    R.string.home_quote_not_found
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = padding),
                textAlign = TextAlign.Start
            )

            if (!quoteState.quote?.author.isNullOrBlank()) {
                SuggestionChip(
                    onClick = { onAuthorClick(quoteState.quote.author) },
                    modifier = Modifier.padding(horizontal = padding),
                    label = {
                        TextBerySmol(
                            text = quoteState.quote.author, color = contentColorFor(
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    icon = {
                        IconButton(imageVector = Icons.Default.AccountCircle)
                    },
                    border = null,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = contentColorFor(MaterialTheme.colorScheme.surface)
                    ),
                    shape = RoundedCornerShape(100)
                )
            }
        }
    }
}

@Composable
private fun FeaturedCategoriesSection(
    activeSessionCategoryUid: String? = null,
    categories: List<Category>,
    onCategoryClick: (Category) -> Unit = {},
    onBrowseClick: () -> Unit = {},
) {
    val featuredCategories = remember(categories) {
        buildList {
            activeSessionCategoryUid?.let { categoryUid -> add(categories.fastFirst { it.uid == categoryUid }) }
            addAll(categories.shuffled().fastFilter { it.uid != activeSessionCategoryUid }.take(5))
        }
    }

    LayoutSectionHeadline(
        title = stringResource(R.string.home_featured_title),
        leadingIcon = R.drawable.ic_fire,
        actionText = stringResource(R.string.home_featured_subtitle),
        actionIcon = R.drawable.ic_arrow_forward,
        onClick = onBrowseClick
    )

    Spacer(Modifier.height(10.dp))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        featuredCategories.fastForEach { category ->
            if (category.id != null) {
                FeaturedCategoryItem(
                    modifier = Modifier
                        .fillMaxWidth(),
                    category = category,
                    onClick = onCategoryClick,
                    isActive = category.uid == activeSessionCategoryUid
                )
            }
        }
    }
}

@Composable
private fun StartResumeButton(
    hasActiveSession: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
        )
    ) {
        TextFancy(
            text = stringResource(
                if (hasActiveSession) R.string.home_button_resume
                else R.string.home_button_start
            ),
            color = contentColorFor(MaterialTheme.colorScheme.primaryContainer),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FeaturedCategoryItem(
    modifier: Modifier = Modifier,
    category: Category,
    isActive: Boolean = false,
    onClick: (Category) -> Unit = {}
) {
    OutlinedCard(
        modifier = modifier,
        onClick = { onClick(category) },
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent
        ),
        border = CardDefaults.outlinedCardBorder(
            enabled = true,
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(PaddingValues(20.dp)),
        ) {
            IconButton(painter = painterResource(R.drawable.ic_fire))
            TextRegular(text = category.name ?: "Undefined", modifier = Modifier.weight(1f))
            if (isActive) IconButton(
                painter = painterResource(R.drawable.ic_live),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewHome() {
    com.example.quiz_game.ui.shared.component.Preview {
        Home()
    }
}