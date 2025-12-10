package com.example.budgify.utils

import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private const val TAG = "DateUtils"

fun findDateInText(text: String): LocalDate {
    val lines = text.split("\r?\n".toRegex())

    // Ordered list of regex patterns and corresponding formatters
    // We try to match more specific/common patterns first.
    val datePatterns = listOf(
        // YYYY-MM-DD
        """\b\d{4}-\d{2}-\d{2}\b""".toRegex() to DateTimeFormatter.ISO_LOCAL_DATE,
        // DD/MM/YYYY, D/M/YYYY, DD/M/YYYY, D/MM/YYYY
        """\b\d{1,2}/\d{1,2}/\d{4}\b""".toRegex() to DateTimeFormatter.ofPattern("d/M/yyyy"),
        // DD-MM-YYYY, D-M-YYYY, DD-M-YYYY, D-MM-YYYY
        """\b\d{1,2}-\d{1,2}-\d{4}\b""".toRegex() to DateTimeFormatter.ofPattern("d-M/yyyy"),
        // MM/DD/YYYY, M/D/YYYY, MM/D/YYYY, M/DD/YYYY
        // Note: This is after dd/MM/yyyy to prioritize DD-MM formats if ambiguity exists
        """\b\d{1,2}/\d{1,2}/\d{4}\b""".toRegex() to DateTimeFormatter.ofPattern("M/d/yyyy"),

        // DD.MM.YYYY, D.M.YYYY etc.
        """\b\d{1,2}\.\d{1,2}\.\d{4}\b""".toRegex() to DateTimeFormatter.ofPattern("d.M.yyyy"),

        // Month Name (MMM or MMMM) DD, YYYY (e.g., Jan 01, 2025 or January 01, 2025)
        """\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{1,2},\s+\d{4}\b""".toRegex() to DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
        """\b\d{1,2}\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{4}\b""".toRegex() to DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),

        // MM/DD/YY or DD/MM/YY (two-digit year)
        """\b\d{1,2}/\d{1,2}/\d{2}\b""".toRegex() to DateTimeFormatter.ofPattern("d/M/yy"),
        """\b\d{1,2}-\d{1,2}-\d{2}\b""".toRegex() to DateTimeFormatter.ofPattern("d-M-yy")
    )

    for (line in lines) {
        for ((pattern, formatter) in datePatterns) {
            pattern.find(line)?.let { matchResult ->
                val dateString = matchResult.value
                try {
                    val parsedDate = LocalDate.parse(dateString, formatter)
                    Log.d(TAG, "Successfully parsed date '$dateString' with pattern '${pattern.pattern}' to $parsedDate")
                    return parsedDate
                } catch (e: DateTimeParseException) {
                    Log.d(TAG, "Failed to parse date '$dateString' with pattern '${pattern.pattern}': ${e.message}")
                    // Continue to the next formatter/pattern
                }
            }
        }
    }

    Log.d(TAG, "No date found in text. Defaulting to today.")
    return LocalDate.now() // Default to today if no date is found
}

// This function is used by AddTransactionDialog to format the date for display
fun parseTransactionDate(dateString: String?): LocalDate {
    if (dateString.isNullOrBlank()) {
        return LocalDate.now()
    }
    // This formatter must match the output format of the date from findDateInText
    return try {
        LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: DateTimeParseException) {
        Log.e(TAG, "Error parsing transaction date string for display: $dateString", e)
        LocalDate.now()
    }
}