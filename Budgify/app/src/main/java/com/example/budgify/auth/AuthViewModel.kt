package com.example.budgify.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgify.dataaccessobjects.CategoryDao
import com.example.budgify.dataaccessobjects.UserDao
import com.example.budgify.entities.Category
import com.example.budgify.entities.DefaultCategories
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val categoryDao: CategoryDao,
    private val authService: AuthService
) : ViewModel() {

    private val _user = MutableStateFlow(authService.getCurrentUser())
    val user: StateFlow<com.example.budgify.auth.User?> = _user

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
        authService.logout()
        _user.value = null
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