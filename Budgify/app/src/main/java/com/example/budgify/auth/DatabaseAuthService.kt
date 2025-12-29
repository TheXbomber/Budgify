package com.example.budgify.auth

import android.content.Context
import android.util.Log
import com.example.budgify.dataaccessobjects.UserDao
import com.example.budgify.utils.hashPassword
import com.google.firebase.auth.AuthResult // Import AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
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
        val hashedPassword = hashPassword(password)

        // 1. Try Firebase login first
        try {
            val firebaseAuthResult: AuthResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = firebaseAuthResult.user

            if (firebaseUser != null) {
                var localUser = userDao.getUserByEmail(email)

                if (localUser == null) {
                    // Firebase login successful, but no local user. Create one.
                    val newLocalUser = com.example.budgify.entities.User(
                        id = firebaseUser.uid,
                        email = email,
                        password = hashedPassword // Store the hashed provided password for local login
                    )
                    userDao.insert(newLocalUser)
                    Log.d("DatabaseAuthService", "Created new local user after successful Firebase login.")
                } else if (localUser.id != firebaseUser.uid || localUser.password != hashedPassword) {
                    // Local user exists, but UID or password mismatch. Update local user.
                    val updatedLocalUser = localUser.copy(
                        id = firebaseUser.uid,
                        password = hashedPassword
                    )
                    userDao.update(updatedLocalUser)
                    Log.d("DatabaseAuthService", "Updated local user after successful Firebase login (UID/password mismatch).")
                }

                prefs.edit().putString(USER_EMAIL_KEY, email).apply()
                return Result.success(User(firebaseUser.uid, firebaseUser.email))
            }
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            // Specific handling for Firebase invalid credentials
            Log.e("DatabaseAuthService", "Firebase login failed due to invalid credentials: ${e.message}")
            // Fall through to local login attempt
        } catch (e: Exception) {
            // General Firebase login failure (e.g., network issues)
            Log.e("DatabaseAuthService", "Firebase login failed. Error: ${e.message}", e)
            // Fall through to local login attempt
        }

        // 2. If Firebase login failed or was skipped, try local login as a fallback
        val localUser = userDao.getUserByEmail(email)
        if (localUser != null && localUser.password == hashedPassword) {
            prefs.edit().putString(USER_EMAIL_KEY, email).apply()
            Log.d("DatabaseAuthService", "Local login successful (Firebase either failed or was not tried).")
            return Result.success(User(localUser.id, localUser.email))
        }

        return Result.failure(Exception("Invalid credentials or account not found."))
    }

    override suspend fun register(email: String, password: String): Result<User> {
        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters long."))
        }

        // Check if user already exists locally BEFORE trying Firebase, to prevent local collision issues
        // if Firebase registration succeeds but local fails for some reason other than collision.
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
            val userEmail = firebaseUser.email ?: ""
            var localUser = userDao.getUserByEmail(userEmail)

            // If a Firebase user exists, ensure a corresponding local user exists and is up-to-date.
            // Password is not available here, so we don't try to sync it.
            // A successful login or registration flow would handle the password for the local user.
            if (localUser == null) {
                // If no local user, create a minimal one. The password would be updated on a successful login.
                val newLocalUser = com.example.budgify.entities.User(
                    id = firebaseUser.uid,
                    email = userEmail,
                    password = "" // Placeholder: Actual password will be set during login/registration.
                )
                userDao.insert(newLocalUser)
                Log.d("DatabaseAuthService", "Created minimal local user for existing Firebase user in getCurrentUser.")
            } else if (localUser.id != firebaseUser.uid) {
                // If local user exists but UID doesn't match, update it.
                val updatedLocalUser = localUser.copy(id = firebaseUser.uid)
                userDao.update(updatedLocalUser)
                Log.d("DatabaseAuthService", "Updated local user UID for existing Firebase user in getCurrentUser.")
            }
            User(firebaseUser.uid, firebaseUser.email)
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