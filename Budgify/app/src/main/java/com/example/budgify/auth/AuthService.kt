package com.example.budgify.auth

interface AuthService {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String): Result<User>
    suspend fun logout()
    suspend fun getCurrentUser(): User?
}

data class User(val uid: String, val email: String?)