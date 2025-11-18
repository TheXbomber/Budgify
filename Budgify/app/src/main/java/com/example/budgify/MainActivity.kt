package com.example.budgify

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.budgify.applicationlogic.FinanceApplication
import com.example.budgify.navigation.NavGraph
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.ui.theme.BudgifyTheme
import com.example.budgify.userpreferences.ThemePreferenceManager
import com.example.budgify.utils.SecurityQuestionAnswer
import com.example.budgify.utils.getSavedSecurityQuestionAnswer
import com.example.budgify.utils.securityQuestions

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themePreferenceManager = ThemePreferenceManager(this)

        setContent {
            var currentTheme by remember { mutableStateOf(themePreferenceManager.getSavedTheme()) }
            val navController = rememberNavController()

            var requiresPinEntry by remember { mutableStateOf(getSavedPin() != null) }

            // State for Dialogs
            var showDirectPinResetDialog by remember { mutableStateOf(false) } // For the original PinResetConfirmationDialog
            var showSecurityQuestionDialog by remember { mutableStateOf(false) }
            var showSecurityQuestionNotSetDialog by remember { mutableStateOf(false) }

            // State for Security Question Data
            var securityQuestionToAsk by remember { mutableStateOf<String?>(null) }
            var correctSecurityAnswer by remember { mutableStateOf<String?>(null) }
            var securityAnswerInput by remember { mutableStateOf("") }
            var securityQuestionErrorMessage by remember { mutableStateOf<String?>(null) }
            var showPinSuccessfullyResetDialog by remember { mutableStateOf(false) }

            val startDestination = if (requiresPinEntry) {
                ScreenRoutes.AccessPin.route
            } else {
                ScreenRoutes.Home.route
            }

            val financeApp = application as FinanceApplication
            val repository = financeApp.repository

            val financeViewModel: com.example.budgify.applicationlogic.FinanceViewModel =
                viewModel(factory = com.example.budgify.applicationlogic.FinanceViewModel.FinanceViewModelFactory(repository))

            BudgifyTheme(appTheme = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Dialog for direct PIN reset
                    if (showDirectPinResetDialog) {
                        PinResetConfirmationDialog(
                            onConfirm = {
                                clearSavedPin()
                                requiresPinEntry = false
                                showDirectPinResetDialog = false
                                navController.navigate(ScreenRoutes.Home.route) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onDismiss = {
                                showDirectPinResetDialog = false
                            }
                        )
                    }

                    // Dialog to input security question answer
                    if (showSecurityQuestionDialog && securityQuestionToAsk != null && correctSecurityAnswer != null) {
                        SecurityQuestionInputDialog(
                            question = securityQuestionToAsk!!,
                            answerInput = securityAnswerInput,
                            onAnswerChange = {
                                securityAnswerInput = it
                                securityQuestionErrorMessage = null // Clear error on new input
                            },
                            errorMessage = securityQuestionErrorMessage,
                            onConfirm = {
                                if (securityAnswerInput.equals(correctSecurityAnswer, ignoreCase = true)) {
                                    // Correct answer
                                    clearSavedPin()
                                    requiresPinEntry = false
                                    showSecurityQuestionDialog = false
                                    securityAnswerInput = "" // Clear input
                                    securityQuestionErrorMessage = null // Clear error
                                    showPinSuccessfullyResetDialog = true
                                    navController.navigate(ScreenRoutes.Home.route) {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    securityQuestionErrorMessage = "Incorrect answer. Please try again."
                                }
                            },
                            onDismiss = {
                                showSecurityQuestionDialog = false
                                securityAnswerInput = "" // Clear input
                                securityQuestionErrorMessage = null // Clear error
                            }
                        )
                    }

                    // Dialog if security question is not set up
                    if (showSecurityQuestionNotSetDialog) {
                        AlertDialog(
                            onDismissRequest = { showSecurityQuestionNotSetDialog = false },
                            title = { Text("PIN Recovery Not Set Up") },
                            text = { Text("You have not set up a security question for PIN recovery. You can set one in Settings. Would you like to reset your PIN directly? This will clear your current PIN.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showSecurityQuestionNotSetDialog = false
                                    showDirectPinResetDialog = true // Offer direct PIN reset
                                }) {
                                    Text("Reset PIN")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showSecurityQuestionNotSetDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    if (showPinSuccessfullyResetDialog) {
                        PinSuccessfullyResetDialog(
                            onDismiss = {
                                showPinSuccessfullyResetDialog = false
                            }
                        )
                    }


                    NavGraph(
                        navController = navController,
                        viewModel = financeViewModel,
                        themePreferenceManager = themePreferenceManager,
                        onThemeChange = { newTheme ->
                            themePreferenceManager.saveTheme(newTheme)
                            currentTheme = newTheme
                        },
                        startDestination = startDestination,
                        onForgotPinClicked = {
                            val savedQA: SecurityQuestionAnswer? = getSavedSecurityQuestionAnswer(this@MainActivity)

                            if (savedQA != null && savedQA.answer.isNotBlank()) {
                                if (savedQA.questionIndex >= 0 && savedQA.questionIndex < securityQuestions.size) {
                                    securityQuestionToAsk = securityQuestions[savedQA.questionIndex]
                                    correctSecurityAnswer = savedQA.answer
                                    securityAnswerInput = ""
                                    securityQuestionErrorMessage = null
                                    showSecurityQuestionDialog = true
                                } else {
                                    Log.e("MainActivity", "Invalid security question index: ${savedQA.questionIndex}")
                                    showSecurityQuestionNotSetDialog = true
                                }
                            } else {
                                showSecurityQuestionNotSetDialog = true
                            }
                        }
                    )
                }
            }
        }
    }

    private fun getSavedPin(): String? {
        return try {
            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val sharedPreferences = EncryptedSharedPreferences.create(
                this,
                "AppSettings",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            sharedPreferences.getString("access_pin", null)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error reading PIN", e)
            null
        }
    }

    private fun clearSavedPin() {
        try {
            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val sharedPreferences = EncryptedSharedPreferences.create(
                this,
                "AppSettings",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            with(sharedPreferences.edit()) {
                remove("access_pin")
                apply()
            }
            Log.i("MainActivity", "Saved PIN has been cleared.")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error clearing PIN", e)
        }
    }
}

@Composable
fun PinSuccessfullyResetDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Your PIN has been reset!") },
        text = { Text("Since you've performed access via a security question, your PIN has been reset. You can set a new PIN in the Settings if you wish.") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun PinResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset PIN?") },
        text = { Text("If you reset the PIN, you will need to set a new one. This will clear your current PIN. Are you sure you want to continue?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Reset PIN", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SecurityQuestionInputDialog(
    question: String,
    answerInput: String,
    onAnswerChange: (String) -> Unit,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Forgot PIN - Security Question") },
        text = {
            Column {
                Text(question)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = answerInput,
                    onValueChange = onAnswerChange,
                    label = { Text("Your Answer") },
                    isError = errorMessage != null,
                    singleLine = true
                )
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Submit Answer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
