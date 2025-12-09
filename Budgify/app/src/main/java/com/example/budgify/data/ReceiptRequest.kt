package com.example.budgify.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReceiptRequest(
    val imageData: String // Base64 encoded image string
)