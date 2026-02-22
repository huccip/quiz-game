package com.example.quiz_game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.category.Category
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CategoryViewModel : ViewModel() {
    var state = MutableStateFlow(CategoryState())
        private set

    init {
        onAction(CategoryAction.GetAll(null))
    }

    fun onAction(action: CategoryAction) {
        viewModelScope.launch {
            when (action) {
                is CategoryAction.GetAll ->
                    execute {
                        val fetched = Repository.categoryRepository.get()
                        val categories =
                            action.translator?.let { translator ->
                                fetched.mapNotNull { category ->
                                    category.name?.let { name ->
                                        val translated = translator.translate(name).await()
                                        category.copy(name = translated)
                                    }
                                }
                            }
                                ?: fetched
                        state.value = state.value.copy(categories = categories)
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
    val translator: Translator? = null
)

sealed interface CategoryAction {
    data class GetAll(val translator: Translator?) : CategoryAction
    data class GetByName(val name: String) : CategoryAction
    data class GetById(val id: Int) : CategoryAction
    data class GetByUid(val uid: String) : CategoryAction
}
