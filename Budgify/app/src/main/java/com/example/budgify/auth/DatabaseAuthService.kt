package com.example.budgify.auth

import com.example.budgify.dataaccessobjects.UserDao
import com.example.budgify.utils.hashPassword
import java.util.UUID

class DatabaseAuthService(private val userDao: UserDao) : AuthService {

    private var currentUser: User? = null

    override suspend fun login(email: String, password: String): Result<User> {
        val user = userDao.getUserByEmail(email)
        val hashedPassword = hashPassword(password)
        return if (user != null && user.password == hashedPassword) {
            val authUser = User(user.id, user.email)
            currentUser = authUser
            Result.success(authUser)
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }

    override suspend fun register(email: String, password: String): Result<User> {
        if (userDao.getUserByEmail(email) != null) {
            return Result.failure(Exception("User already exists"))
        }
        val hashedPassword = hashPassword(password)
        val newUser = com.example.budgify.entities.User(
            id = UUID.randomUUID().toString(),
            email = email,
            password = hashedPassword
        )
        userDao.insert(newUser)
        val authUser = User(newUser.id, newUser.email)
        currentUser = authUser
        return Result.success(authUser)
    }

    override fun logout() {
        currentUser = null
    }

    override fun getCurrentUser(): User? {
        return currentUser
    }
}