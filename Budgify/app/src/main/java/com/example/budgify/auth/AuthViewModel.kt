package com.example.budgify.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgify.dataaccessobjects.CategoryDao
import com.example.budgify.dataaccessobjects.UserDao
import com.example.budgify.entities.Category
import com.example.budgify.entities.DefaultCategories
import com.example.budgify.entities.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val userDao: UserDao,
    private val categoryDao: CategoryDao
) : ViewModel() {

    // This will now safely get the correctly configured default instance.
    private val auth: FirebaseAuth = Firebase.auth

    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<com.google.firebase.auth.FirebaseUser?> = _user

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _user.value = auth.currentUser
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun register(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                _user.value = auth.currentUser
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val newUser = User(id = firebaseUser.uid, email = firebaseUser.email)
                    userDao.insert(newUser)
                    createDefaultCategories(firebaseUser.uid)
                }
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
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
        auth.signOut()
        _user.value = null
    }
}

class AuthViewModelFactory(
    private val userDao: UserDao,
    private val categoryDao: CategoryDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userDao, categoryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}