package com.example.budgify.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.budgify.applicationlogic.FinanceApplication
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.userpreferences.ThemePreferenceManager
import com.example.budgify.viewmodel.CategoriesViewModel
import com.example.budgify.viewmodel.CredDebManagementViewModel
import com.example.budgify.viewmodel.CreditsDebitsViewModel
import com.example.budgify.viewmodel.HomepageViewModel
import com.example.budgify.viewmodel.ObjectivesManagementViewModel
import com.example.budgify.viewmodel.ObjectivesViewModel
import com.example.budgify.viewmodel.SettingsViewModel
import com.example.budgify.viewmodel.TransactionsViewModel

class ViewModelFactory(
    private val application: Application,
    private val financeViewModel: FinanceViewModel,
    private val themePreferenceManager: ThemePreferenceManager? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoriesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoriesViewModel(financeViewModel) as T
        }
        if (modelClass.isAssignableFrom(ObjectivesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ObjectivesViewModel(financeViewModel) as T
        }
        if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionsViewModel(financeViewModel) as T
        }
        if (modelClass.isAssignableFrom(HomepageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomepageViewModel(financeViewModel) as T
        }
        if (modelClass.isAssignableFrom(CreditsDebitsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreditsDebitsViewModel(financeViewModel) as T
        }
        if (modelClass.isAssignableFrom(CredDebManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CredDebManagementViewModel(financeViewModel) as T
        }
        if (modelClass.isAssignableFrom(ObjectivesManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ObjectivesManagementViewModel(financeViewModel) as T
        }
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            // Access the FinanceRepository from the Application class
            val app = application as FinanceApplication
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(application, financeViewModel, themePreferenceManager!!, app.repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
