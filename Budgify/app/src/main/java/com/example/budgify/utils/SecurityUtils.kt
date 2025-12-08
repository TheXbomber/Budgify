package com.example.budgify.utils

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

data class SecurityQuestionAnswer(val questionIndex: Int, val answer: String)

val securityQuestions = listOf(
    "What was the name of your first pet?",
    "What is your mother's maiden name?",
    "What was the name of your elementary school?",
    "In what city were you born?",
    "What is your favorite book?"
)

fun hashPassword(password: String): String {
    val bytes = password.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}

fun getSavedPinFromContext(context: Context): String? {
    return try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "AppSettings",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.getString("access_pin", null)
    } catch (e: Exception) {
        Log.e("SecurityUtils", "Error reading PIN", e)
        null
    }
}

fun getSavedSecurityQuestionAnswer(context: Context): SecurityQuestionAnswer? {
    try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "AppSettings",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val questionIndex = sharedPreferences.getInt("security_question_index", -1)
        val answer = sharedPreferences.getString("security_answer", null)

        return if (questionIndex != -1 && answer != null) {
            SecurityQuestionAnswer(questionIndex, answer)
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e("SecurityUtils", "Error retrieving saved security question/answer", e)
        return null
    }
}

fun saveSecurityQuestionAnswer(context: Context, questionIndex: Int, answer: String): Boolean {
    try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "AppSettings",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        with(sharedPreferences.edit()) {
            putInt("security_question_index", questionIndex)
            putString("security_answer", answer)
            apply()
        }
        return true
    } catch (e: Exception) {
        Log.e("SecurityUtils", "Error saving security question/answer", e)
        return false
    }
}