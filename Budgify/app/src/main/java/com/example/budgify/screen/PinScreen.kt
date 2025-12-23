package com.example.budgify.screen

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.utils.getSavedPinFromContext
import com.example.budgify.utils.getSavedSecurityQuestionAnswer
import com.example.budgify.utils.removePinFromContext
import com.example.budgify.utils.securityQuestions

// Helper function to find a FragmentActivity in the context hierarchy
private fun Context.findFragmentActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinScreen(
    onPinVerified: () -> Unit,
    financeViewModel: FinanceViewModel
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val savedPin = remember { getSavedPinFromContext(context) }
    var showForgotPinDialog by remember { mutableStateOf(false) }

    val fragmentActivity = context.findFragmentActivity()
    Log.d("PinScreen", "FragmentActivity found: ${fragmentActivity != null}")

    val biometricManager = BiometricManager.from(context)
    val biometricAuthResult = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
    Log.d("PinScreen", "BiometricManager.canAuthenticate result: $biometricAuthResult")

    val canAuthenticate = remember(fragmentActivity) {
        fragmentActivity != null && biometricAuthResult == BiometricManager.BIOMETRIC_SUCCESS
    }
    Log.d("PinScreen", "Can authenticate: $canAuthenticate")

    if (fragmentActivity != null) {
        val executor = remember { ContextCompat.getMainExecutor(context) }
        val promptInfo = remember {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Budgify")
                .setSubtitle("Use your fingerprint to unlock")
                .setNegativeButtonText("Use PIN")
                .build()
        }
        val biometricPrompt = remember {
            BiometricPrompt(fragmentActivity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        // Only show snackbar for actual errors, not user cancellation
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            financeViewModel.showSnackbar("Authentication error: $errString")
                        }
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onPinVerified()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        financeViewModel.showSnackbar("Authentication failed")
                    }
                })
        }

        LaunchedEffect(key1 = Unit) {
            if (canAuthenticate) {
                biometricPrompt.authenticate(promptInfo)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Enter PIN", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (pin == savedPin) {
                    onPinVerified()
                } else {
                    error = "Invalid PIN"
                }
            }) {
                Text("Unlock")
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (canAuthenticate) {
                TextButton(onClick = { biometricPrompt.authenticate(promptInfo) }) {
                    Text("Use Fingerprint")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            TextButton(onClick = { showForgotPinDialog = true }) {
                Text("Forgot PIN?")
            }
        }

        if (showForgotPinDialog) {
            val savedSecurityQA = remember { getSavedSecurityQuestionAnswer(context) }
            var securityAnswer by remember { mutableStateOf("") }
            var securityError by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = { showForgotPinDialog = false },
                title = { Text("Forgot PIN") },
                text = {
                    Column {
                        if (savedSecurityQA != null) {
                            Text(securityQuestions[savedSecurityQA.questionIndex])
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = securityAnswer,
                                onValueChange = { securityAnswer = it },
                                label = { Text("Your Answer") }
                            )
                            securityError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            Text("No security question set. Cannot reset PIN.")
                        }
                    }
                },
                confirmButton = {
                    if (savedSecurityQA != null) {
                        Button(onClick = {
                            if (securityAnswer.equals(savedSecurityQA.answer, ignoreCase = true)) {
                                if (removePinFromContext(context)) {
                                    financeViewModel.showSnackbar("Access via security question complete. PIN has been reset.")
                                }
                                onPinVerified()
                            } else {
                                securityError = "Incorrect answer"
                            }
                        }) {
                            Text("Submit")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPinDialog = false }) {
                        Text(if (savedSecurityQA != null) "Cancel" else "Close")
                    }
                }
            )
        }
    } else {
        // Fallback UI or disable biometric features if FragmentActivity is not found
        // For now, we\'ll just show the PIN input without biometric options.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Enter PIN", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (pin == savedPin) {
                    onPinVerified()
                } else {
                    error = "Invalid PIN"
                }
            }) {
                Text("Unlock")
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { showForgotPinDialog = true }) {
                Text("Forgot PIN?")
            }
        }
        if (showForgotPinDialog) {
            val savedSecurityQA = remember { getSavedSecurityQuestionAnswer(context) }
            var securityAnswer by remember { mutableStateOf("") }
            var securityError by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = { showForgotPinDialog = false },
                title = { Text("Forgot PIN") },
                text = {
                    Column {
                        if (savedSecurityQA != null) {
                            Text(securityQuestions[savedSecurityQA.questionIndex])
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = securityAnswer,
                                onValueChange = { securityAnswer = it },
                                label = { Text("Your Answer") }
                            )
                            securityError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            Text("No security question set. Cannot reset PIN.")
                        }
                    }
                },
                confirmButton = {
                    if (savedSecurityQA != null) {
                        Button(onClick = {
                            if (securityAnswer.equals(savedSecurityQA.answer, ignoreCase = true)) {
                                if (removePinFromContext(context)) {
                                    financeViewModel.showSnackbar("Access via security question complete. PIN has been reset.")
                                }
                                onPinVerified()
                            } else {
                                securityError = "Incorrect answer"
                            }
                        }) {
                            Text("Submit")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPinDialog = false }) {
                        Text(if (savedSecurityQA != null) "Cancel" else "Close")
                    }
                }
            )
        }
    }
}
