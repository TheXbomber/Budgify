package com.example.budgify.applicationlogic

import android.content.Context
import android.util.Log
import com.example.budgify.dataaccessobjects.AccountDao
import com.example.budgify.dataaccessobjects.CategoryDao
import com.example.budgify.dataaccessobjects.LoanDao
import com.example.budgify.dataaccessobjects.ObjectiveDao
import com.example.budgify.dataaccessobjects.TransactionDao
import com.example.budgify.dataaccessobjects.UserDao
import com.example.budgify.entities.Account
import com.example.budgify.entities.Category
import com.example.budgify.entities.Loan
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.Objective
import com.example.budgify.entities.TransactionType
import com.example.budgify.entities.TransactionWithDetails
import com.example.budgify.entities.User
import kotlinx.coroutines.flow.Flow
import com.google.firebase.auth.FirebaseAuth // Import for Firebase Auth
import com.google.firebase.storage.FirebaseStorage // Import for Firebase Storage
import com.google.firebase.storage.StorageException
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.tasks.await // For await() on Tasks

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val objectiveDao: ObjectiveDao,
    private val categoryDao: CategoryDao,
    private val loanDao: LoanDao,
    private val userDao: UserDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val applicationContext: Context // Add Context to the constructor
) {
    private val DATABASE_NAME = "finance_database"
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    // USER
    suspend fun insertUser(user: User) {
        userDao.insert(user)
    }

    // TRANSACTIONS
    fun getAllTransactionsWithDetails(userId: String): Flow<List<TransactionWithDetails>> {
        return transactionDao.getAllTransactionsWithDetails(userId)
    }
    suspend fun getTransactionById(transactionId: Int): MyTransaction? = transactionDao.getTransactionById(transactionId)

    suspend fun insertTransaction(myTransaction: MyTransaction) {
        transactionDao.insert(myTransaction)
    }
    suspend fun updateTransaction(myTransaction: MyTransaction) {
        transactionDao.update(myTransaction)
    }
    suspend fun deleteTransaction(myTransaction: MyTransaction) {
        transactionDao.delete(myTransaction)
    }

    // OBJECTIVES
    fun getAllObjectives(userId: String): Flow<List<Objective>> {
        return objectiveDao.getAllGoalsByDate(userId)
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
    fun getAllCategories(userId: String): Flow<List<Category>> {
        return categoryDao.getAllCategories(userId)
    }
    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insert(category)
    }
    fun getCategoryById(id: Int): Flow<Category> {
        return categoryDao.getCategoryById(id)
    }
    suspend fun getCategoryByIdNonFlow(id: Int): Category? {
        return categoryDao.getCategoryByIdNonFlow(id)
    }
    suspend fun deleteCategory(category: Category) {
        categoryDao.delete(category)
    }
    suspend fun updateCategory(category: Category) {
        categoryDao.update(category)
    }

    suspend fun getCategoryByDescription(description: String, userId: String): Category? {
        return categoryDao.getCategoryByDescriptionSuspend(description, userId)
    }

    // ACCOUNTS
    fun getAllAccounts(userId: String): Flow<List<Account>> {
        return accountDao.getAllAccounts(userId)
    }
    suspend fun insertAccount(account: Account) {
        accountDao.insert(account)
    }
    suspend fun getAccountById(accountId: Int): Account? = accountDao.getAccountById(accountId)
    suspend fun getTransactionsForAccount(accountId: Int, userId: String): List<MyTransaction> {
        return transactionDao.getTransactionsForAccount(accountId, userId)
    }
    suspend fun updateAccount(account: Account) {
        accountDao.update(account)
    }
    suspend fun deleteAccount(account: Account) {
        accountDao.delete(account)
    }

    suspend fun updateAccountBalance(accountId: Int) {
        val account = accountDao.getAccountById(accountId)

        if (account != null) {
            val transactionsForAccount = transactionDao.getTransactionsForAccount(accountId, account.userId)
            Log.d("FinanceRepository", "Transactions for account $accountId: $transactionsForAccount")
            var newBalance = 0.0
            transactionsForAccount.forEach { transaction ->
                newBalance += if (transaction.type == TransactionType.INCOME) transaction.amount else -transaction.amount
            }

            val updatedAccount = account.copy(amount = account.initialAmount + newBalance)
            accountDao.update(updatedAccount)
        }
    }

    // LOANS --- Nuova sezione per i Prestiti ---
    fun getAllLoans(userId: String): Flow<List<Loan>> {
        return loanDao.getAllLoans(userId)
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

    suspend fun resetUserLevelAndXp() {
        userPreferencesRepository.resetUserLevelAndXpToDefault()
    }

    suspend fun resetUnlockedThemes() {
        userPreferencesRepository.resetUnlockedThemesToDefault()
    }

    /**
     * Backs up the local Room database files to Firebase Cloud Storage.
     * The database files are stored under the authenticated user's ID.
     * @return true if backup is successful, false otherwise.
     */
    suspend fun backupDatabase(): Boolean {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("FinanceRepository", "User not authenticated for backup.")
            return false
        }

        val dbPath = applicationContext.getDatabasePath(DATABASE_NAME)
        val dbFiles = listOf(
            dbPath,
            File(dbPath.path + "-shm"),
            File(dbPath.path + "-wal")
        ).filter { it.exists() }

        if (dbFiles.isEmpty()) {
            Log.e("FinanceRepository", "No database files found to backup.")
            return false
        }

        return try {
            dbFiles.forEach { file ->
                val storageRef = storage.reference.child("backups/$userId/${file.name}")
                storageRef.putFile(android.net.Uri.fromFile(file)).await()
                Log.d("FinanceRepository", "Backed up ${file.name} successfully.")
            }
            true
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Error during database backup: ${e.message}", e)
            false
        }
    }

    /**
     * Restores the database files from Firebase Cloud Storage to the local device.
     * This operation will overwrite the existing local database files.
     * The app should ideally be restarted after a restore for changes to take effect reliably.
     * @return true if restore is successful, false otherwise.
     */
    suspend fun restoreDatabase(): Boolean {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("FinanceRepository", "User not authenticated for restore.")
            return false
        }

        val dbPath = applicationContext.getDatabasePath(DATABASE_NAME)
        val parentDir = dbPath.parentFile ?: run {
            Log.e("FinanceRepository", "Could not get database parent directory.")
            return false
        }

        // Ensure the directory exists
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }

        // List of files to restore (main db, shm, wal)
        val dbFileNames = listOf(
            DATABASE_NAME,
            "$DATABASE_NAME-shm",
            "$DATABASE_NAME-wal"
        )

        return try {
            dbFileNames.forEach { fileName ->
                val localFile = File(parentDir, fileName)
                val storageRef = storage.reference.child("backups/$userId/$fileName")

                // Delete existing local file before downloading
                if (localFile.exists()) {
                    localFile.delete()
                }

                storageRef.getFile(localFile).await()
                Log.d("FinanceRepository", "Restored $fileName successfully to ${localFile.absolutePath}.")
            }
            true
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Error during database restore: ${e.message}", e)
            // Clean up potentially partially restored files in case of error
            dbFileNames.forEach { fileName ->
                val localFile = File(parentDir, fileName)
                if (localFile.exists()) {
                    localFile.delete()
                }
            }
            false
        }
    }

    /**
     * Retrieves the last modified date of the main database backup file from Firebase Storage.
     * @return A formatted date string (e.g., "dd/MM/yyyy HH:mm") or null if no backup exists or an error occurs.
     */
    suspend fun getLastBackupDate(): String? {
        val userId = auth.currentUser?.uid ?: return null // No user, no backup date
        val mainDbFileName = DATABASE_NAME // Main database file name
        val storageRef = storage.reference.child("backups/$userId/$mainDbFileName")

        return try {
            val metadata = storageRef.metadata.await()
            val dateMillis = metadata.updatedTimeMillis
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            formatter.format(Date(dateMillis))
        } catch (e: StorageException) {
            // If the file simply doesn't exist, it's not an error we need to log as much as handle.
            if (e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                Log.d("FinanceRepository", "No backup file found for $userId/$mainDbFileName.")
            } else {
                Log.e("FinanceRepository", "Error getting backup metadata: ${e.message}", e)
            }
            null
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Unexpected error getting backup date: ${e.message}", e)
            null
        }
    }
}