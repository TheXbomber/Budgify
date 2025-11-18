package com.example.budgify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Account
import com.example.budgify.entities.Loan
import com.example.budgify.entities.LoanType
import com.example.budgify.screen.LoanSectionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CredDebManagementUiState(
    val selectedSection: LoanSectionType = LoanSectionType.CREDITS,
    val loans: List<Loan> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val hasAccounts: Boolean = false,
    val snackbarMessage: String? = null,
    val loanToAction: Loan? = null,
    val showActionChoiceDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteConfirmationDialog: Boolean = false,
    val showAccountSelectionForCompletionDialog: Boolean = false,
    val showInsufficientBalanceDialog: Boolean = false,
    val insufficientBalanceAccountInfo: Pair<String, Double>? = null
)

class CredDebManagementViewModel(private val financeViewModel: FinanceViewModel) : ViewModel() {

    private val _uiState = MutableStateFlow(CredDebManagementUiState())
    val uiState: StateFlow<CredDebManagementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                financeViewModel.allLoans,
                financeViewModel.allAccounts,
                financeViewModel.hasAccounts,
                _uiState
            ) { allLoans, allAccounts, hasAccounts, currentState ->
                currentState.copy(
                    loans = allLoans.filter { it.type == currentState.selectedSection.loanType },
                    accounts = allAccounts,
                    hasAccounts = hasAccounts
                )
            }.stateIn(viewModelScope).collect {
                _uiState.value = it
            }
        }
    }

    fun onSectionSelected(section: LoanSectionType) {
        _uiState.update { it.copy(selectedSection = section) }
    }

    fun onLoanLongPressed(loan: Loan) {
        _uiState.update { it.copy(loanToAction = loan, showActionChoiceDialog = true) }
    }

    fun onDismissActionChoiceDialog() {
        _uiState.update { it.copy(showActionChoiceDialog = false, loanToAction = null) }
    }

    fun onEditLoanClicked() {
        _uiState.update { it.copy(showActionChoiceDialog = false, showEditDialog = true) }
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
            _uiState.value.loanToAction?.let { loan ->
                financeViewModel.deleteLoan(loan)
                _uiState.update {
                    it.copy(
                        showDeleteConfirmationDialog = false,
                        loanToAction = null,
                        snackbarMessage = "'${loan.desc}' deleted"
                    )
                }
            }
        }
    }

    fun onDismissEditLoanDialog() {
        _uiState.update { it.copy(showEditDialog = false, loanToAction = null) }
    }

    fun onLoanUpdated(loan: Loan) {
        viewModelScope.launch {
            financeViewModel.updateLoan(loan)
            _uiState.update {
                it.copy(
                    showEditDialog = false,
                    loanToAction = null,
                    snackbarMessage = "Loan '${loan.desc}' updated"
                )
            }
        }
    }

    fun onDismissAccountSelectionDialog() {
        _uiState.update { it.copy(showAccountSelectionForCompletionDialog = false, loanToAction = null) }
    }

    fun onAccountSelectedForCompletion(account: Account) {
        val loan = _uiState.value.loanToAction ?: return
        if (loan.type == LoanType.DEBT && loan.amount > account.amount) {
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
                        loanToAction = null,
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
