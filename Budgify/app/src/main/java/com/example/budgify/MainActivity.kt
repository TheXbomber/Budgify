package com.example.budgify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgify.applicationlogic.FinanceApplication
import com.example.budgify.navigation.NavGraph
import com.example.budgify.ui.theme.BudgifyTheme
import com.example.budgify.userpreferences.ThemePreferenceManager

// DON'T DELETE THIS DEBUG TOKEN: 9f5704c2-d5ae-4289-a297-f745d590eba1
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val themePreferenceManager = ThemePreferenceManager(this)

        setContent {
            var currentTheme by remember { mutableStateOf(themePreferenceManager.getSavedTheme()) }

            val financeApp = application as FinanceApplication
            val repository = financeApp.repository

            val financeViewModel: com.example.budgify.applicationlogic.FinanceViewModel =
                viewModel(factory = com.example.budgify.applicationlogic.FinanceViewModel.FinanceViewModelFactory(repository))

            BudgifyTheme(appTheme = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        viewModel = financeViewModel,
                        themePreferenceManager = themePreferenceManager,
                        onThemeChange = { newTheme ->
                            themePreferenceManager.saveTheme(newTheme)
                            currentTheme = newTheme
                        }
                    )
                }
            }
        }
    }
}