package com.example.budgify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Objective
import com.example.budgify.screen.calculateXpForNextLevel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class ObjectivesScreenUiState(
    val objectives: List<Objective> = emptyList(),
    val reachedCount: Int = 0,
    val unreachedCount: Int = 0,
    val currentLevel: Int = 1,
    val currentXp: Int = 0,
    val xpForNextLevel: Int = 100,
    val progressToNextLevel: Float = 0f,
    val creditsRepaidCount: Int = 0,
    val debtsCollectedCount: Int = 0
)

class ObjectivesViewModel(financeViewModel: FinanceViewModel) : ViewModel() {

    val uiState: StateFlow<ObjectivesScreenUiState> = combine(
        financeViewModel.allObjectives,
        financeViewModel.userLevel,
        financeViewModel.userXp,
        financeViewModel.completedCreditLoansCount,
        financeViewModel.completedDebtLoansCount
    ) { objectives, level, xp, creditsCount, debtsCount ->
        val reachedCount = objectives.count { it.completed }
        val unreachedCount = objectives.count { !it.completed && it.endDate.isAfter(LocalDate.now().minusDays(1)) }
        val xpForNextLevel = calculateXpForNextLevel(level)
        val progressToNextLevel = if (xpForNextLevel > 0) xp.toFloat() / xpForNextLevel else 0f

        ObjectivesScreenUiState(
            objectives = objectives,
            reachedCount = reachedCount,
            unreachedCount = unreachedCount,
            currentLevel = level,
            currentXp = xp,
            xpForNextLevel = xpForNextLevel,
            progressToNextLevel = progressToNextLevel,
            creditsRepaidCount = creditsCount,
            debtsCollectedCount = debtsCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ObjectivesScreenUiState()
    )
}
