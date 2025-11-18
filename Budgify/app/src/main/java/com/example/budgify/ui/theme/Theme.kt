package com.example.budgify.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.budgify.userpreferences.AppTheme

// --- Base Budgify Themes (Light & Dark) ---
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC5),
    tertiary = Color(0xFF3700B3), // Consider a lighter tertiary for better visibility on dark surfaces
    background = Color(0xFF121212),      // Your existing very dark background (good)
    onBackground = Color.White,          // White text on dark background
    surface = Color(0xFF1E1E1E),         // Very Dark Gray (not pure black)
    onSurface = Color(0xFFE0E0E0),       // Off-white/Very Light Gray for good contrast on the dark gray surface
    surfaceVariant = Color(0xFF2C2C2E),   // Slightly lighter Dark Gray for variants
    onSurfaceVariant = Color(0xFFCACACA),// Medium gray text for surface variants
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color(0xFFB0B0B0),      // Lighter gray for onTertiary, if tertiary is dark
    error = Color(0xFFCF6679),
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC5),
    tertiary = Color(0xFF3700B3),

    background = Color(0xFFF0F2F5),      // A slightly darker, neutral light gray for background
    onBackground = Color(0xFF18191C),   // Dark text, ensuring good contrast

    surface = Color(0xFFE9ECF0),         // A light gray for surfaces, darker than before
    onSurface = Color(0xFF151619),       // Very dark gray/near black for text on this surface

    surfaceVariant = Color(0xFFDDE1E6),   // A medium-light gray for variants, keeping distinction
    onSurfaceVariant = Color(0xFF3F4247), // Darker gray text for surface variants

    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    error = Color(0xFFB00020),
    onError = Color.White,
)

// --- Ocean Blue Theme - More Colorful ---
val OceanBlueColorScheme = lightColorScheme(
    primary = Color(0xFF0077C2),       // Bright Blue
    secondary = Color(0xFF00A8E8),     // Lighter Sky Blue
    tertiary = Color(0xFF80DEEA),      // Cyan
    background = Color(0xFFE0F7FA),    // Lighter, less saturated pale blue for background
    surface = Color(0xFFB3E5FC),       // Light Blue surface (more saturated than before)
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF001A29),  // Darker text on pale blue background
    onSurface = Color(0xFF003C5A),     // Dark Blue text on light blue surface for good contrast
    error = Color(0xFFB00020),
    onError = Color.White,
    surfaceVariant = Color(0xFF81D4FA), // Even lighter blue for variants
    onSurfaceVariant = Color(0xFF003350)
)

// --- Forest Green Theme - More Colorful ---
val ForestGreenColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),       // Dark Green
    secondary = Color(0xFF66BB6A),     // Medium Green
    tertiary = Color(0xFFA5D6A7),      // Light Green
    background = Color(0xFFE8F5E9),    // Pale green for background
    surface = Color(0xFFC8E6C9),       // Light Green surface
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF0A1F0B),  // Very Dark Green for text on background
    onSurface = Color(0xFF1B5E20),     // Dark Green text on light green surface
    error = Color(0xFFB00020),
    onError = Color.White,
    surfaceVariant = Color(0xFFA5D6A7), // Lighter green for variants
    onSurfaceVariant = Color(0xFF003300)
)

// --- Sunset Orange Theme - More Colorful ---
val SunsetOrangeColorScheme = lightColorScheme(
    primary = Color(0xFFF57C00),       // Strong Orange
    secondary = Color(0xFFFF9800),     // Lighter Orange (Amber)
    tertiary = Color(0xFFFFB74D),      // Pale Orange
    background = Color(0xFFFFF3E0),    // Pale Orange/Peach for background
    surface = Color(0xFFFFE0B2),       // Light Orange surface
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF331F00),  // Dark Brown for text on background
    onSurface = Color(0xFFBF360C),     // Deep Orange/Brown text on light orange surface
    error = Color(0xFFB00020),
    onError = Color.White,
    surfaceVariant = Color(0xFFFFCC80), // Lighter orange for variants
    onSurfaceVariant = Color(0xFF792F00)
)

// --- Lavender Bliss (Light & Dark) - More Colorful ---
val LightLavenderColorScheme = lightColorScheme(
    primary = Color(0xFF7E57C2),       // Medium Lavender
    secondary = Color(0xFFAB47BC),     // Light Purple
    tertiary = Color(0xFFCE93D8),      // Lighter Lilac
    background = Color(0xFFF3E5F5),    // Pale Lavender for background
    surface = Color(0xFFE1BEE7),       // Light Lilac/Lavender surface
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF25103A),  // Deep Purple for text on background
    onSurface = Color(0xFF4A148C),     // Dark Purple text on lilac surface
    error = Color(0xFFD32F2F),
    onError = Color.White,
    surfaceVariant = Color(0xFFCE93D8), // Lighter lilac for variants
    onSurfaceVariant = Color(0xFF311B92)
)

val DarkLavenderColorScheme = darkColorScheme(
    primary = Color(0xFFB39DDB),       // Light Lavender
    secondary = Color(0xFFCE93D8),     // Lighter Lilac
    tertiary = Color(0xFFE1BEE7),      // Even Lighter Lilac
    background = Color(0xFF1A1120),    // Very Dark Purple background
    surface = Color(0xFF3F2A56),       // Dark Purple surface (more saturated/distinct, will be tinted)
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFEAE6F0),
    onSurface = Color(0xFFE7D2FF),     // Lighter Lavender text on dark purple surface
    error = Color(0xFFEF9A9A),
    onError = Color.Black,
    surfaceVariant = Color(0xFF4F3A65), // Slightly lighter dark purple variant
    onSurfaceVariant = Color(0xFFD0C4E0)
)

// --- Minty Fresh (Light) - More Colorful ---
val MintyFreshColorScheme = lightColorScheme(
    primary = Color(0xFF00A98F),       // Tealish Mint
    secondary = Color(0xFF76D7C4),     // Light Aqua
    tertiary = Color(0xFFA2E8DA),      // Very Light Aqua
    background = Color(0xFFE0F7FA),    // Pale Cyan background
    surface = Color(0xFFB2DFDB),       // Light Teal/Mint surface
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF002A24),  // Dark Teal for text on background
    onSurface = Color(0xFF004D40),     // Dark Teal text on light teal surface
    error = Color(0xFFF44336),
    onError = Color.White,
    surfaceVariant = Color(0xFF80CBC4), // Lighter teal for variants
    onSurfaceVariant = Color(0xFF00382E)
)

// --- Earthy Tones (Light & Dark) - More Colorful ---
val LightEarthyColorScheme = lightColorScheme(
    primary = Color(0xFF8D6E63),       // Brownish Gray
    secondary = Color(0xFFA1887F),     // Lighter Brownish Gray
    tertiary = Color(0xFFBCAAA4),      // Sandy
    background = Color(0xFFF5F3F2),    // Light Beige-Gray background
    surface = Color(0xFFD7CCC8),       // Light Brown/Beige surface (more distinct)
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF291E1A),  // Dark Brown text on background
    onSurface = Color(0xFF4E342E),     // Dark Brown text on light brown surface
    error = Color(0xFFC62828),
    onError = Color.White,
    surfaceVariant = Color(0xFFBCAAA4), // Sandy color for variants
    onSurfaceVariant = Color(0xFF3E2723)
)

val DarkEarthyColorScheme = darkColorScheme(
    primary = Color(0xFFA1887F),       // Lighter Brownish Gray
    secondary = Color(0xFFBCAAA4),     // Sandy
    tertiary = Color(0xFFD7CCC8),      // Light Sandy/Beige
    background = Color(0xFF1E1A18),    // Very Dark Brown background
    surface = Color(0xFF4E342E),       // Dark Brown surface (more distinct, will be tinted)
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFEAE5E3),
    onSurface = Color(0xFFD7CCC8),     // Lighter Beige text on dark brown surface
    error = Color(0xFFE57373),
    onError = Color.Black,
    surfaceVariant = Color(0xFF5D4037), // Slightly lighter dark brown variant
    onSurfaceVariant = Color(0xFFCDC3BF)
)

val CrimsonFocusColorScheme = darkColorScheme(
    primary = Color(0xFFFF5252),       // Vivid Red (Material Red A200) - Main interactive elements
    secondary = Color(0xFFFF1744),     // Even more Vivid/Deeper Red (Material Red A400) - Secondary actions
    tertiary = Color(0xFFFF8A80),      // Softer, but still bright Red (Material Red A100) - Accents
    background = Color(0xFF261313),    // Very Dark, slightly more saturated Red for background
    surface = Color(0xFF4D1A1A),       // Dark, Saturated Red for surfaces (will be tinted by primary)
    onPrimary = Color.White,           // White text on the vivid primary red (better than black here)
    onSecondary = Color.White,         // White text on the vivid secondary red
    onTertiary = Color.Black,          // Black text can work on the A100 red
    onBackground = Color(0xFFFDE5E5),  // Pale Pink/Off-White for text on the dark red background
    onSurface = Color(0xFFFDEAE9),     // Pale Pink/Off-White for text on the dark red surface
    error = Color(0xFFFF5252),         // Using the primary vivid red for error for consistency, or a distinct bright error red.
    // Alt: Color(0xFFFF6E6E) - A slightly different vivid error red
    onError = Color.White,             // White text on the error red
    surfaceVariant = Color(0xFF7A2A2A), // Muted but still rich Dark Red for surface variants
    onSurfaceVariant = Color(0xFFFDDCDC) // Light Pink text on surface variants
)


// --- Sunny Citrus (Light) - More Colorful ---
val SunnyCitrusColorScheme = lightColorScheme(
    primary = Color(0xFFFFC107),       // Amber/Bright Yellow
    secondary = Color(0xFFFFD54F),     // Lighter Yellow
    tertiary = Color(0xFFFFE082),      // Pale Yellow
    background = Color(0xFFFFF9E6),    // Pale Yellow background
    surface = Color(0xFFFFECB3),       // Light Yellow surface
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF2A2A2A),
    onSurface = Color(0xFF795500),     // Brownish/Dark Yellow text on light yellow surface
    error = Color(0xFFD32F2F),
    onError = Color.White,
    surfaceVariant = Color(0xFFFFE082), // Pale yellow for variants
    onSurfaceVariant = Color(0xFF5C4100)
)

// --- Deep Ocean Slate (Dark) - More Colorful Surface ---
val DeepOceanSlateColorScheme = darkColorScheme(
    primary = Color(0xFF4DD0E1),       // Bright Cyan
    secondary = Color(0xFF607D8B),     // Blue Grey
    tertiary = Color(0xFF78909C),      // Lighter Blue Grey
    background = Color(0xFF0F181F),    // Very Dark Blue Slate/Teal background
    surface = Color(0xFF223640),       // Dark Blue Slate surface (more distinct, will be tinted by cyan)
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE7ECEE),
    onSurface = Color(0xFFA6EDFF),     // Lighter Cyan/Off-white text on dark blue surface
    error = Color(0xFFE57373),
    onError = Color.Black,
    surfaceVariant = Color(0xFF304550), // Slightly lighter dark blue slate variant
    onSurfaceVariant = Color(0xFFB0BEC5)
)

// --- Rose Gold Tint (Light) - More Colorful ---
val RoseGoldTintColorScheme = lightColorScheme(
    primary = Color(0xFFBCAAA4),       // Muted Rose Gold
    secondary = Color(0xFFF48FB1),     // Soft Pink
    tertiary = Color(0xFFFFCDD2),      // Lighter Soft Pink
    background = Color(0xFFFCF4F4),    // Pale Pinkish Beige background
    surface = Color(0xFFF8BBD0),       // Light Pink surface
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF3B2423),  // Dark Brown for text on background
    onSurface = Color(0xFF880E4F),     // Dark Pink/Maroon text on light pink surface
    error = Color(0xFFC62828),
    onError = Color.White,
    surfaceVariant = Color(0xFFF48FB1), // Soft pink for variants
    onSurfaceVariant = Color(0xFF5C002A)
)

val TealAndAmberColorScheme = darkColorScheme(
    primary = Color(0xFF4DB6AC),           // Teal 500 (lighter for dark theme primary)
    onPrimary = Color.Black,               // Black text on this Teal

    primaryContainer = Color(0xFF005457),   // Dark Teal for containers
    onPrimaryContainer = Color(0xFF99D8D3), // Light Teal/Cyan text on dark teal container

    secondary = Color(0xFFFFC107),         // Amber 500 (bright accent)
    onSecondary = Color.Black,             // Black text on Amber

    secondaryContainer = Color(0xFF7F6000), // Dark Amber/Brown for secondary containers
    onSecondaryContainer = Color(0xFFFFE082),// Light Amber/Yellow text

    tertiary = Color(0xFF80CBC4),          // Lighter Teal/Aqua for tertiary accents
    onTertiary = Color.Black,

    tertiaryContainer = Color(0xFF004D40),  // Very Dark Teal for tertiary containers
    onTertiaryContainer = Color(0xFFA7FFEB),// Light Aqua text

    background = Color(0xFF101D1C),       // Very Dark Teal/Gray background (not pure black)
    onBackground = Color(0xFFE0E0E0),     // Light gray text

    surface = Color(0xFF1A2C2A),         // Dark Teal/Gray surface (slightly lighter than background)
    onSurface = Color(0xFFD6DFDE),       // Light gray/cyan text

    surfaceVariant = Color(0xFF2E403E),   // Muted Dark Teal for surface variants
    onSurfaceVariant = Color(0xFFB0C9C7), // Muted light teal text

    error = Color(0xFFFF8A65),             // Light Orange/Red for error
    onError = Color.Black,

    errorContainer = Color(0xFF7F341A),     // Dark Orange/Brown for error containers
    onErrorContainer = Color(0xFFFFD5C1),  // Light Orange text

    outline = Color(0xFF5F7D7A),
    outlineVariant = Color(0xFF3F504E)
)

val CharcoalAndGoldDustColorScheme = darkColorScheme(
    primary = Color(0xFFFFEB3B),          // Bright Yellow (Material Yellow 500) - main radiant gold
    onPrimary = Color.Black,              // Black provides good contrast on bright yellow

    primaryContainer = Color(0xFF3E3D00),  // Very Dark, slightly desaturated yellow/olive for contrast
    onPrimaryContainer = Color(0xFFFFFDE7),// Very Pale Yellow (Material Yellow 50)

    secondary = Color(0xFFFFF176),        // Lighter, still bright Yellow (Material Yellow 300)
    onSecondary = Color.Black,

    secondaryContainer = Color(0xFF282800),// Even darker yellow/olive base
    onSecondaryContainer = Color(0xFFFFFFE0),// Light Yellow (Material Yellow A100)

    tertiary = Color(0xFFFFF59D),         // Pale Bright Yellow (Material Yellow 200)
    onTertiary = Color.Black,

    tertiaryContainer = Color(0xFF797400),  // Dark Gold/Ochre
    onTertiaryContainer = Color(0xFFFFFDD0),// Creamy Yellow

    background = Color(0xFF1C1C1A),       // Very dark, warm grey-brown (to make yellow pop)
    onBackground = Color(0xFFFFFDF7),     // Off-White/Very Pale Yellow

    surface = Color(0xFF2E2E2A),          // Dark warm grey-brown surface
    onSurface = Color(0xFFFFF9E0),        // Pale Yellow on surface

    surfaceVariant = Color(0xFF43433F),    // Muted dark warm grey
    onSurfaceVariant = Color(0xFFF0F0D8),  // Muted Pale Yellow

    error = Color(0xFFFF9800),            // Bright Amber/Orange for error (Material Amber 500)
    onError = Color.Black,

    errorContainer = Color(0xFF7F4F00),    // Dark Amber
    onErrorContainer = Color(0xFFFFE0B2),  // Pale Amber

    outline = Color(0xFFA09C5F),          // Muted Gold/Olive
    outlineVariant = Color(0xFF504E2E)     // Dark Muted Gold/Olive
)

@Composable
fun BudgifyTheme(
    appTheme: AppTheme,
    content: @Composable () -> Unit
) {
    // Determina quale ColorScheme usare in base al parametro appTheme
    val colorScheme = when (appTheme) {
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.OCEAN_BLUE -> OceanBlueColorScheme
        AppTheme.FOREST_GREEN -> ForestGreenColorScheme
        AppTheme.SUNSET_ORANGE -> SunsetOrangeColorScheme
        AppTheme.LIGHT_LAVENDER -> LightLavenderColorScheme
        AppTheme.DARK_LAVENDER -> DarkLavenderColorScheme
        AppTheme.MINTY_FRESH -> MintyFreshColorScheme
        AppTheme.LIGHT_EARTHY -> LightEarthyColorScheme
        AppTheme.DARK_EARTHY -> DarkEarthyColorScheme
        AppTheme.CRIMSON_FOCUS -> CrimsonFocusColorScheme
        AppTheme.SUNNY_CITRUS -> SunnyCitrusColorScheme
        AppTheme.DEEP_OCEAN_SLATE -> DeepOceanSlateColorScheme
        AppTheme.ROSE_GOLD_TINT -> RoseGoldTintColorScheme
        AppTheme.TEAL_AND_AMBER -> TealAndAmberColorScheme
        AppTheme.CHARCOAL_AND_GOLD_DUST -> CharcoalAndGoldDustColorScheme
        // Add AppTheme.SYSTEM and other fallback logic if needed
        else -> LightColorScheme // Default fallback
    }
    // Se volessi ancora considerare il tema di sistema come fallback, potresti fare così:
    // val darkTheme = appTheme == AppTheme.DARK || (appTheme == AppTheme.SYSTEM && isSystemInDarkTheme())
    // val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}