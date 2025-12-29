package com.example.budgify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Account
import com.example.budgify.entities.Category
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.TransactionType
import com.example.budgify.entities.TransactionWithDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class TransactionsUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate? = null,
    val transactionsForSelectedDate: List<TransactionWithDetails> = emptyList(),
    val transactionDatesForCurrentMonth: Set<LocalDate> = emptySet(),
    val transactionToAction: MyTransaction? = null,
    val showTransactionActionChoiceDialog: Boolean = false,
    val showEditTransactionDialog: Boolean = false,
    val showDeleteTransactionConfirmationDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val showLocationPickerDialog: Boolean = false,

    // State for the Edit Dialog
    val editDialogState: EditTransactionDialogState = EditTransactionDialogState()
)

data class EditTransactionDialogState(
    val transaction: MyTransaction? = null,
    val description: String = "",
    val amount: String = "",
    val selectedCategoryId: Int? = null,
    val selectedAccountId: Int? = null,
    val selectedDate: LocalDate? = null,
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isOriginalCategoryDefault: Boolean = false,
    val availableCategories: List<Category> = emptyList(),
    val availableAccounts: List<Account> = emptyList(),
    val errorMessage: String? = null
)

class TransactionsViewModel(private val financeViewModel: FinanceViewModel) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                financeViewModel.allTransactionsWithDetails,
                _uiState,
                financeViewModel.categoriesForTransactionDialog,
                financeViewModel.allAccounts
            ) { allTransactions, currentState, categories, accounts ->
                val transactionsForSelectedDate = if (currentState.selectedDate != null) {
                    allTransactions.filter { it.transaction.date == currentState.selectedDate }
                } else {
                    allTransactions.takeLast(5).reversed()
                }

                val transactionDatesForCurrentMonth = allTransactions
                    .filter { transactionWithDetails ->
                        val transactionDate = transactionWithDetails.transaction.date
                        transactionDate.year == currentState.currentMonth.year && transactionDate.month == currentState.currentMonth.month
                    }
                    .map { it.transaction.date }
                    .distinct()
                    .toSet()

                currentState.copy(
                    transactionsForSelectedDate = transactionsForSelectedDate,
                    transactionDatesForCurrentMonth = transactionDatesForCurrentMonth,
                    editDialogState = currentState.editDialogState.copy(
                        availableCategories = categories,
                        availableAccounts = accounts
                    )
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _uiState.value)
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onMonthChanged(newMonth: YearMonth) {
        _uiState.update { it.copy(currentMonth = newMonth) }
    }

    fun onTransactionLongClicked(transaction: MyTransaction) {
        _uiState.update {
            it.copy(
                transactionToAction = transaction,
                showTransactionActionChoiceDialog = true
            )
        }
    }

    fun onDismissActionChoiceDialog() {
        _uiState.update { it.copy(showTransactionActionChoiceDialog = false, transactionToAction = null) }
    }

    fun onEditTransactionClicked() {
        val transaction = _uiState.value.transactionToAction ?: return
        viewModelScope.launch {
            val isDefault = financeViewModel.isDefaultCategory(transaction.categoryId ?: -1)
            _uiState.update {
                it.copy(
                    showTransactionActionChoiceDialog = false,
                    showEditTransactionDialog = true,
                    editDialogState = EditTransactionDialogState(
                        transaction = transaction,
                        description = transaction.description,
                        amount = transaction.amount.toString().replace('.', ','),
                        selectedCategoryId = transaction.categoryId,
                        selectedAccountId = transaction.accountId,
                        selectedDate = transaction.date,
                        selectedType = transaction.type,
                        latitude = transaction.latitude,
                        longitude = transaction.longitude,
                        isOriginalCategoryDefault = isDefault,
                        availableCategories = it.editDialogState.availableCategories,
                        availableAccounts = it.editDialogState.availableAccounts
                    )
                )
            }
        }
    }

    fun onDismissEditTransactionDialog() {
        _uiState.update {
            it.copy(
                showEditTransactionDialog = false,
                transactionToAction = null,
                editDialogState = EditTransactionDialogState() // Reset dialog state
            )
        }
    }

    fun onDeleteTransactionClicked() {
        _uiState.update {
            it.copy(
                showTransactionActionChoiceDialog = false,
                showDeleteTransactionConfirmationDialog = true
            )
        }
    }

    fun onDismissDeleteConfirmationDialog() {
        _uiState.update { it.copy(showDeleteTransactionConfirmationDialog = false) }
    }

    fun onConfirmDeleteTransaction() {
        val transactionToDelete = _uiState.value.transactionToAction ?: return
        viewModelScope.launch {
            financeViewModel.deleteTransaction(transactionToDelete)
            _uiState.update {
                it.copy(
                    showDeleteTransactionConfirmationDialog = false,
                    transactionToAction = null,
                    snackbarMessage = "Transaction deleted"
                )
            }
        }
    }

    fun onSnackbarMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    // Edit Dialog specific actions
    fun onEditDialogDescriptionChange(newDescription: String) {
        _uiState.update {
            val cleanedValue = newDescription.replace("\n", "").replace("\t", "").replace(Regex("\\s+"), " ")
            it.copy(editDialogState = it.editDialogState.copy(description = cleanedValue.take(30)))
        }
    }

    fun onEditDialogAmountChange(newAmount: String) {
        _uiState.update { it.copy(editDialogState = it.editDialogState.copy(amount = newAmount)) }
    }

    fun onEditDialogCategoryChange(newCategoryId: Int?) {
        _uiState.update { it.copy(editDialogState = it.editDialogState.copy(selectedCategoryId = newCategoryId)) }
    }

    fun onEditDialogAccountChange(newAccountId: Int?) {
        _uiState.update { it.copy(editDialogState = it.editDialogState.copy(selectedAccountId = newAccountId)) }
    }

    fun onEditDialogDateChange(newDate: LocalDate?) {
        _uiState.update { it.copy(editDialogState = it.editDialogState.copy(selectedDate = newDate)) }
    }

    fun onEditDialogTypeChange(newType: TransactionType) {
        _uiState.update { it.copy(editDialogState = it.editDialogState.copy(selectedType = newType)) }
    }

    fun onEditDialogLocationChange(lat: Double?, lng: Double?) {
        _uiState.update {
             it.copy(
                 showLocationPickerDialog = false,
                 editDialogState = it.editDialogState.copy(latitude = lat, longitude = lng)
             )
        }
    }

    fun onShowLocationPicker() {
        _uiState.update { it.copy(showLocationPickerDialog = true) }
    }

    fun onDismissLocationPicker() {
        _uiState.update { it.copy(showLocationPickerDialog = false) }
    }

    fun onSaveChangesClicked() {
        val dialogState = _uiState.value.editDialogState
        val originalTransaction = dialogState.transaction ?: return

        val description = dialogState.description.trim()
        val amountDouble = dialogState.amount.replace(',', '.').toDoubleOrNull()

        if (description.isBlank() || description.length > 30) {
            _uiState.update { it.copy(editDialogState = it.editDialogState.copy(errorMessage = "Description must be between 1 and 30 characters.")) }
            return
        }
        if (amountDouble == null || amountDouble <= 0) {
            _uiState.update { it.copy(editDialogState = it.editDialogState.copy(errorMessage = "Please enter a valid positive amount.")) }
            return
        }
        if (dialogState.selectedAccountId == null) {
            _uiState.update { it.copy(editDialogState = it.editDialogState.copy(errorMessage = "Please select an account.")) }
            return
        }
        if (dialogState.selectedDate == null) {
            _uiState.update { it.copy(editDialogState = it.editDialogState.copy(errorMessage = "Please select a date.")) }
            return
        }

        val updatedTransaction = originalTransaction.copy(
            accountId = dialogState.selectedAccountId,
            type = dialogState.selectedType,
            date = dialogState.selectedDate,
            description = description,
            amount = amountDouble,
            categoryId = if (dialogState.isOriginalCategoryDefault) originalTransaction.categoryId else dialogState.selectedCategoryId,
            latitude = dialogState.latitude,
            longitude = dialogState.longitude
        )

        viewModelScope.launch {
            financeViewModel.updateTransaction(updatedTransaction)
            _uiState.update {
                it.copy(
                    showEditTransactionDialog = false,
                    transactionToAction = null,
                    snackbarMessage = "Transaction updated",
                    editDialogState = EditTransactionDialogState()
                )
            }
        }
    }
}
