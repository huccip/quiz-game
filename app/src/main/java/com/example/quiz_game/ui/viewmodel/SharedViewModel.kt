package com.example.quiz_game.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.quiz_game.App
import com.example.quiz_game.AppDestination
import com.example.quiz_game.R
import com.example.quiz_game.data.Repository
import com.example.quiz_game.ui.activity.main.MainActivity
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {

    var state = MutableStateFlow(SharedState())
        private set

    init {
        onAction(SharedAction.PrepareTranslator)
    }

    fun onAction(action: SharedAction) {
        viewModelScope.launch {
            when (action) {
                is SharedAction.Navigate<*> -> {
                    delay(100L)
                    action.navController.navigate(action.destination)
                }

                is SharedAction.NavigateBack -> {
                    delay(100L)
                    action.navController.popBackStack()
                }

                is SharedAction.StartActivity<*> -> {
                    action.context.startActivity(
                        Intent(action.context, action.activity)
                    )
                }

                is SharedAction.PrepareTranslator -> {
                    state.value = state.value.copy(executing = true)

                    delay(100L)
                    Repository.prepareTranslator(
                        onSuccess = {
                            state.value = state.value.copy(
                                executing = false,
                                translator = it
                            )
                        },
                        onError = {
                            state.value = state.value.copy(
                                executing = false,
                                errors = state.value.errors.apply { add(it) }
                            )
                        }
                    )
                }

                is SharedAction.Restart -> {
                    state.value = state.value.copy(executing = true)

                    Handler(Looper.getMainLooper()).postDelayed({
                        state.value = state.value.copy(executing = false)

                        val intent = Intent(action.context, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        action.context.startActivity(intent)
                        Runtime.getRuntime().exit(0)
                    }, 500)
                }

                is SharedAction.UpdateScore -> {
                    state.value = state.value.copy(
                        executing = true
                    )

                    App.userPrefs.edit {
                        putInt("score", App.userPrefs.getInt("score", 0) + action.mark)
                        putStringSet(
                            "achievements",
                            App.userPrefs.getStringSet("achievements", mutableSetOf())?.apply {
                                if (App.userPrefs.getInt("score", 0) > action.mark) {
                                    add(action.context.getString(R.string.achievements_new_score))
                                }

                                add(
                                    when (action.incorrectlyAnswered) {
                                        0 -> action.context.getString(R.string.achievements_no_mistakes)
                                        1 -> action.context.getString(R.string.achievements_one_mistake)
                                        2 -> action.context.getString(R.string.achievements_two_mistakes)
                                        10 -> action.context.getString(R.string.achievements_ten_mistakes)
                                        30 -> action.context.getString(R.string.achievements_thirty_mistakes)
                                        50 -> action.context.getString(R.string.achievements_fifty_mistakes)
                                        else -> action.context.getString(R.string.achievements_empty)
                                    }
                                )
                            }
                        )

                        state.value = state.value.copy(executing = false)

                        commit()
                    }
                }
            }
        }
    }
}

data class SharedState(
    val executing: Boolean = false,
    val errors: ArrayList<Throwable> = arrayListOf(),
    val translator: Translator? = null
)

sealed interface SharedAction {
    data class Navigate<T : AppDestination>(val destination: T, val navController: NavController) :
        SharedAction

    data class NavigateBack(val navController: NavController) : SharedAction
    data class StartActivity<T : ComponentActivity>(val context: Context, val activity: Class<T>) :
        SharedAction

    data object PrepareTranslator : SharedAction
    data class Restart(val context: Context) : SharedAction

    data class UpdateScore(val context: Context, val mark: Int, val incorrectlyAnswered: Int) :
        SharedAction
}