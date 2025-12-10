package com.example.budgify.utils

import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private const val TAG = "DateUtils"

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