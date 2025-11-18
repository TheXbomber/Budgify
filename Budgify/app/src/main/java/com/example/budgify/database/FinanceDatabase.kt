package com.example.budgify.database

import android.content.Context
import android.util.Log
import androidx.activity.result.launch
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgify.dataaccessobjects.AccountDao
import com.example.budgify.dataaccessobjects.CategoryDao
import com.example.budgify.dataaccessobjects.LoanDao
import com.example.budgify.dataaccessobjects.ObjectiveDao
import com.example.budgify.dataaccessobjects.TransactionDao
import com.example.budgify.entities.Account
import com.example.budgify.entities.Category
import com.example.budgify.entities.CategoryType
import com.example.budgify.entities.DefaultCategories
import com.example.budgify.entities.Loan
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.Objective
import com.example.budgify.entities.ObjectiveType
import com.example.budgify.entities.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class Converters {

    // Type Converters for LocalDate
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? {
        return epochDay?.let { LocalDate.ofEpochDay(it) }
    }

    // Type Converters for TransactionType enum
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun toTransactionType(typeName: String): TransactionType {
        return TransactionType.valueOf(typeName)
    }

    // Type Converters for ObjectiveType enum
    @TypeConverter
    fun fromObjectiveType(type: ObjectiveType): String {
        return type.name
    }

    @TypeConverter
    fun toObjectiveType(typeName: String): ObjectiveType {
        return ObjectiveType.valueOf(typeName)
    }

    // Type Converters for CategoryType enum
    @TypeConverter
    fun fromCategoryType(type: CategoryType): String {
        return type.name
    }

    @TypeConverter
    fun toCategoryType(typeName: String): CategoryType {
        return CategoryType.valueOf(typeName)
    }

}

@Database(
    entities = [
        MyTransaction::class,
        Account::class,
        Objective::class,
        Category::class,
        Loan::class
   ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class) // Se hai bisogno di TypeConverters
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun goalDao(): ObjectiveDao
    abstract fun categoryDao(): CategoryDao
    abstract fun loanDao(): LoanDao



    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database" // Il nome del tuo file database
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not covered in this codelab.
                    .fallbackToDestructiveMigration()
                    .addCallback(FinanceDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class FinanceDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) { // Perform on a background thread
                    populateDatabase(database.categoryDao())
                }
            }
        }

        suspend fun populateDatabase(categoryDao: CategoryDao) {
            // Add default categories if the table is empty (which it will be on first create)
            // You could add a check here if needed, but onCreate is usually sufficient.

            // Check if default categories already exist (optional, but good for robustness
            // if this callback were ever called under different circumstances, though onCreate is reliable)
            // Check for existence before inserting
            if (categoryDao.getCategoryByDescriptionSuspend(DefaultCategories.OBJECTIVES_EXP.desc) == null) {
                categoryDao.insert(DefaultCategories.OBJECTIVES_EXP)
            }
            if (categoryDao.getCategoryByDescriptionSuspend(DefaultCategories.OBJECTIVES_INC.desc) == null) {
                categoryDao.insert(DefaultCategories.OBJECTIVES_INC)
            }
            if (categoryDao.getCategoryByDescriptionSuspend(DefaultCategories.CREDIT_EXP.desc) == null) {
                categoryDao.insert(DefaultCategories.CREDIT_EXP)
            }
            if (categoryDao.getCategoryByDescriptionSuspend(DefaultCategories.CREDIT_INC.desc) == null) {
                categoryDao.insert(DefaultCategories.CREDIT_INC)
            }
            if (categoryDao.getCategoryByDescriptionSuspend(DefaultCategories.DEBT_EXP.desc) == null) {
                categoryDao.insert(DefaultCategories.DEBT_EXP)
            }
            if (categoryDao.getCategoryByDescriptionSuspend(DefaultCategories.DEBT_INC.desc) == null) {
                categoryDao.insert(DefaultCategories.DEBT_INC)
            }
            // You can log here if needed
            Log.d("AppDatabase", "Default categories pre-populated.")
        }
    }
}