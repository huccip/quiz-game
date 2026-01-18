package com.example.quiz_game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.category.Category
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "test1234 CategoryViewModel"

class CategoryViewModel() : ViewModel() {
    var state = MutableStateFlow(CategoryState())
        private set

    fun onAction(action: CategoryAction) {
        viewModelScope.launch {
            when (action) {
                is CategoryAction.GetAll -> execute {
                    Repository.categoryRepository.get(
                        onSuccess = {
                            viewModelScope.launch {
                                val categories = arrayListOf<Category>()

                                action.translator?.let { translator ->
                                    it.forEach { category ->
                                        category.name?.let { name ->
                                            val translatedName =
                                                translator.translate(name).await()
                                            categories.add(category.copy(name = translatedName))
                                        }
                                    }
                                } ?: categories.addAll(it)

                                updateStateOnSuccess(list = categories)
                            }
                        },
                        onError = { updateStateOnError(it) }
                    )
                }

                is CategoryAction.GetById -> execute {
                    Repository.categoryRepository.getById(
                        id = action.id,
                        onSuccess = { updateStateOnSuccess(data = it) },
                        onError = { updateStateOnError(it) }
                    )
                }

                is CategoryAction.GetByName -> execute {
                    Repository.categoryRepository.getByName(
                        name = action.name,
                        onSuccess = { updateStateOnSuccess(data = it) },
                        onError = { updateStateOnError(it) }
                    )
                }

                is CategoryAction.GetByUid -> execute {
                    Repository.categoryRepository.getByName(
                        name = action.uid,
                        onSuccess = { updateStateOnSuccess(data = it) },
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

    private fun updateStateOnError(e: Throwable) {
        state.value = state.value.copy(
            executing = false,
            errors = state.value.errors.apply { add(e) }
        )
    }

    private fun updateStateOnSuccess(data: Category? = null, list: List<Category>? = null) {
        state.value = state.value.copy(executing = false)
        data?.let { state.value = state.value.copy(category = data) }
        list?.let { state.value = state.value.copy(categories = list) }
    }
}

data class CategoryState(
    var executing: Boolean = false,
    var errors: ArrayList<Throwable> = arrayListOf(),
    var categories: List<Category> = emptyList(),
    var category: Category = Category(),
    var translator: Translator? = null
)

sealed interface CategoryAction {
    data class GetAll(val translator: Translator?) : CategoryAction
    data class GetByName(val name: String) : CategoryAction
    data class GetById(val id: Int) : CategoryAction
    data class GetByUid(val uid: String) : CategoryAction
}