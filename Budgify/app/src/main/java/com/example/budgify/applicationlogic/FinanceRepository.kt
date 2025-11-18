package com.example.budgify.applicationlogic

import android.util.Log
import com.example.budgify.dataaccessobjects.AccountDao
import com.example.budgify.dataaccessobjects.CategoryDao
import com.example.budgify.dataaccessobjects.LoanDao
import com.example.budgify.dataaccessobjects.ObjectiveDao
import com.example.budgify.dataaccessobjects.TransactionDao
import com.example.budgify.entities.Account
import com.example.budgify.entities.Category
import com.example.budgify.entities.Loan
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.Objective
import com.example.budgify.entities.TransactionType
import com.example.budgify.entities.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val objectiveDao: ObjectiveDao,
    private val categoryDao: CategoryDao,
    private val loanDao: LoanDao,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    // TRANSACTIONS
//    fun getAllTransactions(): Flow<List<MyTransaction>> {
//        return transactionDao.getAllTransactions()
//    }
    fun getAllTransactionsWithDetails(): Flow<List<TransactionWithDetails>> {
        return transactionDao.getAllTransactionsWithDetails()
    }
    suspend fun getTransactionById(transactionId: Int): MyTransaction? = transactionDao.getTransactionById(transactionId)

    suspend fun insertTransaction(myTransaction: MyTransaction) {
        transactionDao.insert(myTransaction)
    }
    suspend fun updateTransaction(myTransaction: MyTransaction) {
        //Log.d("FinanceRepository", "Updating transaction: $myTransaction")
        transactionDao.update(myTransaction)
        //Log.d("FinanceRepository", "Updated transaction: $myTransaction")
    }
    suspend fun deleteTransaction(myTransaction: MyTransaction) {
        transactionDao.delete(myTransaction)
    }

    // OBJECTIVES
    fun getAllObjectives(): Flow<List<Objective>> {
        return objectiveDao.getAllGoalsByDate()
    }
    suspend fun insertObjective(objective: Objective) {
        objectiveDao.insert(objective)
    }
    fun getObjectiveById(id: Int): Flow<Objective> {
        return objectiveDao.getGoalById(id)
    }
    suspend fun updateObjective(objective: Objective) {
        objectiveDao.update(objective)
    }
    suspend fun deleteObjective(objective: Objective) {
        objectiveDao.delete(objective)
    }

    // --- USER LEVEL AND XP (from DataStore) ---
    val userLevel: Flow<Int> = userPreferencesRepository.userLevel
    val userXp: Flow<Int> = userPreferencesRepository.userXp

    suspend fun updateUserLevelAndXp(level: Int, xp: Int) {
        userPreferencesRepository.updateUserLevelAndXp(level, xp)
    }

    suspend fun getInitialUserLevel(): Int = userPreferencesRepository.getInitialUserLevel()
    suspend fun getInitialUserXp(): Int = userPreferencesRepository.getInitialUserXp()

    //CATEGORIES
    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories()
    }
    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insert(category)
    }
    fun getCategoryById(id: Int): Flow<Category> {
        return categoryDao.getCategoryById(id)
    }
    suspend fun getCategoryByIdNonFlow(id: Int): Category? {
        return categoryDao.getCategoryByIdNonFlow(id) // Calls the new suspend fun in DAO
    }
    suspend fun deleteCategory(category: Category) {
        categoryDao.delete(category)
    }
    suspend fun updateCategory(category: Category) {
        categoryDao.update(category)
    }

    suspend fun getCategoryByDescription(description: String): Category? {
        return categoryDao.getCategoryByDescriptionSuspend(description)
    }

    // ACCOUNTS
    fun getAllAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts()
    }
    suspend fun insertAccount(account: Account) {
        accountDao.insert(account)
    }
    // Inside your FinanceRepository class
    suspend fun getAccountById(accountId: Int): Account? = accountDao.getAccountById(accountId)
    suspend fun getTransactionsForAccount(accountId: Int): List<MyTransaction> {
        return transactionDao.getTransactionsForAccount(accountId)
    }
    suspend fun updateAccount(account: Account) {
        accountDao.update(account)
    }
    suspend fun deleteAccount(account: Account) {
        accountDao.delete(account)
    }

    suspend fun updateAccountBalance(accountId: Int) {
        // Get the account from the database
        val account = accountDao.getAccountById(accountId)

        if (account != null) {
            // Get all transactions for this account
            val transactionsForAccount = transactionDao.getTransactionsForAccount(accountId)
            Log.d("FinanceRepository", "Transactions for account $accountId: $transactionsForAccount")
            // Calculate the new balance
            var newBalance = 0.0
            transactionsForAccount.forEach { transaction ->
                newBalance += if (transaction.type == TransactionType.INCOME) transaction.amount else -transaction.amount
            }

            // Update the account's amount in the database
            val updatedAccount = account.copy(amount = account.initialAmount + newBalance)
            accountDao.update(updatedAccount)
        }
    }

    // LOANS --- Nuova sezione per i Prestiti ---
    fun getAllLoans(): Flow<List<Loan>> {
        return loanDao.getAllLoans()
    }

    suspend fun insertLoan(loan: Loan) {
        loanDao.insert(loan)
    }

    suspend fun updateLoan(loan: Loan) {
        loanDao.update(loan)
    }

    suspend fun deleteLoan(loan: Loan) {
        loanDao.delete(loan)
    }
    // --- Fine sezione LOANS ---

    val unlockedThemes: Flow<Set<String>> = userPreferencesRepository.unlockedThemes

    suspend fun addUnlockedTheme(themeName: String) {
        userPreferencesRepository.addUnlockedTheme(themeName)
    }

    suspend fun resetUserLevelAndXp() { // Expose level/XP reset
        userPreferencesRepository.resetUserLevelAndXpToDefault()
    }

    suspend fun resetUnlockedThemes() { // Expose theme reset
        userPreferencesRepository.resetUnlockedThemesToDefault()
    }
}