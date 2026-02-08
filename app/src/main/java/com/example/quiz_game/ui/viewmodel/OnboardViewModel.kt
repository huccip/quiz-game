package com.example.quiz_game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_game.App
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.user.User
import kotlinx.coroutines.async
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
                is OnboardAction.GetUser -> execute {
                    try {
                        updateStateOnSuccess(Repository.getUser())
                    } catch (e: Exception) {
                        updateStateOnError(Exception("Failed to get user: ${e.message}"))
                    }
                }

                is OnboardAction.UpdateUsername -> execute {
                    try {
                        async { Repository.saveUser(state.value.user.copy(username = action.username)) }.await()
                        updateStateOnSuccess(Repository.getUser())
                    } catch (e: Exception) {
                        updateStateOnError(Exception("Failed to save username: ${e.message}"))
                    }
                }

                is OnboardAction.UpdateLanguage -> execute {
                    try {
                        async { Repository.saveUser(state.value.user.copy(language = action.language)) }.await()
                        println("test1234 user is ${Repository.getUser().toString()}")
                        updateStateOnSuccess(Repository.getUser())
                    } catch (e: Exception) {
                        updateStateOnError(Exception("Failed to update language: ${e.message}"))
                    }
                }

                is OnboardAction.UpdateCoins -> execute {
                    try {
                        async { Repository.saveUser(state.value.user.copy(coins = action.coins)) }.await()
                        updateStateOnSuccess(Repository.getUser())
                    } catch (e: Exception) {
                        updateStateOnError(Exception("Failed to update coins: ${e.message}"))
                    }
                }

                is OnboardAction.UpdateOnboarded -> execute {
                    try {
                        async { Repository.saveUser(state.value.user.copy(onboarded = true)) }.await()
                    } catch (e: Exception) {
                        updateStateOnError(Exception("Failed to update onboarded: ${e.message}"))
                    }
                }
            }
        }
    }

    private suspend fun execute(block: suspend () -> Unit) {
        state.value = state.value.copy(executing = true)

        delay(500)
        block()
    }

    private fun updateStateOnSuccess(user: User? = null) {
        state.value = state.value.copy(executing = false, errors = arrayListOf())

        user?.let { state.value = state.value.copy(user = it) }
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
    val errors: ArrayList<Throwable> = arrayListOf(),
    val user: User = User(),
)

sealed interface OnboardAction {
    data object GetUser : OnboardAction
    data class UpdateUsername(val username: String) : OnboardAction
    data class UpdateLanguage(val language: String) : OnboardAction
    data class UpdateCoins(val coins: Int) : OnboardAction
    data object UpdateOnboarded : OnboardAction
}