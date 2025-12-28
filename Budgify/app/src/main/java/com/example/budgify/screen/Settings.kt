package com.example.budgify.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Settings // Import Settings icon
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults // Import TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.budgify.R
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.auth.AuthViewModel
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.userpreferences.AppTheme
import com.example.budgify.utils.getSavedPinFromContext
import com.example.budgify.utils.getSavedSecurityQuestionAnswer
import com.example.budgify.utils.saveBiometricEnabled
import com.example.budgify.utils.saveSecurityQuestionAnswer
import com.example.budgify.utils.securityQuestions
import com.example.budgify.utils.getBiometricEnabled
import com.example.budgify.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

const val DEV = false

enum class SettingsOptionType {
    NONE, PIN, THEME, ABOUT, DEV_RESET, PASSWORD, BACKUP_RESTORE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    navController: NavController,
    viewModel: FinanceViewModel,
    settingsViewModel: SettingsViewModel,
    onThemeChange: (AppTheme) -> Unit,
    authViewModel: AuthViewModel,
    onRestartApp: () -> Unit // Added callback for app restart
) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Settings.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val user by authViewModel.user.collectAsStateWithLifecycle()

    var showRestartAppDialog by remember { mutableStateOf(false) }

    // State for the dropdown menu
    var expanded by remember { mutableStateOf(false) }
    val selectedOptionTitle = remember(uiState.selectedOption) {
        when (uiState.selectedOption) {
            SettingsOptionType.NONE -> "Select an option..."
            SettingsOptionType.PIN -> "Access Security"
            SettingsOptionType.PASSWORD -> "Change Password"
            SettingsOptionType.THEME -> "Theme"
            SettingsOptionType.BACKUP_RESTORE -> "Backup & Restore"
            SettingsOptionType.ABOUT -> "About the app"
            SettingsOptionType.DEV_RESET -> "DEV: Reset Level & Unlocks"
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            settingsViewModel.onSnackbarMessageShown()
        }
    }

    // Handle showing restart dialog if restore was successful
    LaunchedEffect(uiState.snackbarMessage) {
        if (uiState.snackbarMessage == "Restore successful!") {
            showRestartAppDialog = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopBar(navController, currentRoute, authViewModel, isHomeScreen = false) },
        bottomBar = {
            BottomBar(
                navController,
                viewModel
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dropdown Menu for Settings Options
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TextField(
                    value = selectedOptionTitle,
                    onValueChange = {},
                    readOnly = true,
                    // Removed label
                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = "Settings") }, // Changed icon and content description
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        // Keeping cursor and text colors as default or adjust as needed
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .height(56.dp), // Made the selection box larger (default height is 56.dp, so this ensures it is explicit)
                    placeholder = { // Added placeholder for hint when nothing is selected
                        if (uiState.selectedOption == SettingsOptionType.NONE) {
                            Text("Select an option...")
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Access Security") },
                        onClick = {
                            settingsViewModel.onOptionSelected(SettingsOptionType.PIN)
                            expanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Change Password") },
                        onClick = {
                            settingsViewModel.onOptionSelected(SettingsOptionType.PASSWORD)
                            expanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Theme") },
                        onClick = {
                            settingsViewModel.onOptionSelected(SettingsOptionType.THEME)
                            expanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.NightsStay, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Backup & Restore") },
                        onClick = {
                            settingsViewModel.onOptionSelected(SettingsOptionType.BACKUP_RESTORE)
                            expanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.CloudUpload, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("About the app") },
                        onClick = {
                            settingsViewModel.onOptionSelected(SettingsOptionType.ABOUT)
                            expanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                    )
                    if (DEV) {
                        DropdownMenuItem(
                            text = { Text("DEV: Reset Level & Unlocks") },
                            onClick = {
                                settingsViewModel.onOptionSelected(SettingsOptionType.DEV_RESET)
                                expanded = false
                            },
                            leadingIcon = { Icon(Icons.Filled.DeleteForever, contentDescription = null) }
                        )
                    }
                }
            }

            // Content display area (remains mostly the same)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                when (uiState.selectedOption) {
                    SettingsOptionType.NONE -> {
                        Text(
                            "Select an option for more details",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.align(Alignment.Center) // Center the text within the box
                        )
                    }
                    SettingsOptionType.PIN -> {
                        PinSettingsContent(
                            uiState = uiState,
                            onNewPinChange = { settingsViewModel.onNewPinChange(it) },
                            onConfirmPinChange = { settingsViewModel.onConfirmPinChange(it) },
                            onNewPinVisibilityToggle = { settingsViewModel.onNewPinVisibilityToggle() },
                            onConfirmPinVisibilityToggle = { settingsViewModel.onConfirmPinVisibilityToggle() },
                            snackbarHostState = snackbarHostState
                        )
                    }
                    SettingsOptionType.THEME -> {
                        ThemeSettingsContent(
                            uiState = uiState,
                            onThemeChange = {
                                settingsViewModel.onThemeSelected(it)
                                onThemeChange(it)
                            }
                        )
                    }
                    SettingsOptionType.ABOUT -> {
                        AboutSettingsContent()
                    }
                    SettingsOptionType.DEV_RESET -> {}
                    SettingsOptionType.PASSWORD -> {
                        ChangePasswordContent(
                            uiState = uiState,
                            authViewModel = authViewModel,
                            onCurrentPasswordChange = { settingsViewModel.onCurrentPasswordChange(it) },
                            onNewPasswordChange = { settingsViewModel.onNewPasswordChange(it) },
                            onConfirmNewPasswordChange = { settingsViewModel.onConfirmNewPasswordChange(it) },
                            onCurrentPasswordVisibilityToggle = { settingsViewModel.onCurrentPasswordVisibilityToggle() },
                            onNewPasswordVisibilityToggle = { settingsViewModel.onNewPasswordVisibilityToggle() },
                            onConfirmNewPasswordVisibilityToggle = { settingsViewModel.onConfirmNewPasswordVisibilityToggle() },
                            snackbarHostState = snackbarHostState
                        )
                    }
                    SettingsOptionType.BACKUP_RESTORE -> {
                        BackupRestoreContent(
                            settingsViewModel = settingsViewModel,
                            onRestartApp = onRestartApp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

        }
        if (uiState.showResetConfirmationDialog) {
            ResetConfirmationDialog(
                onConfirm = { settingsViewModel.onResetDialogConfirm() },
                onDismiss = { settingsViewModel.onResetDialogDismiss() }
            )
        }

        if (showRestartAppDialog) {
            AlertDialog(
                onDismissRequest = { showRestartAppDialog = false },
                title = { Text("Restart App Required") },
                text = { Text("Database restored successfully. Please restart the app for changes to take full effect.") },
                confirmButton = {
                    Button(onClick = {
                        showRestartAppDialog = false
                        onRestartApp()
                    }) {
                        Text("Restart Now")
                    }
                },
                dismissButton = {
                    Button(onClick = { showRestartAppDialog = false }) {
                        Text("Later")
                    }
                }
            )
        }
    }
}

@Composable
fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Reset") },
        text = { Text("Are you sure you want to reset all user level progress, XP, and unlocked themes? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// No longer needed as options are in dropdown
/*
@Composable
fun SettingsOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Normal)
    }
    Divider()
}
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSettingsContent(
    uiState: com.example.budgify.viewmodel.SettingsUiState,
    onNewPinChange: (String) -> Unit,
    onConfirmPinChange: (String) -> Unit,
    onNewPinVisibilityToggle: () -> Unit,
    onConfirmPinVisibilityToggle: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isPinSet by remember { mutableStateOf(getSavedPinFromContext(context) != null) }
    var savedSecurityQA by remember { mutableStateOf(getSavedSecurityQuestionAnswer(context)) }
    var isBiometricEnabled by remember { mutableStateOf(getBiometricEnabled(context)) } // Biometric state

    var selectedQuestionIndex by remember { mutableStateOf(savedSecurityQA?.questionIndex ?: 0) }
    var securityAnswerInput by remember { mutableStateOf(savedSecurityQA?.answer ?: "") }
    var securityQuestionDropdownExpanded by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        val loadedQA = getSavedSecurityQuestionAnswer(context)
        if (loadedQA != null) {
            selectedQuestionIndex = loadedQA.questionIndex
            securityAnswerInput = loadedQA.answer
        } else {
            selectedQuestionIndex = 0
            securityAnswerInput = ""
        }
    }

    val isSecurityQASet = remember { mutableStateOf(savedSecurityQA != null) }
    LaunchedEffect(savedSecurityQA) {
        isSecurityQASet.value = savedSecurityQA != null
        if (savedSecurityQA != null && securityAnswerInput.isEmpty()) {
            securityAnswerInput = savedSecurityQA!!.answer
            selectedQuestionIndex = savedSecurityQA!!.questionIndex
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            when {
                isPinSet && isSecurityQASet.value -> "Manage Access Security Methods"
                isPinSet -> "Set Security Question & Manage PIN"
                isSecurityQASet.value -> "Set PIN & Manage Security Question"
                else -> "Set PIN & Security Question"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (isPinSet) {
            Text("Change Access PIN", style = MaterialTheme.typography.titleMedium)
        } else {
            Text("Set New Access PIN", style = MaterialTheme.typography.titleMedium)
        }

        TextField(
            value = uiState.newPin,
            onValueChange = onNewPinChange,
            label = { Text("New PIN (min 4 digits)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "New PIN") },
            trailingIcon = {
                val image = if (uiState.newPinVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (uiState.newPinVisible) "Hide PIN" else "Show PIN"
                IconButton(onClick = onNewPinVisibilityToggle) {
                    Icon(imageVector = image, description)
                }
            },
            visualTransformation = if (uiState.newPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage?.contains("PIN", ignoreCase = true) == true
        )

        TextField(
            value = uiState.confirmPin,
            onValueChange = onConfirmPinChange,
            label = { Text(if (isPinSet) "Confirm New PIN (if changing)" else "Confirm New PIN") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm PIN") },
            trailingIcon = {
                val image = if (uiState.confirmPinVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (uiState.confirmPinVisible) "Hide PIN" else "Show PIN"
                IconButton(onClick = onConfirmPinVisibilityToggle) {
                    Icon(imageVector = image, description)
                }
            },
            visualTransformation = if (uiState.confirmPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage?.contains("PINs do not match", ignoreCase = true) == true
        )

        Text("Security Question for Temporary Access", style = MaterialTheme.typography.titleMedium)

        ExposedDropdownMenuBox(
            expanded = securityQuestionDropdownExpanded,
            onExpandedChange = { securityQuestionDropdownExpanded = !securityQuestionDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = securityQuestions[selectedQuestionIndex],
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Security Question") },
                leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = "Security Question") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = securityQuestionDropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = securityQuestionDropdownExpanded,
                onDismissRequest = { securityQuestionDropdownExpanded = false }
            ) {
                securityQuestions.forEachIndexed { index, question ->
                    DropdownMenuItem(
                        text = { Text(question) },
                        onClick = {
                            selectedQuestionIndex = index
                            securityQuestionDropdownExpanded = false
                            errorMessage = null
                        }
                    )
                }
            }
        }

        TextField(
            value = securityAnswerInput,
            onValueChange = {
                securityAnswerInput = it
                errorMessage = null
            },
            label = { Text("Your Answer") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Security Answer") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage?.contains("answer", ignoreCase = true) == true
        )

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // Biometric Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Fingerprint Unlock")
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isBiometricEnabled,
                onCheckedChange = {
                    isBiometricEnabled = it
                    saveBiometricEnabled(context, it)
                    scope.launch { snackbarHostState.showSnackbar("Fingerprint unlock preference saved.") }
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            if (isPinSet) {
                Button(
                    onClick = {
                        try {
                            val masterKey = MasterKey.Builder(context)
                                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
                            val sharedPreferences = EncryptedSharedPreferences.create(
                                context, "AppSettings", masterKey,
                                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                            )
                            with(sharedPreferences.edit()) {
                                remove("access_pin")
                                apply()
                            }
                            scope.launch { snackbarHostState.showSnackbar("PIN removed successfully.") }
                            errorMessage = null
                            onNewPinChange("")
                            onConfirmPinChange("")
                            isPinSet = false
                        } catch (e: Exception) {
                            Log.e("PinSettingsContent", "Error removing PIN", e)
                            errorMessage = "Error removing PIN."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Remove PIN")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        errorMessage = null
                        var changesMade = false
                        var showSuccessMessage = "Settings updated."

                        if (securityAnswerInput.isBlank()) {
                            errorMessage = "Security answer cannot be empty."
                            return@Button
                        }

                        val isTryingToSetOrChangePin =
                            uiState.newPin.isNotEmpty() || uiState.confirmPin.isNotEmpty()
                        if (isTryingToSetOrChangePin) {
                            if (uiState.newPin.length < 4) {
                                errorMessage = "New PIN must be at least 4 digits long."
                                return@Button
                            }
                            if (uiState.newPin != uiState.confirmPin) {
                                errorMessage = "PINs do not match."
                                return@Button
                            }
                        }

                        var pinSavedSuccessfully = true
                        var qaSavedSuccessfully = true

                        if (isTryingToSetOrChangePin && uiState.newPin.isNotBlank()) {
                            try {
                                val masterKey = MasterKey.Builder(context)
                                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
                                val sharedPreferences = EncryptedSharedPreferences.create(
                                    context, "AppSettings", masterKey,
                                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                                )
                                with(sharedPreferences.edit()) {
                                    putString("access_pin", uiState.newPin)
                                    apply()
                                }
                                changesMade = true
                                isPinSet = true
                                onNewPinChange("")
                                onConfirmPinChange("")
                            } catch (e: Exception) {
                                Log.e("PinSettingsContent", "Error saving PIN", e)
                                errorMessage = "Error saving PIN."
                                pinSavedSuccessfully = false
                            }
                        }

                        val currentSavedQA = getSavedSecurityQuestionAnswer(context)
                        val qaChanged = currentSavedQA?.questionIndex != selectedQuestionIndex ||
                                currentSavedQA?.answer != securityAnswerInput
                        val isFirstTimeSettingQA = currentSavedQA == null

                        if (qaChanged || isFirstTimeSettingQA) {
                            if (saveSecurityQuestionAnswer(
                                    context,
                                    selectedQuestionIndex,
                                    securityAnswerInput
                                )
                            ) {
                                savedSecurityQA = com.example.budgify.utils.SecurityQuestionAnswer(
                                    selectedQuestionIndex,
                                    securityAnswerInput
                                )
                                isSecurityQASet.value = true
                                changesMade = true
                            } else {
                                errorMessage =
                                    (errorMessage ?: "") + " Error saving security question."
                                qaSavedSuccessfully = false
                            }
                        }

                        if (changesMade) {
                            when {
                                isTryingToSetOrChangePin && uiState.newPin.isNotBlank() && (qaChanged || isFirstTimeSettingQA) ->
                                    showSuccessMessage =
                                        if (pinSavedSuccessfully && qaSavedSuccessfully) "PIN and security question updated." else "Partial update. Check errors."

                                isTryingToSetOrChangePin && uiState.newPin.isNotBlank() ->
                                    showSuccessMessage =
                                        if (pinSavedSuccessfully) "PIN updated." else "Error saving PIN."

                                qaChanged || isFirstTimeSettingQA ->
                                    showSuccessMessage =
                                        if (qaSavedSuccessfully) "Security question updated." else "Error saving security question."
                            }
                            scope.launch { snackbarHostState.showSnackbar(showSuccessMessage) }
                            if (isTryingToSetOrChangePin && pinSavedSuccessfully) {
                                onNewPinChange("")
                                onConfirmPinChange("")
                            }
                        } else if (errorMessage == null) {
                            scope.launch { snackbarHostState.showSnackbar("No changes were made.") }
                        }

                    },
                    enabled = securityAnswerInput != (savedSecurityQA?.answer ?: "") ||
                            selectedQuestionIndex != (savedSecurityQA?.questionIndex ?: 0) ||
                            uiState.newPin.isNotEmpty() || uiState.confirmPin.isNotEmpty() ||
                            (!isSecurityQASet.value && securityAnswerInput.isNotBlank()),
                ) {
                    Text("Save")
                }
            }
        }
    }
}


@Composable
fun ThemeSettingsContent(
    uiState: com.example.budgify.viewmodel.SettingsUiState,
    onThemeChange: (AppTheme) -> Unit
) {
    val availableThemes = remember(uiState.unlockedThemeNames) {
        AppTheme.entries.filter { themeEnum ->
            uiState.unlockedThemeNames.contains(themeEnum.name)
        }.sortedBy { it.unlockLevel }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Choose App Theme",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (AppTheme.entries.isEmpty()) {
            Text(
                "No themes defined in the app.",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Column {
                val allThemesSorted = remember { AppTheme.entries.sortedBy { it.unlockLevel } }
                allThemesSorted.forEach { theme ->
                    val isUnlocked = uiState.unlockedThemeNames.contains(theme.name)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isUnlocked) {
                                if (isUnlocked) {
                                    onThemeChange(theme)
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (uiState.currentTheme == theme),
                            onClick = if (isUnlocked) {
                                {
                                    onThemeChange(theme)
                                }
                            } else null,
                            enabled = isUnlocked
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = theme.displayName,
                            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        )
                        if (!isUnlocked) {
                            Text(
                                text = "(Unlocks at Level ${theme.unlockLevel})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    Divider()
                }
            }
        }
    }
}

@Composable
fun AboutSettingsContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Budgify", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Image(
            painter = painterResource(id = R.drawable.ic_launcher),
            contentDescription = "App Icon",
            modifier = Modifier
                .size(96.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            "Your personal finance manager",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Text("Version: 1.1.0", style = MaterialTheme.typography.bodyLarge)
        Text("Developers:", style = MaterialTheme.typography.bodyLarge)
        Text("A. Catalano, A. Rocchi, O. Iacobelli", style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordContent(
    uiState: com.example.budgify.viewmodel.SettingsUiState,
    authViewModel: AuthViewModel,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmNewPasswordChange: (String) -> Unit,
    onCurrentPasswordVisibilityToggle: () -> Unit,
    onNewPasswordVisibilityToggle: () -> Unit,
    onConfirmNewPasswordVisibilityToggle: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Change Password",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        TextField(
            value = uiState.currentPassword,
            onValueChange = onCurrentPasswordChange,
            label = { Text("Current Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Current Password") },
            trailingIcon = {
                val image = if (uiState.currentPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (uiState.currentPasswordVisible) "Hide password" else "Show password"
                IconButton(onClick = onCurrentPasswordVisibilityToggle) {
                    Icon(imageVector = image, description)
                }
            },
            visualTransformation = if (uiState.currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage?.contains("current password", ignoreCase = true) == true
        )

        TextField(
            value = uiState.newPassword,
            onValueChange = onNewPasswordChange,
            label = { Text("New Password (min 6 characters)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "New Password") },
            trailingIcon = {
                val image = if (uiState.newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (uiState.newPasswordVisible) "Hide password" else "Show password"
                IconButton(onClick = onNewPasswordVisibilityToggle) {
                    Icon(imageVector = image, description)
                }
            },
            visualTransformation = if (uiState.newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage?.contains("new password", ignoreCase = true) == true
        )

        TextField(
            value = uiState.confirmNewPassword,
            onValueChange = onConfirmNewPasswordChange,
            label = { Text("Confirm New Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm New Password") },
            trailingIcon = {
                val image = if (uiState.confirmNewPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (uiState.confirmNewPasswordVisible) "Hide password" else "Show password"
                IconButton(onClick = onConfirmNewPasswordVisibilityToggle) {
                    Icon(imageVector = image, description)
                }
            },
            visualTransformation = if (uiState.confirmNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage?.contains("passwords do not match", ignoreCase = true) == true
        )

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Button(
            onClick = {
                errorMessage = null
                if (uiState.newPassword.length < 6) {
                    errorMessage = "New password must be at least 6 characters long."
                    return@Button
                }
                if (uiState.newPassword != uiState.confirmNewPassword) {
                    errorMessage = "New passwords do not match."
                    return@Button
                }

                scope.launch {
                    val success = authViewModel.changePassword(uiState.currentPassword, uiState.newPassword)
                    if (success) {
                        snackbarHostState.showSnackbar("Password changed successfully!")
                        onCurrentPasswordChange("")
                        onNewPasswordChange("")
                        onConfirmNewPasswordChange("")
                    } else {
                        errorMessage = "Failed to change password. Check your current password."
                        snackbarHostState.showSnackbar("Failed to change password.")
                    }
                }
            },
            enabled = uiState.currentPassword.isNotBlank() && uiState.newPassword.isNotBlank() && uiState.confirmNewPassword.isNotBlank()
        ) {
            Text("Save New Password")
        }
    }
}

@Composable
fun BackupRestoreContent(
    settingsViewModel: SettingsViewModel,
    onRestartApp: () -> Unit // Callback to restart the app
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Cloud Backup & Restore",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Ensure you are logged in to your account before backing up or restoring data.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Button(
            onClick = { settingsViewModel.onShowBackupConfirmation() }, // Trigger confirmation
            enabled = !uiState.isBackupInProgress,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isBackupInProgress) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(8.dp))
                Text("Backing up...")
            } else {
                Icon(Icons.Default.CloudUpload, contentDescription = "Backup")
                Spacer(Modifier.width(8.dp))
                Text("Backup to Cloud")
            }
        }

        Text("Backup will overwrite any previous cloud backup.", style = MaterialTheme.typography.bodySmall)

        Button(
            onClick = { settingsViewModel.onShowRestoreConfirmation() }, // Trigger confirmation
            enabled = !uiState.isRestoreInProgress,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isRestoreInProgress) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(8.dp))
                Text("Restoring...")
            } else {
                Icon(Icons.Default.CloudDownload, contentDescription = "Restore")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restore from Cloud")
            }
        }

        Text("Restoring will replace your current local data.", style = MaterialTheme.typography.bodySmall)

        if (uiState.lastBackupDate != null) {
            Text(
                text = "Last backup: ${uiState.lastBackupDate}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            Text(
                text = "No backup found in cloud.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

    }

    // Backup Confirmation Dialog
    if (uiState.showBackupConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { settingsViewModel.onDismissBackupConfirmation() },
            title = { Text("Confirm Backup") },
            text = { Text("Are you sure you want to back up your data? This will overwrite any existing cloud backup.") },
            confirmButton = {
                Button(onClick = {
                    settingsViewModel.confirmBackupData { /* snackbar handled in ViewModel */ }
                }) {
                    Text("Backup")
                }
            },
            dismissButton = {
                Button(onClick = { settingsViewModel.onDismissBackupConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Restore Confirmation Dialog
    if (uiState.showRestoreConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { settingsViewModel.onDismissRestoreConfirmation() },
            title = { Text("Confirm Restore") },
            text = { Text("Are you sure you want to restore your data? This will overwrite all your current local app data with the cloud backup. This action cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    settingsViewModel.confirmRestoreData { /* snackbar/restart dialog handled in Settings */ }
                },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                Button(onClick = { settingsViewModel.onDismissRestoreConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }
}