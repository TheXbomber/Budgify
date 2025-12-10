package com.example.budgify.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TransactionResponse(
    val amount: Double,
    val description: String,
    val date: String, // Or a more specific date type
    val category: String
    // Add other fields as per your server's response
)