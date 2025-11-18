package com.example.budgify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Account
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.TransactionType
import com.example.budgify.entities.TransactionWithDetails
import com.example.budgify.screen.ChartType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    // Charts
    val chartType: ChartType = ChartType.PIE,
    val chartTransactionType: TransactionType = TransactionType.EXPENSE,
    val selectedChartAccountIds: Set<Int> = emptySet(),
    val showChartDetailDialog: Boolean = false,
    val selectedAccountForDetail: Account? = null,
    val showAccountFilterDialog: Boolean = false
)

class HomepageViewModel(private val financeViewModel: FinanceViewModel) : ViewModel() {

    private val _uiState = MutableStateFlow(HomepageUiState())
    val uiState: StateFlow<HomepageUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                financeViewModel.allAccounts,
                financeViewModel.allTransactionsWithDetails
            ) { accounts, transactions ->
                _uiState.value.copy(
                    accounts = accounts,
                    transactions = transactions,
                    selectedChartAccountIds = if (_uiState.value.selectedChartAccountIds.isEmpty()) accounts.map { it.id }.toSet() else _uiState.value.selectedChartAccountIds
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
        _uiState.update { it.copy(showEditTransactionDialog = true, showTransactionActionChoiceDialog = false) }
    }

    fun onDismissEditTransactionDialog() {
        _uiState.update { it.copy(transactionToAction = null, showEditTransactionDialog = false) }
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

    fun addAccount(account: Account) {
        viewModelScope.launch {
            financeViewModel.addAccount(account)
            _uiState.update { it.copy(showAddAccountDialog = false, snackbarMessage = "Account '${account.title}' added!") }
        }
    }

    fun updateAccount(account: Account, newTitle: String, newInitialAmount: Double) {
        viewModelScope.launch {
            financeViewModel.updateAccountAndRecalculateBalance(account.id, newTitle, newInitialAmount)
            _uiState.update {
                it.copy(
                    showEditAccountDialog = false,
                    accountToAction = null,
                    snackbarMessage = "Account '${newTitle}' updated!"
                )
            }
        }
    }

    fun updateTransaction(transaction: MyTransaction) {
        viewModelScope.launch {
            financeViewModel.updateTransaction(transaction)
            _uiState.update {
                it.copy(
                    showEditTransactionDialog = false,
                    transactionToAction = null,
                    snackbarMessage = "Transaction updated"
                )
            }
        }
    }
}
