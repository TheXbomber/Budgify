package com.example.budgify.auth

import android.content.Context
import com.example.budgify.dataaccessobjects.UserDao
import com.example.budgify.utils.hashPassword
import java.util.UUID

class DatabaseAuthService(
    private val userDao: UserDao,
    private val context: Context
) : AuthService {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val USER_EMAIL_KEY = "logged_in_user_email"

    override suspend fun login(email: String, password: String): Result<User> {
        val user = userDao.getUserByEmail(email)
        val hashedPassword = hashPassword(password)
        return if (user != null && user.password == hashedPassword) {
            prefs.edit().putString(USER_EMAIL_KEY, user.email).apply()
            val authUser = User(user.id, user.email)
            Result.success(authUser)
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }

    override suspend fun register(email: String, password: String): Result<User> {
        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters long."))
        }
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
        prefs.edit().putString(USER_EMAIL_KEY, newUser.email).apply()
        val authUser = User(newUser.id, newUser.email)
        return Result.success(authUser)
    }

    override suspend fun logout() {
        prefs.edit().remove(USER_EMAIL_KEY).apply()
    }

    override suspend fun getCurrentUser(): User? {
        val userEmail = prefs.getString(USER_EMAIL_KEY, null) ?: return null
        val user = userDao.getUserByEmail(userEmail)
        return user?.let { User(it.id, it.email) }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        if (newPassword.length < 6) {
            return Result.failure(Exception("New password must be at least 6 characters long."))
        }
        val userEmail = prefs.getString(USER_EMAIL_KEY, null)
        if (userEmail == null) {
            return Result.failure(Exception("No user logged in."))
        }

        val user = userDao.getUserByEmail(userEmail)
        if (user == null) {
            return Result.failure(Exception("Logged in user not found in database."))
        }

        val hashedPassword = hashPassword(currentPassword)
        if (user.password != hashedPassword) {
            return Result.failure(Exception("Invalid current password."))
        }

        val newHashedPassword = hashPassword(newPassword)
        val updatedUser = user.copy(password = newHashedPassword)
        userDao.update(updatedUser)
        return Result.success(Unit)
    }
}