package com.example.budgify.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgify.dataaccessobjects.CategoryDao
import com.example.budgify.entities.Category
import com.example.budgify.entities.DefaultCategories
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val categoryDao: CategoryDao,
    private val authService: AuthService
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            _user.value = authService.getCurrentUser()
            _isLoading.value = false
        }
    }

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authService.login(email, password)
            if (result.isSuccess) {
                _user.value = result.getOrNull()
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message)
            }
        }
    }

    fun register(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            if (password.length < 6) {
                onResult(false, "Password must be at least 6 characters long.")
                return@launch
            }
            val result = authService.register(email, password)
            if (result.isSuccess) {
                val registeredUser = result.getOrNull()
                _user.value = registeredUser
                if (registeredUser != null) {
                    createDefaultCategories(registeredUser.uid)
                }
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message)
            }
        }
    }

    private suspend fun createDefaultCategories(userId: String) {
        val defaultCategories = listOf(
            DefaultCategories.OBJECTIVES_EXP,
            DefaultCategories.OBJECTIVES_INC,
            DefaultCategories.CREDIT_INC,
            DefaultCategories.CREDIT_EXP,
            DefaultCategories.DEBT_EXP,
            DefaultCategories.DEBT_INC
        )
        defaultCategories.forEach {
            categoryDao.insert(
                Category(
                    userId = userId,
                    type = it.type,
                    desc = it.desc
                )
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authService.logout()
            _user.value = null
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Boolean {
        if (newPassword.length < 6) {
            return false
        }
        return authService.changePassword(currentPassword, newPassword).isSuccess
    }
}

class AuthViewModelFactory(
    private val categoryDao: CategoryDao,
    private val authService: AuthService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(categoryDao, authService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}