package com.example.budgify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Account
import com.example.budgify.entities.Loan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreditsDebitsUiState(
    val totalActiveCredits: Double = 0.0,
    val totalActiveDebts: Double = 0.0,
    val lastThreeLoans: List<Loan> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val hasAccounts: Boolean = false,
    val showActionChoiceDialog: Boolean = false,
    val showDeleteConfirmationDialog: Boolean = false,
    val showEditLoanDialog: Boolean = false,
    val showAccountSelectionForCompletionDialog: Boolean = false,
    val showInsufficientBalanceDialog: Boolean = false,
    val selectedLoan: Loan? = null,
    val insufficientBalanceAccountInfo: Pair<String, Double>? = null,
    val snackbarMessage: String? = null
)

class CreditsDebitsViewModel(private val financeViewModel: FinanceViewModel) : ViewModel() {

    private val _uiState = MutableStateFlow(CreditsDebitsUiState())
    val uiState: StateFlow<CreditsDebitsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                financeViewModel.totalActiveCreditLoans,
                financeViewModel.totalActiveDebtLoans,
                financeViewModel.latestActiveLoans,
                financeViewModel.allAccounts,
                financeViewModel.hasAccounts
            ) { totalCredits, totalDebts, lastThreeLoans, accounts, hasAccounts ->
                _uiState.value.copy(
                    totalActiveCredits = totalCredits,
                    totalActiveDebts = totalDebts,
                    lastThreeLoans = lastThreeLoans,
                    accounts = accounts,
                    hasAccounts = hasAccounts
                )
            }.stateIn(viewModelScope).collect {
                _uiState.value = it
            }
        }
    }

    fun onLoanLongPressed(loan: Loan) {
        if (!loan.completed) {
            _uiState.update { it.copy(selectedLoan = loan, showActionChoiceDialog = true) }
        } else {
            _uiState.update { it.copy(snackbarMessage = "This loan is already paid/collected.") }
        }
    }

    fun onDismissActionChoiceDialog() {
        _uiState.update { it.copy(showActionChoiceDialog = false, selectedLoan = null) }
    }

    fun onEditLoanClicked() {
        _uiState.update { it.copy(showActionChoiceDialog = false, showEditLoanDialog = true) }
    }

    fun onDeleteLoanClicked() {
        _uiState.update { it.copy(showActionChoiceDialog = false, showDeleteConfirmationDialog = true) }
    }

    fun onCompleteLoanClicked() {
        _uiState.update { it.copy(showActionChoiceDialog = false, showAccountSelectionForCompletionDialog = true) }
    }

    fun onDismissDeleteConfirmationDialog() {
        _uiState.update { it.copy(showDeleteConfirmationDialog = false) }
    }

    fun onConfirmDeleteLoan() {
        viewModelScope.launch {
            _uiState.value.selectedLoan?.let { loan ->
                financeViewModel.deleteLoan(loan)
                _uiState.update {
                    it.copy(
                        showDeleteConfirmationDialog = false,
                        selectedLoan = null,
                        snackbarMessage = "'${loan.desc}' deleted"
                    )
                }
            }
        }
    }

    fun onDismissEditLoanDialog() {
        _uiState.update { it.copy(showEditLoanDialog = false, selectedLoan = null) }
    }

    fun onLoanUpdated(loan: Loan) {
        viewModelScope.launch {
            financeViewModel.updateLoan(loan)
            _uiState.update {
                it.copy(
                    showEditLoanDialog = false,
                    selectedLoan = null,
                    snackbarMessage = "Loan '${loan.desc}' updated"
                )
            }
        }
    }

    fun onDismissAccountSelectionDialog() {
        _uiState.update { it.copy(showAccountSelectionForCompletionDialog = false, selectedLoan = null) }
    }

    fun onAccountSelectedForCompletion(account: Account) {
        val loan = _uiState.value.selectedLoan ?: return
        if (loan.type == com.example.budgify.entities.LoanType.DEBT && loan.amount > account.amount) {
            _uiState.update {
                it.copy(
                    insufficientBalanceAccountInfo = Pair(account.title, account.amount),
                    showInsufficientBalanceDialog = true
                )
            }
        } else {
            viewModelScope.launch {
                financeViewModel.completeLoanAndCreateTransaction(loan, account.id)
                _uiState.update {
                    it.copy(
                        showAccountSelectionForCompletionDialog = false,
                        selectedLoan = null,
                        snackbarMessage = "${loan.type.name.lowercase().replaceFirstChar { it.titlecase() }} '${loan.desc}' marked complete. Transaction created for '${account.title}'."
                    )
                }
            }
        }
    }

    fun onDismissInsufficientBalanceDialog() {
        _uiState.update { it.copy(showInsufficientBalanceDialog = false, insufficientBalanceAccountInfo = null) }
    }

    fun onSnackbarMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
