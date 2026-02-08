package com.example.quiz_game.ui.activity.onboard.destination

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.edit
import com.example.quiz_game.App
import com.example.quiz_game.R
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.Utils
import com.example.quiz_game.ui.shared.component.DialogYesOrNo
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

    var showRestartWarning by rememberSaveable { mutableStateOf(false) }
    var selectedLanguage: String? by rememberSaveable { mutableStateOf(null) }

    val onLanguageSelect: (String) -> Unit = { language ->
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
        }

        Utils.updateAppLocale(language)
        sharedAction(SharedAction.Restart(context))
    }

    if (showRestartWarning) {
        DialogYesOrNo(
            title = R.string.language_restart_dialog_title,
            text = R.string.language_restart_dialog_message,
            onDismiss = { showRestartWarning = false },
            onConfirm = {
                showRestartWarning = false
                selectedLanguage?.let { onLanguageSelect(it) } ?: Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            },
            buttonDismissText = R.string.language_restart_dialog_button_negative,
            buttonConfirmText = R.string.language_restart_dialog_button_positive,
        )
    }

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
                            painter = painterResource(R.drawable.ic_pin),
                        )
                    }
                },
                modifier = Modifier.clickable(
                    enabled = App.userPrefs.getString(
                        "selectedLanguage",
                        "selectedLanguage"
                    ) != language,
                    onClick = {
                        showRestartWarning = true
                        selectedLanguage = language
                    }
                )
            )
        }
    }

}