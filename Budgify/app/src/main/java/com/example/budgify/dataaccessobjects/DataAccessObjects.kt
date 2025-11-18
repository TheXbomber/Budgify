package com.example.budgify.dataaccessobjects

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.budgify.entities.Account
import com.example.budgify.entities.Objective
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.Category
import com.example.budgify.entities.Loan
import com.example.budgify.entities.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(myTransaction: MyTransaction)

    @Update
    suspend fun update(myTransaction: MyTransaction)

    @Delete
    suspend fun delete(myTransaction: MyTransaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<MyTransaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: Int): MyTransaction?

    @Transaction // Use @Transaction when querying relations
    @Query("SELECT * FROM transactions")
    fun getAllTransactionsWithDetails(): Flow<List<TransactionWithDetails>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId")
    suspend fun getTransactionsForAccount(accountId: Int): List<MyTransaction> // Return List<MyTransaction>

}

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(account: Account)

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM accounts ORDER BY title ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): Account?
}

@Dao
interface ObjectiveDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(objective: Objective)

    @Update
    suspend fun update(objective: Objective)

    @Delete
    suspend fun delete(objective: Objective)

    @Query("SELECT * FROM objectives ORDER BY startDate ASC")
    fun getAllGoalsByDate(): Flow<List<Objective>>

    @Query("SELECT * FROM objectives WHERE id = :id")
    fun getGoalById(id: Int): Flow<Objective>
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long

    @Query("SELECT * FROM categories WHERE `desc` = :description LIMIT 1")
    suspend fun getCategoryByDescriptionSuspend(description: String): Category?

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryById(id: Int): Flow<Category>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryByIdNonFlow(id: Int): Category?
}


@Dao
interface LoanDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(loan: Loan)

    @Update
    suspend fun update(loan: Loan)

    @Delete
    suspend fun delete(loan: Loan)

    @Query("SELECT * FROM loans ORDER BY startDate DESC")
    fun getAllLoans(): Flow<List<Loan>>
}
