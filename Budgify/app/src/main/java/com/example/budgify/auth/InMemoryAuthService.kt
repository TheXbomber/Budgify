package com.example.budgify.auth

import java.util.UUID

class InMemoryAuthService : AuthService {

    private val users = mutableMapOf<String, Pair<String, User>>()
    private var currentUser: User? = null

    override suspend fun login(email: String, password: String): Result<User> {
        val storedUser = users[email]
        return if (storedUser != null && storedUser.first == password) {
            currentUser = storedUser.second
            Result.success(currentUser!!)
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }

    override suspend fun register(email: String, password: String): Result<User> {
        return if (users.containsKey(email)) {
            Result.failure(Exception("User already exists"))
        } else {
            val newUser = User(UUID.randomUUID().toString(), email)
            users[email] = Pair(password, newUser)
            currentUser = newUser
            Result.success(currentUser!!)
        }
    }

    override fun logout() {
        currentUser = null
    }

    override fun getCurrentUser(): User? {
        return currentUser
    }
}