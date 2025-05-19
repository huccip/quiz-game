package com.example.quiz_game.ui.viewmodel

import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_game.App
import com.example.quiz_game.data.user.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class OnboardViewModel : ViewModel() {
    var state = MutableStateFlow(OnboardState())
        private set

    fun onAction(action: OnboardAction) {
        viewModelScope.launch {
            when (action) {
                is OnboardAction.Submit -> execute {
                    App.userPrefs.edit {
                        putString(User.KEY_USERNAME, action.username)
                        commit()

                        updateStateOnSuccess()
                        return@execute
                    }

                    updateStateOnError(Exception("username was not written in shared prefs"))
                }
            }
        }
    }

    private suspend fun execute(block: suspend () -> Unit) {
        state.value.copy(executing = true)

        delay(500)
        block()
    }

    private fun updateStateOnSuccess() {
        state.value = state.value.copy(executing = false, errors = arrayListOf())
    }

    private fun updateStateOnError(e: Throwable) {
        state.value = state.value.copy(
            executing = false,
            errors = state.value.errors.apply { add(e) }
        )
    }
}

data class OnboardState(
    val executing: Boolean = false,
    val errors: ArrayList<Throwable> = arrayListOf()
)

sealed interface OnboardAction {
    data class Submit(val username: String) : OnboardAction
}