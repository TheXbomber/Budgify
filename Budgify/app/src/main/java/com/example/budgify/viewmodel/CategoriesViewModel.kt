package com.example.budgify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Category
import com.example.budgify.entities.CategoryType
import com.example.budgify.screen.CategoriesTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val selectedTab: CategoriesTab = CategoriesTab.Expenses,
    val showAddDialog: Boolean = false,
    val showCategoryActionChoiceDialog: Boolean = false,
    val showEditCategoryDialog: Boolean = false,
    val showDeleteConfirmDialog: Category? = null,
    val categoryToAction: Category? = null,
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val snackbarMessage: String? = null
)

class CategoriesViewModel(private val financeViewModel: FinanceViewModel) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                financeViewModel.allCategories,
                _uiState
            ) { allCategories, currentState ->
                val expenseCategories = allCategories.filter {
                    it.type == CategoryType.EXPENSE &&
                            it.desc != "Debts repaid" &&
                            it.desc != "Credits contracted" &&
                            it.desc != "Goals (Expense)"
                }
                val incomeCategories = allCategories.filter {
                    it.type == CategoryType.INCOME &&
                            it.desc != "Credits collected" &&
                            it.desc != "Debts contracted" &&
                            it.desc != "Goals (Income)"
                }
                currentState.copy(
                    expenseCategories = expenseCategories,
                    incomeCategories = incomeCategories
                )
            }.stateIn(viewModelScope)
                .collect {
                _uiState.value = it
            }
        }
    }

    fun onTabSelected(tab: CategoriesTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun onAddCategoryClicked() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun onDismissAddCategoryDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun onCategoryLongClicked(category: Category) {
        _uiState.value =
            _uiState.value.copy(showCategoryActionChoiceDialog = true, categoryToAction = category)
    }

    fun onDismissCategoryActionChoiceDialog() {
        _uiState.value =
            _uiState.value.copy(showCategoryActionChoiceDialog = false, categoryToAction = null)
    }

    fun onEditCategoryClicked() {
        _uiState.value = _uiState.value.copy(
            showEditCategoryDialog = true,
            showCategoryActionChoiceDialog = false
        )
    }

    fun onDismissEditCategoryDialog() {
        _uiState.value =
            _uiState.value.copy(showEditCategoryDialog = false, categoryToAction = null)
    }

    fun onDeleteCategoryClicked() {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmDialog = _uiState.value.categoryToAction,
            showCategoryActionChoiceDialog = false
        )
    }

    fun onDismissDeleteCategoryDialog() {
        _uiState.value =
            _uiState.value.copy(showDeleteConfirmDialog = null, categoryToAction = null)
    }

    fun onSnackbarMessageShown() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun addCategory(description: String, type: CategoryType) {
        viewModelScope.launch {
            val userId = financeViewModel.userId.first()
            if (userId != null) {
                val newCategory = Category(
                    userId = userId,
                    type = type,
                    desc = description
                )
                financeViewModel.addCategory(newCategory) {
                    _uiState.value = _uiState.value.copy(
                        showAddDialog = false,
                        snackbarMessage = "Category '${it.desc}' added"
                    )
                }
            }
        }
    }

    fun updateCategory(description: String) {
        viewModelScope.launch {
            _uiState.value.categoryToAction?.let {
                val updatedCategory = it.copy(desc = description)
                financeViewModel.updateCategory(updatedCategory)
                _uiState.value = _uiState.value.copy(
                    showEditCategoryDialog = false,
                    categoryToAction = null,
                    snackbarMessage = "Category '${updatedCategory.desc}' updated"
                )
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            financeViewModel.deleteCategory(category)
            _uiState.value = _uiState.value.copy(
                showDeleteConfirmDialog = null,
                categoryToAction = null,
                snackbarMessage = "Category '${category.desc}' deleted"
            )
        }
    }
}
