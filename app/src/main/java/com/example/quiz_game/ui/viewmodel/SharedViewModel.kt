package com.example.quiz_game.ui.viewmodel

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.quiz_game.AppDestination
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
                        val packageManager = action.context.packageManager
                        val intent = packageManager.getLaunchIntentForPackage(action.context.packageName)
                        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        action.context.startActivity(intent)
                        if (action.context is Activity) {
                            action.context.finish()
                        }
                        Runtime.getRuntime().exit(0)
                    }, 500)
                }
            }
        }
    }
}

data class SharedState(
    val executing: Boolean = false,
    val errors: ArrayList<Throwable> = arrayListOf<Throwable>(),
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
}