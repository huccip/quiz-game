package com.example.quiz_game.ui.activity.onboard.destination

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import com.example.quiz_game.App
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.Utils
import com.example.quiz_game.ui.shared.component.IconButton
import com.example.quiz_game.ui.shared.component.TextBerySmol
import com.example.quiz_game.ui.viewmodel.OnboardAction
import com.example.quiz_game.ui.viewmodel.SharedAction
import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.Locale

private const val TAG = "test1234 Language"

@Composable
fun Language(
    modifier: Modifier = Modifier,
    onboardAction: (OnboardAction) -> Unit = {},
    sharedAction: (SharedAction) -> Unit = {},
) {

    val context = LocalContext.current

    LazyColumn {
        items(items = Constants.SUPPORTED_LANGUAGES, key = { it.hashCode() }) {
            val (language, country, countryCode) = it
            ListItem(
                leadingContent = {
                    IconButton(
                        model = "https://flagsapi.com/$countryCode/shiny/64.png"
                    )
                },
                headlineContent = {
                    Column {
                        Text(country)
                        TextBerySmol(text = Locale(language, countryCode).displayLanguage)
                        HorizontalDivider()
                    }
                },
                trailingContent = {
                    if (App.userPrefs.getString(
                            "selectedLanguage",
                            "selectedLanguage"
                        ) == language
                    ) {
                        IconButton(
                            imageVector = Icons.Default.Done,
                            color = Color.Green
                        )
                    }
                },
                modifier = Modifier.clickable(
                    enabled = true,
                    onClick = {
                        App.userPrefs.edit {
                            putString(
                                "lastKnownLanguage",
                                App.userPrefs.getString(
                                    "selectedLanguage",
                                    TranslateLanguage.ENGLISH
                                )
                            )
                            putString("selectedLanguage", language)

                            commit()

                            Utils.updateAppLocale(language)
                            sharedAction(SharedAction.Restart(context))
                        }
                    }
                )
            )
        }
    }

}