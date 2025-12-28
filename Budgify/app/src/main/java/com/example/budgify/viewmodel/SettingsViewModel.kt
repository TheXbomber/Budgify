package com.example.budgify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgify.applicationlogic.FinanceRepository
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.screen.SettingsOptionType
import com.example.budgify.userpreferences.AppTheme
import com.example.budgify.userpreferences.ThemePreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.map

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
    val isBackupInProgress: Boolean = false,
    val isRestoreInProgress: Boolean = false,
    val showBackupConfirmationDialog: Boolean = false,
    val showRestoreConfirmationDialog: Boolean = false,
    val lastBackupDate: String? = null // New: last backup date string
)

class SettingsViewModel(
    application: Application,
    private val financeViewModel: FinanceViewModel,
    private val themePreferenceManager: ThemePreferenceManager,
    private val financeRepository: FinanceRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        SettingsUiState(currentTheme = themePreferenceManager.getSavedTheme())
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    init {
        viewModelScope.launch {
            financeViewModel.unlockedThemeNames.collect { unlockedThemes ->
                _uiState.update { it.copy(unlockedThemeNames = unlockedThemes) }
            }
        }
        // Fetch last backup date on ViewModel initialization
        viewModelScope.launch {
            updateLastBackupDate()
        }
        // Re-fetch last backup date when user logs in/out
        viewModelScope.launch {
            auth.addAuthStateListener { firebaseAuth ->
                // Trigger an update when auth state changes
                viewModelScope.launch { updateLastBackupDate() }
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

    // Backup confirmation dialog actions
    fun onShowBackupConfirmation() {
        if (auth.currentUser == null) {
            _uiState.update { it.copy(snackbarMessage = "Please log in to backup your data.") }
            return
        }
        _uiState.update { it.copy(showBackupConfirmationDialog = true) }
    }

    fun onDismissBackupConfirmation() {
        _uiState.update { it.copy(showBackupConfirmationDialog = false) }
    }

    // Actual backup data call after confirmation
    fun confirmBackupData(onComplete: (Boolean) -> Unit) {
        _uiState.update { it.copy(showBackupConfirmationDialog = false, isBackupInProgress = true) }
        viewModelScope.launch {
            val success = financeRepository.backupDatabase()
            _uiState.update {
                it.copy(
                    isBackupInProgress = false,
                    snackbarMessage = if (success) "Backup successful!" else "Backup failed."
                )
            }
            onComplete(success)
            if (success) { updateLastBackupDate() } // Update date after successful backup
        }
    }

    // Restore confirmation dialog actions
    fun onShowRestoreConfirmation() {
        if (auth.currentUser == null) {
            _uiState.update { it.copy(snackbarMessage = "Please log in to restore your data.") }
            return
        }
        _uiState.update { it.copy(showRestoreConfirmationDialog = true) }
    }

    fun onDismissRestoreConfirmation() {
        _uiState.update { it.copy(showRestoreConfirmationDialog = false) }
    }

    // Actual restore data call after confirmation
    fun confirmRestoreData(onComplete: (Boolean) -> Unit) {
        _uiState.update { it.copy(showRestoreConfirmationDialog = false, isRestoreInProgress = true) }
        viewModelScope.launch {
            val success = financeRepository.restoreDatabase()
            _uiState.update {
                it.copy(
                    isRestoreInProgress = false,
                    snackbarMessage = if (success) "Restore successful!" else "Restore failed."
                )
            }
            onComplete(success)
            if (success) { updateLastBackupDate() } // Update date after successful restore
        }
    }

    private suspend fun updateLastBackupDate() {
        val dateString = financeRepository.getLastBackupDate()
        _uiState.update { it.copy(lastBackupDate = dateString) }
    }
}