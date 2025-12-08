package com.example.budgify.applicationlogic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgify.auth.AuthService
import com.example.budgify.entities.Account
import com.example.budgify.entities.Category
import com.example.budgify.entities.DefaultCategories
import com.example.budgify.entities.Loan
import com.example.budgify.entities.LoanType
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.Objective
import com.example.budgify.entities.ObjectiveType
import com.example.budgify.entities.TransactionType
import com.example.budgify.userpreferences.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class FinanceViewModel(
    private val repository: FinanceRepository,
    private val authService: AuthService
) : ViewModel() {

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    init {
        viewModelScope.launch {
            _userId.value = authService.getCurrentUser()?.uid
        }
    }

    fun onUserLoggedIn() {
        viewModelScope.launch {
            _userId.value = authService.getCurrentUser()?.uid
        }
    }

    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages: Flow<String> = _snackbarMessages.asSharedFlow()

    private val _showAddTransactionDialog = MutableStateFlow(false)
    val showAddTransactionDialog: StateFlow<Boolean> = _showAddTransactionDialog.asStateFlow()

    private val _showAddObjectiveDialog = MutableStateFlow(false)
    val showAddObjectiveDialog: StateFlow<Boolean> = _showAddObjectiveDialog.asStateFlow()

    private val _showAddLoanDialog = MutableStateFlow(false)
    val showAddLoanDialog: StateFlow<Boolean> = _showAddLoanDialog.asStateFlow()

    fun onShowAddTransactionDialog() {
        _showAddTransactionDialog.value = true
    }

    fun onDismissAddTransactionDialog() {
        _showAddTransactionDialog.value = false
    }

    fun onShowAddObjectiveDialog() {
        _showAddObjectiveDialog.value = true
    }

    fun onDismissAddObjectiveDialog() {
        _showAddObjectiveDialog.value = false
    }

    fun onShowAddLoanDialog() {
        _showAddLoanDialog.value = true
    }

    fun onDismissAddLoanDialog() {
        _showAddLoanDialog.value = false
    }


    // TRANSACTIONS
    val allTransactionsWithDetails = userId.flatMapLatest { userId ->
        if (userId != null) {
            repository.getAllTransactionsWithDetails(userId)
        } else {
            MutableStateFlow(emptyList())
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addTransaction(myTransaction: MyTransaction) {
        viewModelScope.launch {
            repository.insertTransaction(myTransaction)
            repository.updateAccountBalance(myTransaction.accountId)
        }
    }

    suspend fun updateTransaction(myTransaction: MyTransaction) {
        val oldTransaction = withContext(Dispatchers.IO) {
            repository.getTransactionById(myTransaction.id)
        }
        repository.updateTransaction(myTransaction)
        if (oldTransaction != null) {
            if (oldTransaction.accountId != myTransaction.accountId) {
                withContext(Dispatchers.IO) {
                    repository.updateAccountBalance(oldTransaction.accountId)
                    repository.updateAccountBalance(myTransaction.accountId)
                }
            } else {
                withContext(Dispatchers.IO) {
                    repository.updateAccountBalance(myTransaction.accountId)
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                repository.updateAccountBalance(myTransaction.accountId)
            }
        }
    }

    fun deleteTransaction(myTransaction: MyTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(myTransaction)
            repository.updateAccountBalance(myTransaction.accountId)
        }
    }

    // OBJECTIVES
    val allObjectives = userId.flatMapLatest { userId ->
        if (userId != null) {
            repository.getAllObjectives(userId)
        } else {
            MutableStateFlow(emptyList())
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addObjective(objective: Objective) {
        viewModelScope.launch {
            repository.insertObjective(objective)
        }
    }

    fun updateObjective(objective: Objective) {
        viewModelScope.launch {
            repository.updateObjective(objective)
        }
    }

    fun deleteObjective(objective: Objective) {
        viewModelScope.launch {
            repository.deleteObjective(objective)
        }
    }

    // --- XP AND LEVEL SYSTEM ---

    private val _userLevel = MutableStateFlow(1)
    val userLevel: StateFlow<Int> = _userLevel.asStateFlow()

    private val _userXp = MutableStateFlow(0)
    val userXp: StateFlow<Int> = _userXp.asStateFlow()

    val xpForCurrentUserNextLevel: StateFlow<Int> = _userLevel.map { level ->
        calculateXpForNextLevel(level)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), calculateXpForNextLevel(1))

    private val _unlockedThemeNames = MutableStateFlow<Set<String>>(emptySet())
    val unlockedThemeNames: StateFlow<Set<String>> = _unlockedThemeNames.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val loadedLevel = repository.userLevel.first()
                _userLevel.value = loadedLevel
                _userXp.value = repository.userXp.first()
                repository.unlockedThemes.collect { themesFromDataStore ->
                    val themesUnlockedByLevel = AppTheme.entries
                        .filter { it.unlockLevel <= loadedLevel }
                        .map { it.name }
                        .toSet()
                    _unlockedThemeNames.value = themesFromDataStore + themesUnlockedByLevel
                }
            } catch (e: Exception) {
                Log.e("XP_System", "Error loading initial level/XP from DataStore", e)
                _userLevel.value = 1
                _userXp.value = 0
                val defaultThemesForLevel1 = AppTheme.entries
                    .filter { it.unlockLevel <= _userLevel.value }
                    .map { it.name }
                    .toSet()
                _unlockedThemeNames.value = defaultThemesForLevel1
            }
        }
    }

    private suspend fun ensureThemesForCurrentLevelAreStored() {
        val currentLvl = _userLevel.value
        val currentUnlockedInVm = _unlockedThemeNames.value

        AppTheme.entries.forEach { theme ->
            if (theme.unlockLevel <= currentLvl && !currentUnlockedInVm.contains(theme.name)) {
                try {
                    repository.addUnlockedTheme(theme.name)
                } catch (e: Exception) {
                    Log.e("ThemeUnlock_Ensure", "Failed to save ${theme.name} during ensure check.", e)
                }
            }
        }
    }

    fun completeLoanAndCreateTransaction(
        loan: Loan,
        accountId: Int,
    ) {
        viewModelScope.launch {
            if (!loan.completed) {
                val updatedLoan = loan.copy(completed = true)
                repository.updateLoan(updatedLoan)

                val transactionType: TransactionType
                val defaultCategoryDescription: String
                when (loan.type) {
                    LoanType.DEBT -> {
                        transactionType = TransactionType.EXPENSE
                        defaultCategoryDescription = DefaultCategories.DEBT_EXP.desc
                    }
                    LoanType.CREDIT -> {
                        transactionType = TransactionType.INCOME
                        defaultCategoryDescription = DefaultCategories.CREDIT_INC.desc
                    }
                }

                val defaultCategory = withContext(Dispatchers.IO) {
                    repository.getCategoryByDescription(defaultCategoryDescription, userId.value!!)
                }
                val categoryIdForTransaction = defaultCategory?.id

                if (categoryIdForTransaction == null) {
                    _snackbarMessages.emit("Error: Default category '$defaultCategoryDescription' not found for loan. Transaction created without category.")
                }

                val newTransaction = MyTransaction(
                    userId = userId.value!!,
                    accountId = accountId,
                    type = transactionType,
                    date = LocalDate.now(),
                    description = "Loan: ${loan.desc}",
                    amount = loan.amount,
                    categoryId = categoryIdForTransaction
                )
                addTransaction(newTransaction)

                val xpGained = calculateXpForLoanCompletion(loan)
                addXp(xpGained, null)
            }
        }
    }


    fun completeObjectiveAndCreateTransaction(
        objective: Objective,
        accountId: Int,
    ) {
        viewModelScope.launch {
            if (!objective.completed) {
                val updatedObjective = objective.copy(completed = true)
                repository.updateObjective(updatedObjective)

                val transactionType: TransactionType
                val defaultCategoryDescription: String

                when (objective.type) {
                    ObjectiveType.INCOME -> {
                        transactionType = TransactionType.INCOME
                        defaultCategoryDescription = DefaultCategories.OBJECTIVES_INC.desc
                    }
                    ObjectiveType.EXPENSE -> {
                        transactionType = TransactionType.EXPENSE
                        defaultCategoryDescription = DefaultCategories.OBJECTIVES_EXP.desc
                    }
                }

                val defaultCategory = withContext(Dispatchers.IO) {
                    repository.getCategoryByDescription(defaultCategoryDescription, userId.value!!)
                }

                val categoryIdForTransaction = defaultCategory?.id

                if (categoryIdForTransaction == null) {
                    _snackbarMessages.emit("Error: Default category '$defaultCategoryDescription' not found. Transaction created without category.")
                }

                val newTransaction = MyTransaction(
                    userId = userId.value!!,
                    accountId = accountId,
                    type = transactionType,
                    date = LocalDate.now(),
                    description = "Goal: ${objective.desc}",
                    amount = objective.amount,
                    categoryId = categoryIdForTransaction
                )
                addTransaction(newTransaction)

                val xpGained = calculateXpForObjective(objective)
                addXp(xpGained, null)
            }
        }
    }

    private fun addXp(
        amount: Int,
        onLevelUpCallback: ((newLevel: Int, newlyUnlockedTheme: AppTheme?) -> Unit)? = null
    ) {
        viewModelScope.launch {
            if (amount <= 0) {
                return@launch
            }

            var tempXp = _userXp.value + amount
            var tempLevel = _userLevel.value
            var xpNeededForNext = calculateXpForNextLevel(tempLevel)
            var hasLeveledUpThisGain = false
            var highestLevelReachedThisGain = tempLevel
            var newlyUnlockedThemeDuringGain: AppTheme? = null

            while (tempXp >= xpNeededForNext) {
                tempXp -= xpNeededForNext
                tempLevel++
                xpNeededForNext = calculateXpForNextLevel(tempLevel)
                hasLeveledUpThisGain = true
                highestLevelReachedThisGain = tempLevel

                _snackbarMessages.emit("Level Up! You are now Level $tempLevel!")
                val themeUnlockedNow = AppTheme.entries.find { theme ->
                    theme.unlockLevel == tempLevel && !_unlockedThemeNames.value.contains(theme.name)
                }

                if (themeUnlockedNow != null) {
                    repository.addUnlockedTheme(themeUnlockedNow.name)
                    _snackbarMessages.emit("New Theme Unlocked: ${themeUnlockedNow.displayName}!")
                    if (newlyUnlockedThemeDuringGain == null) {
                        newlyUnlockedThemeDuringGain = themeUnlockedNow
                    }
                }
            }

            _userLevel.value = tempLevel
            _userXp.value = tempXp

            try {
                repository.updateUserLevelAndXp(_userLevel.value, _userXp.value)
            } catch (e: Exception) {
                Log.e("XP_DEBUG_ADDXP", "Error saving Level/XP to DataStore", e)
            }

            if (hasLeveledUpThisGain) {
                onLevelUpCallback?.invoke(highestLevelReachedThisGain, newlyUnlockedThemeDuringGain)
            }
        }
    }


    private fun checkForThemeUnlock(newLevel: Int): AppTheme? {
        val currentUnlocked = unlockedThemeNames.value
        return AppTheme.entries.find { theme ->
            theme.unlockLevel == newLevel && !currentUnlocked.contains(theme.name)
        }
    }

    private fun calculateXpForLoanCompletion(loan: Loan): Int {
        var baseXP = 0
        val amountXp = maxOf(8, (loan.amount / 15).toInt())
        baseXP += amountXp
        when (loan.type) {
            LoanType.DEBT -> {
                val debtClearBonus = 20
                baseXP += debtClearBonus
            }
            LoanType.CREDIT -> {
                val creditCollectBonus = 10
                baseXP += creditCollectBonus
            }
        }
        if (loan.endDate != null) {
            val today = LocalDate.now()
            val daysRemaining = ChronoUnit.DAYS.between(today, loan.endDate)
            if (daysRemaining >= 0) {
                val onTimeBonus = (baseXP * 0.05).toInt()
                baseXP += onTimeBonus
            }
        }
        val finalXP = maxOf(15, baseXP)
        return finalXP
    }

    private fun calculateXpForObjective(objective: Objective): Int {
        var baseXP = 0
        val amountXp = maxOf(5, (objective.amount / 10).toInt())
        baseXP += amountXp
        val today = LocalDate.now()
        val daysRemaining = ChronoUnit.DAYS.between(today, objective.endDate)
        val totalDuration = ChronoUnit.DAYS.between(objective.startDate, objective.endDate)
        if (totalDuration <= 0) {
            if (daysRemaining >= 0) {
                baseXP += (baseXP * 0.1).toInt()
            }
        } else {
            if (daysRemaining >= 0) {
                val earlyCompletionRatio = daysRemaining.toDouble() / totalDuration.toDouble()
                val earlyCompletionBonus = (earlyCompletionRatio * (baseXP * 0.5)).toInt()
                baseXP += earlyCompletionBonus
            }
        }
        return maxOf(10, baseXP)
    }

    private fun calculateXpForNextLevel(level: Int): Int {
        if (level <= 0) return 100
        return 100 * level + (level - 1) * 50
    }


    //CATEGORIES
    val allCategories = userId.flatMapLatest { userId ->
        if (userId != null) {
            repository.getAllCategories(userId)
        } else {
            MutableStateFlow(emptyList())
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val defaultCategoryDescriptions = setOf(
        DefaultCategories.OBJECTIVES_EXP.desc,
        DefaultCategories.OBJECTIVES_INC.desc,
        DefaultCategories.CREDIT_EXP.desc,
        DefaultCategories.CREDIT_INC.desc,
        DefaultCategories.DEBT_EXP.desc,
        DefaultCategories.DEBT_INC.desc
    )

    suspend fun isDefaultCategory(categoryId: Int?): Boolean {
        if (categoryId == null) return false
        val category = repository.getCategoryByIdNonFlow(categoryId)
        return category?.desc in defaultCategoryDescriptions
    }

    val categoriesForTransactionDialog: Flow<List<Category>> = allCategories.map { categories ->
        val defaultCategoryDescriptions = setOf(
            DefaultCategories.OBJECTIVES_EXP.desc,
            DefaultCategories.OBJECTIVES_INC.desc,
            DefaultCategories.CREDIT_EXP.desc,
            DefaultCategories.CREDIT_INC.desc,
            DefaultCategories.DEBT_EXP.desc,
            DefaultCategories.DEBT_INC.desc
        )
        categories.filterNot { category ->
            defaultCategoryDescriptions.contains(category.desc)
        }
    }

    fun addCategory(category: Category, onCategoryCreated: (Category) -> Unit) {
        viewModelScope.launch {
            val newId = repository.insertCategory(category)
            if (newId != -1L) {
                val createdCategory = category.copy(id = newId.toInt())
                onCategoryCreated(createdCategory)
            } else {
                val existingCategory = repository.getCategoryByDescription(category.desc, userId.value!!)
                if (existingCategory != null) {
                    onCategoryCreated(existingCategory)
                } else {
                    Log.e("FinanceViewModel", "Failed to insert or find existing category: ${category.desc}")
                }
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // ACCOUNTS
    val allAccounts = userId.flatMapLatest { userId ->
        if (userId != null) {
            repository.getAllAccounts(userId)
        } else {
            MutableStateFlow(emptyList())
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addAccount(account: Account) {
        viewModelScope.launch {
            repository.insertAccount(account)
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    val hasAccounts: Flow<Boolean> = allAccounts.map { it.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    suspend fun updateAccountAndRecalculateBalance(
        accountId: Int,
        newTitle: String,
        newInitialAmount: Double
    ): Boolean {
        return viewModelScope.async {
            val accountToUpdate = repository.getAccountById(accountId)
            if (accountToUpdate == null) {
                return@async false
            }

            val transactionsForAccount = repository.getTransactionsForAccount(accountId, userId.value!!)

            var transactionsDelta = 0.0
            transactionsForAccount.forEach { transaction ->
                if (transaction.type == TransactionType.INCOME) {
                    transactionsDelta += transaction.amount
                } else {
                    transactionsDelta -= transaction.amount
                }
            }

            val newCurrentAmount = newInitialAmount + transactionsDelta
            val updatedAccount = accountToUpdate.copy(
                title = newTitle,
                initialAmount = newInitialAmount,
                amount = newCurrentAmount
            )

            repository.updateAccount(updatedAccount)
            true
        }.await()
    }


    // LOANS
    val allLoans: StateFlow<List<Loan>> = userId.flatMapLatest { userId ->
        if (userId != null) {
            repository.getAllLoans(userId)
        } else {
            MutableStateFlow(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalActiveCreditLoans: StateFlow<Double> = allLoans
        .map { loans ->
            loans.filter { it.type == LoanType.CREDIT && !it.completed }
                .sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalActiveDebtLoans: StateFlow<Double> = allLoans
        .map { loans ->
            loans.filter { it.type == LoanType.DEBT && !it.completed }
                .sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val latestActiveLoans: StateFlow<List<Loan>> = allLoans
        .map { loans ->
            loans.takeLast(6).reversed()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val completedCreditLoansCount: StateFlow<Int> = allLoans
        .map { loans ->
            loans.count { it.type == LoanType.CREDIT && it.completed }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val completedDebtLoansCount: StateFlow<Int> = allLoans
        .map { loans ->
            loans.count { it.type == LoanType.DEBT && it.completed }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun addLoan(loan: Loan, accountId: Int) {
        viewModelScope.launch {
            repository.insertLoan(loan)
            _snackbarMessages.emit("${loan.type.name.lowercase().replaceFirstChar { it.titlecase() }} '${loan.desc}' added.")

            val transactionType: TransactionType
            val defaultCategoryDescription: String

            when (loan.type) {
                LoanType.CREDIT -> {
                    transactionType = TransactionType.EXPENSE
                    defaultCategoryDescription = DefaultCategories.CREDIT_EXP.desc
                }
                LoanType.DEBT -> {
                    transactionType = TransactionType.INCOME
                    defaultCategoryDescription = DefaultCategories.DEBT_INC.desc
                }
            }

            val defaultCategory = withContext(Dispatchers.IO) {
                repository.getCategoryByDescription(defaultCategoryDescription, userId.value!!)
            }
            val categoryIdForTransaction = defaultCategory?.id

            if (categoryIdForTransaction == null) {
                _snackbarMessages.emit("Warning: Default category '$defaultCategoryDescription' not found. Transaction created without category.")
            }

            val newTransaction = MyTransaction(
                userId = userId.value!!,
                accountId = accountId,
                type = transactionType,
                date = loan.startDate,
                description = "Loan: ${loan.desc}",
                amount = loan.amount,
                categoryId = categoryIdForTransaction
            )
            addTransaction(newTransaction)
        }
    }

    fun updateLoan(loan: Loan) {
        viewModelScope.launch {
            repository.updateLoan(loan)
        }
    }

    fun deleteLoan(loan: Loan) {
        viewModelScope.launch {
            repository.deleteLoan(loan)
        }
    }

    fun getExpenseDistributionForAccount(accountId: Int): StateFlow<Map<Category, Double>> {
        return allTransactionsWithDetails
            .map { transactionsWithDetailsList ->
                transactionsWithDetailsList
                    .filter {
                        it.transaction.accountId == accountId &&
                                it.transaction.type == TransactionType.EXPENSE &&
                                it.category != null
                    }
                    .groupBy { it.category!! }
                    .mapValues { entry ->
                        entry.value.sumOf { transactionWithDetail -> transactionWithDetail.transaction.amount }
                    }
                    .toList()
                    .sortedByDescending { it.second }
                    .toMap()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyMap()
            )
    }

    fun getIncomeDistributionForAccount(accountId: Int): StateFlow<Map<Category, Double>> {
        return allTransactionsWithDetails
            .map { transactionsWithDetailsList ->
                transactionsWithDetailsList
                    .filter {
                        it.transaction.accountId == accountId &&
                                it.transaction.type == TransactionType.INCOME &&
                                it.category != null
                    }
                    .groupBy { it.category!! }
                    .mapValues { entry ->
                        entry.value.sumOf { transactionWithDetail -> transactionWithDetail.transaction.amount }
                    }
                    .toList()
                    .sortedByDescending { it.second }
                    .toMap()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyMap()
            )
    }

    fun resetUserProgressForTesting() {
        viewModelScope.launch {
            try {
                repository.resetUserLevelAndXp()
                _userLevel.value = 1
                _userXp.value = 0

                repository.resetUnlockedThemes()
                val defaultThemes = AppTheme.entries
                    .filter { it.unlockLevel <= 1 }
                    .map { it.name }
                    .toSet()
                _unlockedThemeNames.value = defaultThemes
                _snackbarMessages.emit("User progress has been reset.")
            } catch (e: Exception) {
                Log.e("DevReset", "Error resetting user progress", e)
                _snackbarMessages.emit("Error resetting progress.")
            }
        }
    }

    class FinanceViewModelFactory(
        private val repository: FinanceRepository,
        private val authService: AuthService
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FinanceViewModel(repository, authService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}