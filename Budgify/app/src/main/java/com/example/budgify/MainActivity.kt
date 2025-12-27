package com.example.budgify

import android.content.Intent
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
import com.example.budgify.navigation.NavGraph
import com.example.budgify.ui.theme.BudgifyTheme
import com.example.budgify.userpreferences.ThemePreferenceManager
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val themePreferenceManager = ThemePreferenceManager(this)

        setContent {
            var currentTheme by remember { mutableStateOf(themePreferenceManager.getSavedTheme()) }

            BudgifyTheme(appTheme = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        themePreferenceManager = themePreferenceManager,
                        onThemeChange = { newTheme ->
                            themePreferenceManager.saveTheme(newTheme)
                            currentTheme = newTheme
                        },
                        onRestartApp = { restartApplication() } // Pass the restart function
                    )
                }
            }
        }
    }

    private fun restartApplication() {
        val packageManager = applicationContext.packageManager
        val intent = packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        val componentName = intent?.component
        // The `Intent.makeRestartActivityTask` is deprecated, use `Intent.FLAG_ACTIVITY_CLEAR_TASK` and `Intent.FLAG_ACTIVITY_NEW_TASK` instead
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext.startActivity(mainIntent)
        Runtime.getRuntime().exit(0) // Terminate the current process
    }
}