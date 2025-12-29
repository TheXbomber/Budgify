package com.example.budgify.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.budgify.dataaccessobjects.AccountDao
import com.example.budgify.dataaccessobjects.CategoryDao
import com.example.budgify.dataaccessobjects.LoanDao
import com.example.budgify.dataaccessobjects.ObjectiveDao
import com.example.budgify.dataaccessobjects.TransactionDao
import com.example.budgify.dataaccessobjects.UserDao
import com.example.budgify.entities.Account
import com.example.budgify.entities.Category
import com.example.budgify.entities.CategoryType
import com.example.budgify.entities.Loan
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.Objective
import com.example.budgify.entities.ObjectiveType
import com.example.budgify.entities.TransactionType
import com.example.budgify.entities.User
import kotlinx.coroutines.CoroutineScope
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
        Loan::class,
        User::class
   ],
    version = 12,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun objectiveDao(): ObjectiveDao
    abstract fun categoryDao(): CategoryDao
    abstract fun loanDao(): LoanDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}