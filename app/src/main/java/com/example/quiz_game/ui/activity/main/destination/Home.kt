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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.quiz_game.ui.shared.component.TextBig
import com.example.quiz_game.ui.shared.component.TextFancy
import com.example.quiz_game.ui.shared.component.TextRegular
import com.example.quiz_game.ui.viewmodel.CategoryAction
import com.example.quiz_game.ui.viewmodel.CategoryState
import com.example.quiz_game.ui.viewmodel.QuizState
import com.example.quiz_game.ui.viewmodel.QuoteAction
import com.example.quiz_game.ui.viewmodel.QuoteState
import com.example.quiz_game.ui.viewmodel.SessionAction
import com.example.quiz_game.ui.viewmodel.SessionState
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.example.quiz_game.ui.viewmodel.SharedState

private const val TAG = "test1234 Home"

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
    navController: NavController = rememberNavController()
) {
    val context = LocalContext.current
    var confirmationTrigger by rememberSaveable { mutableStateOf(false) }
    var confirmationDestination by rememberSaveable { mutableStateOf<MainDestination?>(null) }

//    val isLoading = sharedState.executing || quizState.executing ||
//            categoryState.executing || quoteState.executing
//
//    if (isLoading) {
//        LoadingInfiniteLine(subject = stringArrayResource(R.array.home_loading_subjects))
//        return
//    }

    // Navigation functions
    val navigateToGame = {
        sessionAction(SessionAction.EndSession(sessionState.session.uid))
        val destination = MainDestination.Game(
            quizzesUids = quizState.quizzes.take(Constants.DEFAULT_QUIZ_SESSION_AMOUNT)
                .map { it.uid }
        )
        sharedAction(SharedAction.Navigate(destination, navController))
    }

    val navigateToBrowse = {
        sessionAction(SessionAction.EndSession(sessionState.session.uid))
        sharedAction(SharedAction.Navigate(MainDestination.Browse, navController))
    }

    val showConfirmationDialog = { destination: MainDestination ->
        confirmationDestination = destination
        confirmationTrigger = true
    }

    if (confirmationTrigger) {
        DialogYesOrNo(
            modifier = Modifier.fillMaxWidth(),
            title = R.string.dialog_discard_session_title,
            text = R.string.dialog_discard_session_text,
            icon = R.drawable.ic_warning,
            buttonConfirmText = R.string.dialog_discard_session_confirm_button,
            buttonDismissText = R.string.dialog_discard_session_dissmiss_button,
            onConfirm = {
                when (confirmationDestination) {
                    is MainDestination.Browse -> navigateToBrowse()
                    else -> navigateToGame()
                }
                confirmationTrigger = false
            },
            onDismiss = { confirmationTrigger = false }
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
            GreetingSection()

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
                categories = categoryState.categories,
                onBrowseClick = navigateToBrowse,
                onCategoryClick = { categoryId ->
                    categoryAction(CategoryAction.GetById(categoryId))
                }
            )
        }

        StartResumeButton(
            hasActiveSession = !sessionState.session.quizzesUids.isNullOrEmpty(),
            onClick = {
                if (sessionState.session.quizzesUids.isNullOrEmpty()) {
                    showConfirmationDialog(MainDestination.Game())
                } else {
                    navigateToGame()
                }
            }
        )
    }
}

@Composable
private fun GreetingSection() {
    LayoutSectionHeadline(
        title = "${Utils.greetingBasedOnTimezone()} ${Repository.getUser()?.username}",
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
                text = if (quoteState.quote?.author != null) "\"${quoteState.quote.quote}\"" else "No quote available today. Check back later!",
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
    categories: List<Category>,
    onCategoryClick: (Int) -> Unit = {},
    onBrowseClick: () -> Unit = {},
) {
    LayoutSectionHeadline(
        title = stringResource(R.string.home_featured_title),
        leadingIcon = R.drawable.ic_fire,
        actionText = stringResource(R.string.home_featured_subtitle),
        actionIcon = R.drawable.ic_arrow_forward
    )

    Spacer(Modifier.height(10.dp))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        categories.take(6).shuffled().forEach { category ->
            if (category.id != null) {
                FeaturedCategoryItem(
                    modifier = Modifier
                        .fillMaxWidth(),
                    category = category,
                    onClick = onCategoryClick
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
    onClick: (Int) -> Unit = {}
) {
    OutlinedCard(
        modifier = modifier,
        onClick = { onClick(category.id ?: -1) },
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
            TextRegular(text = category.name ?: "Undefined")
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