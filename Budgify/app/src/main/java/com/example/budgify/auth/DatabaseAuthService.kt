package com.example.budgify.auth

import android.content.Context
import android.util.Log
import com.example.budgify.dataaccessobjects.UserDao
import com.example.budgify.utils.hashPassword
import com.google.firebase.auth.AuthResult // Import AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.EmailAuthProvider
import java.util.UUID
import kotlinx.coroutines.tasks.await // Import for await()

class DatabaseAuthService(
    private val userDao: UserDao,
    private val context: Context
) : AuthService {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val USER_EMAIL_KEY = "logged_in_user_email"
    private val firebaseAuth: FirebaseAuth = Firebase.auth // Initialize Firebase Auth

    override suspend fun login(email: String, password: String): Result<User> {
        val localUser = userDao.getUserByEmail(email)
        val hashedPassword = hashPassword(password)

        if (localUser != null && localUser.password == hashedPassword) {
            // Local authentication successful, now try Firebase
            try {
                val firebaseAuthResult: AuthResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = firebaseAuthResult.user

                if (firebaseUser != null) {
                    // Firebase login successful
                    if (localUser.id != firebaseUser.uid) {
                        userDao.update(localUser.copy(id = firebaseUser.uid))
                        Log.d("DatabaseAuthService", "Updated local user UID with Firebase UID.")
                    }
                    prefs.edit().putString(USER_EMAIL_KEY, email).apply()
                    return Result.success(User(firebaseUser.uid, firebaseUser.email))
                }
            } catch (e: Exception) {
                // Log Firebase login failure but do not block local login
                Log.e("DatabaseAuthService", "Firebase login failed, proceeding with local-only session. Error: ${e.message}")
            }

            // If Firebase login fails or firebaseUser is null, fall back to local-only session.
            Log.w("DatabaseAuthService", "Proceeding with local-only session for user: $email")
            prefs.edit().putString(USER_EMAIL_KEY, email).apply()
            return Result.success(User(localUser.id, localUser.email))

        } else {
            return Result.failure(Exception("Invalid local credentials"))
        }
    }

    override suspend fun register(email: String, password: String): Result<User> {
        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters long."))
        }
        if (userDao.getUserByEmail(email) != null) {
            return Result.failure(Exception("User already exists locally."))
        }

        // Try to create Firebase user first
        return try {
            val firebaseAuthResult: AuthResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = firebaseAuthResult.user

            if (firebaseUser != null) {
                val hashedPassword = hashPassword(password)
                val newUser = com.example.budgify.entities.User(
                    id = firebaseUser.uid, // Use Firebase UID as the local ID
                    email = email,
                    password = hashedPassword
                )
                userDao.insert(newUser) // Insert into local Room database
                prefs.edit().putString(USER_EMAIL_KEY, newUser.email).apply()
                Result.success(User(newUser.id, newUser.email))
            } else {
                Log.e("DatabaseAuthService", "Firebase createUserWithEmailAndPassword succeeded but user is null.")
                Result.failure(Exception("Firebase registration failed: user is null."))
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.e("DatabaseAuthService", "Firebase registration failed: User already exists in Firebase. ${e.message}", e)
            Result.failure(Exception("An account with this email already exists with Firebase."))
        } catch (e: Exception) {
            Log.e("DatabaseAuthService", "Error during Firebase registration: ${e.message}", e)
            Result.failure(Exception("Failed to register with Firebase: ${e.message}"))
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut() // Sign out from Firebase
        prefs.edit().remove(USER_EMAIL_KEY).apply()
        Log.d("DatabaseAuthService", "User logged out from local preferences and Firebase.")
    }

    override suspend fun getCurrentUser(): User? {
        // Prioritize getting the Firebase user as the source of truth for current session
        val firebaseUser = firebaseAuth.currentUser
        return if (firebaseUser != null) {
            val localUser = userDao.getUserByEmail(firebaseUser.email ?: "")
            if (localUser != null && localUser.id == firebaseUser.uid) {
                User(firebaseUser.uid, firebaseUser.email)
            } else {
                // If Firebase user exists but local user is not found or UID doesn't match,
                // it might indicate a data inconsistency.
                // For simplicity, we'll return the Firebase user.
                Log.w("DatabaseAuthService", "Firebase user found, but local user data mismatch or not found.")
                User(firebaseUser.uid, firebaseUser.email)
            }
        } else {
            // No Firebase user, check local preferences as a fallback
            val userEmail = prefs.getString(USER_EMAIL_KEY, null)
            if (userEmail != null) {
                val localUser = userDao.getUserByEmail(userEmail)
                localUser?.let { User(it.id, it.email) }
            }
            else {
                null
            }
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        if (newPassword.length < 6) {
            return Result.failure(Exception("New password must be at least 6 characters long."))
        }
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            return Result.failure(Exception("No user logged in to Firebase."))
        }

        val userEmail = firebaseUser.email ?: return Result.failure(Exception("Firebase user email not found."))
        val localUser = userDao.getUserByEmail(userEmail)
        if (localUser == null) {
            return Result.failure(Exception("Local user not found for Firebase user."))
        }

        val hashedPassword = hashPassword(currentPassword)
        if (localUser.password != hashedPassword) {
            return Result.failure(Exception("Invalid current password."))
        }

        return try {
            // Re-authenticate user before changing password
            val credential = EmailAuthProvider.getCredential(userEmail, currentPassword)
            firebaseUser.reauthenticate(credential).await()

            // Update password in Firebase
            firebaseUser.updatePassword(newPassword).await()

            // Update password in local database
            val newHashedPassword = hashPassword(newPassword)
            val updatedUser = localUser.copy(password = newHashedPassword)
            userDao.update(updatedUser)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("DatabaseAuthService", "Error changing password: ${e.message}", e)
            Result.failure(Exception("Failed to change password: ${e.message}"))
        }
    }
}