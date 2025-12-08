package com.example.budgify.applicationlogic

import android.app.Application
import com.example.budgify.auth.AuthService
import com.example.budgify.auth.DatabaseAuthService
import com.example.budgify.database.FinanceDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class FinanceApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database: FinanceDatabase by lazy {
        FinanceDatabase.getDatabase(this, applicationScope)
    }

    val repository: FinanceRepository by lazy {
        FinanceRepository(
            database.transactionDao(),
            database.accountDao(),
            database.objectiveDao(),
            database.categoryDao(),
            database.loanDao(),
            database.userDao(),
            userPreferencesRepository = userPreferencesRepository
        )
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(applicationContext)
    }

    val authService: AuthService by lazy {
        DatabaseAuthService(database.userDao())
    }
}