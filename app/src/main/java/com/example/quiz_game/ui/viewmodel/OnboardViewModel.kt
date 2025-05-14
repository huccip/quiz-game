package com.example.quiz_game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            }
        }
    }
}

data class OnboardState(
    val executing: Boolean = false,
)

sealed interface OnboardAction {
    data class Submit(val username: String) : OnboardAction
}