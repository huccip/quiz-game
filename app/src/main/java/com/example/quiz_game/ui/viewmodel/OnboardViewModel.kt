package com.example.quiz_game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.user.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class OnboardViewModel : ViewModel() {
    var state = MutableStateFlow(OnboardState())
        private set

    init {
        onAction(OnboardAction.GetUser)
    }

    fun onAction(action: OnboardAction) {
        viewModelScope.launch {
            when (action) {
                is OnboardAction.GetUser ->
                        execute {
                            val user = Repository.getUser()
                            user?.let { state.value = state.value.copy(user = it) }
                        }
                is OnboardAction.UpdateUsername ->
                        execute {
                            Repository.saveUser(state.value.user.copy(username = action.username))
                            val user = Repository.getUser()
                            user?.let { state.value = state.value.copy(user = it) }
                        }
                is OnboardAction.UpdateLanguage ->
                        execute {
                            Repository.saveUser(state.value.user.copy(language = action.language))
                            val user = Repository.getUser()
                            user?.let { state.value = state.value.copy(user = it) }
                        }
                is OnboardAction.UpdateCoins ->
                        execute {
                            Repository.saveUser(state.value.user.copy(coins = action.coins))
                            val user = Repository.getUser()
                            user?.let { state.value = state.value.copy(user = it) }
                        }
                is OnboardAction.UpdateOnboarded ->
                        execute { Repository.saveUser(state.value.user.copy(onboarded = true)) }
            }
        }
    }

    private suspend fun execute(block: suspend () -> Unit) {
        state.value = state.value.copy(executing = true, errors = emptyList())
        try {
            delay(500)
            block()
        } catch (e: Exception) {
            state.value = state.value.copy(errors = state.value.errors + e)
        } finally {
            state.value = state.value.copy(executing = false)
        }
    }
}

data class OnboardState(
        val executing: Boolean = false,
        val errors: List<Throwable> = emptyList(),
        val user: User = User(),
)

sealed interface OnboardAction {
    data object GetUser : OnboardAction
    data class UpdateUsername(val username: String) : OnboardAction
    data class UpdateLanguage(val language: String) : OnboardAction
    data class UpdateCoins(val coins: Int) : OnboardAction
    data object UpdateOnboarded : OnboardAction
}
