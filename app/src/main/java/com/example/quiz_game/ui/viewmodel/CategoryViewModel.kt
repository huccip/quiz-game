package com.example.quiz_game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.category.Category
import com.example.quiz_game.other.TranslatorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CategoryViewModel : ViewModel() {
    var state = MutableStateFlow(CategoryState())
        private set

    init {
        // Fetch categories immediately (untranslated)
        onAction(CategoryAction.GetAll)

        // Auto-translate when translator becomes available
        viewModelScope.launch {
            TranslatorManager.translator.collect { translator ->
                if (translator != null && state.value.categories.isNotEmpty()) {
                    val translated =
                            state.value.categories.mapNotNull { category ->
                                category.name?.let { name ->
                                    val translatedName = translator.translate(name).await()
                                    category.copy(name = translatedName)
                                }
                            }
                    state.value = state.value.copy(categories = translated)
                }
            }
        }
    }

    fun onAction(action: CategoryAction) {
        viewModelScope.launch {
            when (action) {
                is CategoryAction.GetAll ->
                        execute {
                            val fetched = Repository.categoryRepository.get()
                            state.value = state.value.copy(categories = fetched)

                            // If translator is already available, translate immediately
                            TranslatorManager.translator.value?.let { translator ->
                                val translated =
                                        fetched.mapNotNull { category ->
                                            category.name?.let { name ->
                                                val translatedName =
                                                        translator.translate(name).await()
                                                category.copy(name = translatedName)
                                            }
                                        }
                                state.value = state.value.copy(categories = translated)
                            }
                        }
                is CategoryAction.GetById ->
                        execute {
                            val category = Repository.categoryRepository.getById(action.id)
                            state.value = state.value.copy(category = category)
                        }
                is CategoryAction.GetByName ->
                        execute {
                            val category = Repository.categoryRepository.getByName(action.name)
                            category?.let { state.value = state.value.copy(category = it) }
                        }
                is CategoryAction.GetByUid ->
                        execute {
                            val category = Repository.categoryRepository.getByUid(action.uid)
                            state.value = state.value.copy(category = category)
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

data class CategoryState(
        val executing: Boolean = false,
        val errors: List<Throwable> = emptyList(),
        val categories: List<Category> = emptyList(),
        val category: Category = Category(),
)

sealed interface CategoryAction {
    data object GetAll : CategoryAction
    data class GetByName(val name: String) : CategoryAction
    data class GetById(val id: Int) : CategoryAction
    data class GetByUid(val uid: String) : CategoryAction
}
