package com.example.budgify.applicationlogic

import android.app.Application
import android.util.Log
import com.example.budgify.BuildConfig
import com.example.budgify.database.FinanceDatabase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class FinanceApplication : Application() {

    override fun onCreate() {
        super.onCreate() // This MUST be the first line.
        
        // Since the auto-initializer is disabled, we manually initialize the default instance.
        FirebaseApp.initializeApp(this)
        
        // Now, configure App Check on the single, definitive default instance.
        val firebaseAppCheck = FirebaseAppCheck.getInstance()

        if (BuildConfig.DEBUG) {
            Log.d("AppCheck", "Initializing App Check with DEBUG provider.")
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            Log.d("AppCheck", "Initializing App Check with PLAY INTEGRITY provider.")
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
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
            userPreferencesRepository = userPreferencesRepository
        )
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(applicationContext)
    }
}