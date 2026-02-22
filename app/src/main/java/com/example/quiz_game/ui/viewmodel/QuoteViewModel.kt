package com.example.quiz_game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.quote.Quote
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class QuoteViewModel : ViewModel() {
    var state = MutableStateFlow(QuoteState())
        private set

    fun onAction(action: QuoteAction) {
        viewModelScope.launch {
            when (action) {
                is QuoteAction.GetQuote ->
                        execute {
                            val quote = Repository.quoteRepository.get()
                            val translatedQuote =
                                    action.translator?.let { it.translate(quote.quote!!).await() }
                                            ?: quote.quote!!
                            state.value =
                                    state.value.copy(quote = quote.copy(quote = translatedQuote))
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

data class QuoteState(
        val executing: Boolean = false,
        val errors: List<Throwable> = emptyList(),
        val quote: Quote? = null
)

sealed interface QuoteAction {
    data class GetQuote(val translator: Translator? = null) : QuoteAction
}
