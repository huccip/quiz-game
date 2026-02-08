package com.example.quiz_game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_game.App
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.quote.Quote
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class QuoteViewModel : ViewModel() {
    private val TAG = "test1234 QuoteViewModel"
    var state = MutableStateFlow(QuoteState())
        private set

    fun onAction(action: QuoteAction) {
        viewModelScope.launch {
            when (action) {
                is QuoteAction.GetQuote -> execute {
                    Repository.quoteRepository.get(
                        onSuccess = { quote ->
                            App.ioScope.launch {
                                val translatedQuote =
                                    action.translator?.translate(quote.quote!!)?.await()
                                        ?: quote.quote!!
                                updateStateOnSuccess(data = quote.copy(quote = translatedQuote))
                            }
                        },
                        onError = { updateStateOnError(it) }
                    )
                }
            }
        }
    }

    private suspend fun execute(block: suspend () -> Unit) {
        state.value = state.value.copy(executing = true)
        block()
    }

    private fun updateStateOnSuccess(data: Quote? = null) {
        state.value = state.value.copy(executing = false, errors = arrayListOf())
        data?.let { state.value = state.value.copy(quote = it) }
    }

    private fun updateStateOnError(throwable: Throwable) {
        state.value = state.value.copy(
            errors = state.value.errors.apply { add(throwable) },
            executing = false
        )
    }
}

data class QuoteState(
    val executing: Boolean = false,
    val errors: ArrayList<Throwable> = arrayListOf(),
    val quote: Quote? = null
)

sealed interface QuoteAction {
    data class GetQuote(val translator: Translator? = null) : QuoteAction
}