package com.example.quiz_game.ui.viewmodel

import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_game.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class OnboardViewModel : ViewModel() {
    var state = MutableStateFlow(OnboardState())
        private set

    fun onAction(action: OnboardAction) {
        viewModelScope.launch {
            when (action) {
                is OnboardAction.Submit -> {
                    state.value = state.value.copy(executing = true)
                }

                OnboardAction.Done -> {
                    state.value = state.value.copy(executing = true)

                    App.userPrefs.edit {
                        putBoolean("guided", true)

                        commit()

                        state.value = state.value.copy(executing = false)
                    }
                }
            }
        }
    }
}

data class OnboardState(
    val executing: Boolean = false,
)

sealed interface OnboardAction {
    data class Submit(val username: String) : OnboardAction

    data object Done : OnboardAction
}