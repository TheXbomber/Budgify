package com.example.budgify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Account
import com.example.budgify.entities.Category
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.TransactionType
import com.example.budgify.entities.TransactionWithDetails
import com.example.budgify.screen.ChartType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomepageEditTransactionDialogState(
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

data class HomepageUiState(
    val balancesVisible: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val transactions: List<TransactionWithDetails> = emptyList(),
    val snackbarMessage: String? = null,

    // Dialogs
    val accountToAction: Account? = null,
    val transactionToAction: MyTransaction? = null,
    val showAddAccountDialog: Boolean = false,
    val showAccountActionChoiceDialog: Boolean = false,
    val showEditAccountDialog: Boolean = false,
    val showDeleteAccountConfirmationDialog: Boolean = false,
    val showTransactionActionChoiceDialog: Boolean = false,
    val showEditTransactionDialog: Boolean = false,
    val showDeleteTransactionConfirmationDialog: Boolean = false,
    val showLocationPickerDialog: Boolean = false,

    // Charts
    val chartType: ChartType = ChartType.PIE,
    val chartTransactionType: TransactionType = TransactionType.EXPENSE,
    val selectedChartAccountIds: Set<Int> = emptySet(),
    val showChartDetailDialog: Boolean = false,
    val selectedAccountForDetail: Account? = null,
    val showAccountFilterDialog: Boolean = false,

    val editDialogState: HomepageEditTransactionDialogState = HomepageEditTransactionDialogState()
)

class HomepageViewModel(private val financeViewModel: FinanceViewModel) : ViewModel() {

    private val _uiState = MutableStateFlow(HomepageUiState())
    val uiState: StateFlow<HomepageUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                financeViewModel.allAccounts,
                financeViewModel.allTransactionsWithDetails,
                financeViewModel.categoriesForTransactionDialog
            ) { accounts, transactions, categories ->
                _uiState.value.copy(
                    accounts = accounts,
                    transactions = transactions,
                    selectedChartAccountIds = if (_uiState.value.selectedChartAccountIds.isEmpty()) accounts.map { it.id }.toSet() else _uiState.value.selectedChartAccountIds,
                    editDialogState = _uiState.value.editDialogState.copy(
                        availableCategories = categories,
                        availableAccounts = accounts
                    )
                )
            }.stateIn(viewModelScope).collect {
                _uiState.value = it
            }
        }
    }

    fun onToggleBalanceVisibility() {
        _uiState.update { it.copy(balancesVisible = !it.balancesVisible) }
    }

    fun onAddAccountClicked() {
        _uiState.update { it.copy(showAddAccountDialog = true) }
    }

    fun onDismissAddAccountDialog() {
        _uiState.update { it.copy(showAddAccountDialog = false) }
    }

    fun onAccountLongClicked(account: Account) {
        _uiState.update { it.copy(accountToAction = account, showAccountActionChoiceDialog = true) }
    }

    fun onDismissAccountActionChoiceDialog() {
        _uiState.update { it.copy(accountToAction = null, showAccountActionChoiceDialog = false) }
    }

    fun onEditAccountClicked() {
        _uiState.update { it.copy(showEditAccountDialog = true, showAccountActionChoiceDialog = false) }
    }

    fun onDismissEditAccountDialog() {
        _uiState.update { it.copy(accountToAction = null, showEditAccountDialog = false) }
    }

    fun onDeleteAccountClicked() {
        _uiState.update { it.copy(showDeleteAccountConfirmationDialog = true, showAccountActionChoiceDialog = false) }
    }

    fun onDismissDeleteAccountConfirmationDialog() {
        _uiState.update { it.copy(showDeleteAccountConfirmationDialog = false) }
    }

    fun onConfirmDeleteAccount() {
        viewModelScope.launch {
            _uiState.value.accountToAction?.let {
                financeViewModel.deleteAccount(it)
                _uiState.update { state ->
                    state.copy(
                        showDeleteAccountConfirmationDialog = false,
                        accountToAction = null,
                        snackbarMessage = "Account '${it.title}' deleted"
                    )
                }
            }
        }
    }

    fun onTransactionLongClicked(transaction: MyTransaction) {
        _uiState.update { it.copy(transactionToAction = transaction, showTransactionActionChoiceDialog = true) }
    }

    fun onDismissTransactionActionChoiceDialog() {
        _uiState.update { it.copy(transactionToAction = null, showTransactionActionChoiceDialog = false) }
    }

    fun onEditTransactionClicked() {
        val transaction = _uiState.value.transactionToAction ?: return
        viewModelScope.launch {
            val isDefault = financeViewModel.isDefaultCategory(transaction.categoryId ?: -1)
            _uiState.update {
                it.copy(
                    showTransactionActionChoiceDialog = false,
                    showEditTransactionDialog = true,
                    editDialogState = HomepageEditTransactionDialogState(
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
                transactionToAction = null,
                showEditTransactionDialog = false,
                editDialogState = HomepageEditTransactionDialogState()
            )
        }
    }

    fun onDeleteTransactionClicked() {
        _uiState.update { it.copy(showDeleteTransactionConfirmationDialog = true, showTransactionActionChoiceDialog = false) }
    }

    fun onDismissDeleteTransactionConfirmationDialog() {
        _uiState.update { it.copy(showDeleteTransactionConfirmationDialog = false) }
    }

    fun onConfirmDeleteTransaction() {
        viewModelScope.launch {
            _uiState.value.transactionToAction?.let {
                financeViewModel.deleteTransaction(it)
                _uiState.update { state ->
                    state.copy(
                        showDeleteTransactionConfirmationDialog = false,
                        transactionToAction = null,
                        snackbarMessage = "Transaction deleted"
                    )
                }
            }
        }
    }

    fun onChartTypeChanged() {
        val newType = if (_uiState.value.chartType == ChartType.PIE) ChartType.HISTOGRAM else ChartType.PIE
        _uiState.update { it.copy(chartType = newType) }
    }

    fun onChartTransactionTypeChanged() {
        val newType = if (_uiState.value.chartTransactionType == TransactionType.EXPENSE) TransactionType.INCOME else TransactionType.EXPENSE
        _uiState.update { it.copy(chartTransactionType = newType) }
    }

    fun onChartAccountFilterChanged(selectedIds: Set<Int>) {
        _uiState.update { it.copy(selectedChartAccountIds = selectedIds) }
    }

    fun onChartLongClicked(account: Account) {
        _uiState.update { it.copy(selectedAccountForDetail = account, showChartDetailDialog = true) }
    }

    fun onDismissChartDetailDialog() {
        _uiState.update { it.copy(showChartDetailDialog = false, selectedAccountForDetail = null) }
    }

    fun onShowAccountFilterDialog() {
        _uiState.update { it.copy(showAccountFilterDialog = true) }
    }

    fun onDismissAccountFilterDialog() {
        _uiState.update { it.copy(showAccountFilterDialog = false) }
    }

    fun onSnackbarMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun addAccount(title: String, balance: Double) {
        viewModelScope.launch {
            val userId = financeViewModel.userId.first()
            if (userId != null) {
                val newAccount = Account(
                    userId = userId,
                    title = title,
                    amount = balance,
                    initialAmount = balance
                )
                financeViewModel.addAccount(newAccount)
                _uiState.update { it.copy(showAddAccountDialog = false, snackbarMessage = "Account '$title' added!") }
            }
        }
    }

    fun updateAccount(accountToEdit: Account, newTitle: String, currentBalanceDisplayString: String) {
        viewModelScope.launch {
            val newDisplayedBalanceDouble = currentBalanceDisplayString.replace(',', '.').toDoubleOrNull()
            if (newDisplayedBalanceDouble != null && newTitle.isNotBlank()) {
                val balanceDifference = newDisplayedBalanceDouble - accountToEdit.amount
                val newCalculatedInitialAmount = accountToEdit.initialAmount + balanceDifference
                financeViewModel.updateAccountAndRecalculateBalance(accountToEdit.id, newTitle, newCalculatedInitialAmount)
                _uiState.update {
                    it.copy(
                        showEditAccountDialog = false,
                        accountToAction = null,
                        snackbarMessage = "Account '${newTitle}' updated!"
                    )
                }
            }
        }
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
                    editDialogState = HomepageEditTransactionDialogState()
                )
            }
        }
    }
}
