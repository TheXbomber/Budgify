package com.example.budgify.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.utils.getSavedPinFromContext
import com.example.budgify.utils.getSavedSecurityQuestionAnswer
import com.example.budgify.utils.removePinFromContext
import com.example.budgify.utils.securityQuestions

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