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
import com.example.quiz_game.data.Repository
import com.example.quiz_game.other.Utils
import com.example.quiz_game.ui.activity.onboard.OnboardActivity
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {

    private val TAG = "test1234 SharedViewModel"

    var state = MutableStateFlow(SharedState())
        private set

    init {
        if (Utils.hasInternet()) {
            onAction(SharedAction.PrepareTranslator)
        }
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

                is SharedAction.PrepareTranslator -> execute {
                    Repository.prepareTranslator(
                        onSuccess = {
                            println("test1234 translator: $it")
                            updateStateOnSuccess(translator = it)
                        },
                        onError = { updateStateOnError(it) }
                    )
                }

                is SharedAction.Restart -> execute {
                    val currentActivity = action.context as? Activity
                    val intent = Intent(action.context, OnboardActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    action.context.startActivity(intent)
                    currentActivity?.overridePendingTransition(0, 0)
                    currentActivity?.finish()
                }

                is SharedAction.Deeplink -> execute {
                    val intent = Intent(Intent.ACTION_VIEW, action.url.toUri())
                    action.context.startActivity(intent)
                }
            }
        }
    }

    private suspend fun execute(block: suspend () -> Unit) {
        state.value = state.value.copy(executing = true)

        delay(500L)
        block()
    }

    private fun updateStateOnSuccess(translator: Translator) {
        state.value =
            state.value.copy(translator = translator, executing = false, errors = arrayListOf())
    }

    private fun updateStateOnError(throwable: Throwable) {
        state.value = state.value.copy(
            errors = state.value.errors.apply { add(throwable) },
            executing = false
        )
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

    data class Deeplink(val context: Context, val url: String) : SharedAction
}