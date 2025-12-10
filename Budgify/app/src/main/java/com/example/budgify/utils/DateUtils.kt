package com.example.budgify.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun parseTransactionDate(dateString: String?): LocalDate {
    if (dateString.isNullOrBlank()) {
        return LocalDate.now()
    }

    // Add more date formats if needed
    val formatters = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("MM-dd-yyyy")
    )

    for (formatter in formatters) {
        try {
            return LocalDate.parse(dateString, formatter)
        } catch (e: DateTimeParseException) {
            // Continue to the next formatter
        }
    }

    // Return current date if no format matches
    return LocalDate.now()
}