package com.example.budgify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Account
import com.example.budgify.entities.Objective
import com.example.budgify.screen.ObjectivesManagementSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ObjectivesManagementUiState(
    val selectedSection: ObjectivesManagementSection = ObjectivesManagementSection.Active,
    val objectives: List<Objective> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val hasAccounts: Boolean = false,
    val snackbarMessage: String? = null,
    val objectiveToAction: Objective? = null,
    val showActionChoiceDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteConfirmationDialog: Boolean = false,
    val showAccountSelectionForCompletionDialog: Boolean = false,
    val showInsufficientBalanceDialog: Boolean = false,
    val insufficientBalanceAccountInfo: Pair<String, Double>? = null
)

class ObjectivesManagementViewModel(private val financeViewModel: FinanceViewModel) : ViewModel() {

    private val _uiState = MutableStateFlow(ObjectivesManagementUiState())
    val uiState: StateFlow<ObjectivesManagementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                financeViewModel.allObjectives,
                financeViewModel.allAccounts,
                financeViewModel.hasAccounts,
                _uiState.map { it.selectedSection }.distinctUntilChanged()
            ) { allObjectives, allAccounts, hasAccounts, selectedSection ->
                val now = LocalDate.now()
                _uiState.update {
                    it.copy(
                        objectives = when (selectedSection) {
                            ObjectivesManagementSection.Active -> allObjectives.filter { obj -> !obj.endDate.isBefore(now) && !obj.completed }
                            ObjectivesManagementSection.Expired -> allObjectives.filter { obj -> obj.endDate.isBefore(now) || obj.completed }
                        },
                        accounts = allAccounts,
                        hasAccounts = hasAccounts
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

    fun onSectionSelected(section: ObjectivesManagementSection) {
        _uiState.update { it.copy(selectedSection = section) }
    }

    fun onObjectiveLongPressed(objective: Objective) {
        _uiState.update { it.copy(objectiveToAction = objective, showActionChoiceDialog = true) }
    }

    fun onDismissActionChoiceDialog() {
        _uiState.update { it.copy(showActionChoiceDialog = false, objectiveToAction = null) }
    }

    fun onEditObjectiveClicked() {
        _uiState.update { it.copy(showActionChoiceDialog = false, showEditDialog = true) }
    }

    fun onDeleteObjectiveClicked() {
        _uiState.update { it.copy(showActionChoiceDialog = false, showDeleteConfirmationDialog = true) }
    }

    fun onCompleteObjectiveClicked() {
        _uiState.update { it.copy(showActionChoiceDialog = false, showAccountSelectionForCompletionDialog = true) }
    }

    fun onDismissDeleteConfirmationDialog() {
        _uiState.update { it.copy(showDeleteConfirmationDialog = false) }
    }

    fun onConfirmDeleteObjective() {
        viewModelScope.launch {
            _uiState.value.objectiveToAction?.let { objective ->
                financeViewModel.deleteObjective(objective)
                _uiState.update {
                    it.copy(
                        showDeleteConfirmationDialog = false,
                        objectiveToAction = null,
                        snackbarMessage = "'${objective.desc}' deleted"
                    )
                }
            }
        }
    }

    fun onDismissEditObjectiveDialog() {
        _uiState.update { it.copy(showEditDialog = false, objectiveToAction = null) }
    }

    fun onObjectiveUpdated(objective: Objective) {
        viewModelScope.launch {
            financeViewModel.updateObjective(objective)
            _uiState.update {
                it.copy(
                    showEditDialog = false,
                    objectiveToAction = null,
                    snackbarMessage = "Goal '${objective.desc}' updated"
                )
            }
        }
    }

    fun onDismissAccountSelectionDialog() {
        _uiState.update { it.copy(showAccountSelectionForCompletionDialog = false, objectiveToAction = null) }
    }

    fun onAccountSelectedForCompletion(account: Account) {
        val objective = _uiState.value.objectiveToAction ?: return
        if (objective.type == com.example.budgify.entities.ObjectiveType.EXPENSE && objective.amount > account.amount) {
            _uiState.update {
                it.copy(
                    insufficientBalanceAccountInfo = Pair(account.title, account.amount),
                    showInsufficientBalanceDialog = true
                )
            }
        } else {
            viewModelScope.launch {
                financeViewModel.completeObjectiveAndCreateTransaction(objective, account.id)
                _uiState.update {
                    it.copy(
                        showAccountSelectionForCompletionDialog = false,
                        objectiveToAction = null,
                        snackbarMessage = "Goal '${objective.desc}' reached. Transaction created for '${account.title}'."
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
