package com.example.budgify.auth

interface AuthService {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String): Result<User>
    fun logout()
    fun getCurrentUser(): User?
}

data class User(val uid: String, val email: String?)