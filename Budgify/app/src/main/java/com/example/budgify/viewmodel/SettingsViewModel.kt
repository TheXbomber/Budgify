package com.example.budgify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel // Use AndroidViewModel to get applicationContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgify.applicationlogic.FinanceApplication
import com.example.budgify.applicationlogic.FinanceRepository
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.auth.AuthService
import com.example.budgify.screen.SettingsOptionType
import com.example.budgify.userpreferences.AppTheme
import com.example.budgify.userpreferences.ThemePreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth // Import for FirebaseAuth check

data class SettingsUiState(
    val selectedOption: SettingsOptionType = SettingsOptionType.NONE,
    val showResetConfirmationDialog: Boolean = false,
    val snackbarMessage: String? = null,
    // PIN settings
    val isPinSet: Boolean = false,
    val newPin: String = "",
    val confirmPin: String = "",
    val pinErrorMessage: String? = null,
    val newPinVisible: Boolean = false,
    val confirmPinVisible: Boolean = false,
    // Theme settings
    val currentTheme: AppTheme = AppTheme.LIGHT,
    val unlockedThemeNames: Set<String> = emptySet(),
    // Password settings
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val currentPasswordVisible: Boolean = false,
    val newPasswordVisible: Boolean = false,
    val confirmNewPasswordVisible: Boolean = false,
    val isBackupInProgress: Boolean = false, // New state for UI feedback
    val isRestoreInProgress: Boolean = false // New state for UI feedback
)

class SettingsViewModel(
    application: Application, // Change to Application
    private val financeViewModel: FinanceViewModel,
    private val themePreferenceManager: ThemePreferenceManager,
    private val financeRepository: FinanceRepository // Inject FinanceRepository
) : AndroidViewModel(application) { // Extend AndroidViewModel

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    init {
        viewModelScope.launch {
            combine(
                financeViewModel.unlockedThemeNames,
                _uiState
            ) { unlockedThemes, currentState ->
                currentState.copy(
                    unlockedThemeNames = unlockedThemes,
                    currentTheme = themePreferenceManager.getSavedTheme()
                )
            }.stateIn(viewModelScope).collect {
                _uiState.value = it
            }
        }
    }

    fun onOptionSelected(option: SettingsOptionType) {
        _uiState.update { it.copy(selectedOption = option) }
    }

    fun onResetDialogDismiss() {
        _uiState.update { it.copy(showResetConfirmationDialog = false) }
    }

    fun onResetDialogConfirm() {
        viewModelScope.launch {
            financeViewModel.resetUserProgressForTesting()
            _uiState.update {
                it.copy(
                    showResetConfirmationDialog = false,
                    snackbarMessage = "User level, XP, and themes reset!"
                )
            }
        }
    }

    fun onNewPinChange(pin: String) {
        _uiState.update { it.copy(newPin = pin.filter { char -> char.isDigit() }, pinErrorMessage = null) }
    }

    fun onConfirmPinChange(pin: String) {
        _uiState.update { it.copy(confirmPin = pin.filter { char -> char.isDigit() }, pinErrorMessage = null) }
    }

    fun onNewPinVisibilityToggle() {
        _uiState.update { it.copy(newPinVisible = !it.newPinVisible) }
    }

    fun onConfirmPinVisibilityToggle() {
        _uiState.update { it.copy(confirmPinVisible = !it.confirmPinVisible) }
    }

    fun onThemeSelected(theme: AppTheme) {
        themePreferenceManager.saveTheme(theme)
        _uiState.update { it.copy(currentTheme = theme) }
    }

    fun onSnackbarMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    // Password change functions
    fun onCurrentPasswordChange(password: String) {
        _uiState.update { it.copy(currentPassword = password) }
    }

    fun onNewPasswordChange(password: String) {
        _uiState.update { it.copy(newPassword = password) }
    }

    fun onConfirmNewPasswordChange(password: String) {
        _uiState.update { it.copy(confirmNewPassword = password) }
    }

    fun onCurrentPasswordVisibilityToggle() {
        _uiState.update { it.copy(currentPasswordVisible = !it.currentPasswordVisible) }
    }

    fun onNewPasswordVisibilityToggle() {
        _uiState.update { it.copy(newPasswordVisible = !it.newPasswordVisible) }
    }

    fun onConfirmNewPasswordVisibilityToggle() {
        _uiState.update { it.copy(confirmNewPasswordVisible = !it.confirmNewPasswordVisible) }
    }

    // New backup function
    fun backupData(onComplete: (Boolean) -> Unit) {
        if (auth.currentUser == null) {
            _uiState.update { it.copy(snackbarMessage = "Please log in to backup your data.") }
            onComplete(false)
            return
        }
        _uiState.update { it.copy(isBackupInProgress = true) }
        viewModelScope.launch {
            val success = financeRepository.backupDatabase()
            _uiState.update {
                it.copy(
                    isBackupInProgress = false,
                    snackbarMessage = if (success) "Backup successful!" else "Backup failed."
                )
            }
            onComplete(success)
        }
    }

    // New restore function
    fun restoreData(onComplete: (Boolean) -> Unit) {
        if (auth.currentUser == null) {
            _uiState.update { it.copy(snackbarMessage = "Please log in to restore your data.") }
            onComplete(false)
            return
        }
        _uiState.update { it.copy(isRestoreInProgress = true) }
        viewModelScope.launch {
            val success = financeRepository.restoreDatabase()
            _uiState.update {
                it.copy(
                    isRestoreInProgress = false,
                    snackbarMessage = if (success) "Restore successful!" else "Restore failed."
                )
            }
            onComplete(success)
        }
    }
}