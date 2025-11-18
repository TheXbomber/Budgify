package com.example.budgify.userpreferences
import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.util.Log

enum class AppTheme(val displayName: String, val unlockLevel: Int) {
    // --- Level 1: Core Themes ---
    LIGHT("Light", 1),
    DARK("Dark", 1),

    // --- Level 3: Common & Pleasant Variations ---
    MINTY_FRESH("Minty Fresh", 2),
    OCEAN_BLUE("Ocean Blue", 3), // Was level 5, bringing it slightly earlier

    // --- Level 5: Paired Light/Dark Themes ---
    LIGHT_LAVENDER("Lavender Bliss (Light)", 4),
    DARK_LAVENDER("Lavender Bliss (Dark)", 5),

    // --- Level 7: More Distinct Colors ---
    SUNNY_CITRUS("Sunny Citrus", 6), // Was level 15, feels like a mid-tier bright option
    FOREST_GREEN("Forest Green", 7), // Was level 10

    // --- Level 10: Earthy & Natural ---
    LIGHT_EARTHY("Earthy Tones (Light)", 8),
    DARK_EARTHY("Earthy Tones (Dark)", 9),

    // --- Level 12: Elegant & Unique ---
    ROSE_GOLD_TINT("Rose Gold Tint", 10),
    TEAL_AND_AMBER("Teal and Amber", 11), // Your new theme

    // --- Level 15: Bold & Focused ---
    SUNSET_ORANGE("Sunset Orange", 12),
    CRIMSON_FOCUS("Crimson Focus", 13),

    // --- Level 18: Deep & Sophisticated ---
    DEEP_OCEAN_SLATE("Deep Ocean Slate", 14),

    CHARCOAL_AND_GOLD_DUST("Charcoal and Gold Dust", 15)
}

class ThemePreferenceManager(context: Context) {
    private val sharedPreferences: EncryptedSharedPreferences by lazy<EncryptedSharedPreferences> {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Crea l'istanza e assegnala a una variabile tipizzata esplicitamente
            val encryptedPrefs: EncryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                "ThemeSettings", // File name for theme preferences
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
            encryptedPrefs // Restituisci la variabile tipizzata

        } catch (e: Exception) {
            Log.e("ThemePreferenceManager", "Error creating EncryptedSharedPreferences for theme", e)
            throw RuntimeException("Could not initialize EncryptedSharedPreferences for theme", e) as Nothing
        }
    }

    fun getSavedTheme(): AppTheme {
        val themeString = sharedPreferences.getString("app_theme", AppTheme.LIGHT.name)
        return try {
            AppTheme.valueOf(themeString ?: AppTheme.LIGHT.name)
        } catch (e: IllegalArgumentException) {
            Log.e("ThemePreferenceManager", "Invalid saved theme value: $themeString", e)
            AppTheme.LIGHT // Default to light theme in case of invalid saved value
        }
    }

    fun saveTheme(theme: AppTheme) {
        with(sharedPreferences.edit()) {
            putString("app_theme", theme.name)
            apply()
        }
    }
}

@Composable
fun rememberThemePreferenceManager(): ThemePreferenceManager {
    val context = LocalContext.current
    return remember { ThemePreferenceManager(context) }
}