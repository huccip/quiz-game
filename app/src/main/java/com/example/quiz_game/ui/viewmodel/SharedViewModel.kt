package com.example.quiz_game.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.quiz_game.AppDestination
import com.example.quiz_game.other.TranslatorManager
import com.example.quiz_game.ui.activity.onboard.OnboardActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    var state = MutableStateFlow(SharedState())
        private set

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
                    action.context.startActivity(Intent(action.context, action.activity))
                    (action.context as ComponentActivity).finish()
                }
                is SharedAction.PrepareTranslator ->
                        execute {
                            // Delegate to TranslatorManager (which manages status updates)
                            TranslatorManager.prepare(action.language)

                            // Restart to apply the correct strings.xml
                            val currentActivity = action.context as? Activity
                            val intent = Intent(action.context, OnboardActivity::class.java)
                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                            )
                            action.context.startActivity(intent)
                            currentActivity?.overridePendingTransition(0, 0)
                            currentActivity?.finishAffinity()
                        }
                is SharedAction.Restart ->
                        execute {
                            val currentActivity = action.context as? Activity
                            val intent = Intent(action.context, OnboardActivity::class.java)
                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                            )
                            action.context.startActivity(intent)
                            currentActivity?.overridePendingTransition(0, 0)
                            currentActivity?.finishAffinity()
                        }
                is SharedAction.Deeplink ->
                        execute {
                            val intent = Intent(Intent.ACTION_VIEW, action.url.toUri())
                            action.context.startActivity(intent)
                        }
            }
        }
    }

    private suspend fun execute(block: suspend () -> Unit) {
        state.value = state.value.copy(executing = true, errors = emptyList())
        try {
            block()
        } catch (e: Exception) {
            state.value = state.value.copy(errors = state.value.errors + e)
        } finally {
            state.value = state.value.copy(executing = false)
        }
    }
}

data class SharedState(
        val executing: Boolean = false,
        val errors: List<Throwable> = emptyList(),
)

sealed interface SharedAction {
    data class Navigate<T : AppDestination>(val destination: T, val navController: NavController) :
            SharedAction

    data class NavigateBack(val navController: NavController) : SharedAction
    data class StartActivity<T : ComponentActivity>(val context: Context, val activity: Class<T>) :
            SharedAction

    data class PrepareTranslator(val language: String, val context: Context) : SharedAction
    data class Restart(val context: Context) : SharedAction

    data class Deeplink(val context: Context, val url: String) : SharedAction
}
