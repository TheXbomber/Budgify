package com.example.budgify.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// This class matches the structure of the JSON returned by your Python server
@JsonClass(generateAdapter = true)
data class ServerResponse(
    val status: String,
    val data: OcrResponse?, // Make data nullable in case of an error status
    val error: String?,
    val details: List<String>?
)

@JsonClass(generateAdapter = true)
data class OcrResponse(
    @Json(name = "ParsedResults")
    val parsedResults: List<ParsedResult>?,
    @Json(name = "IsErroredOnProcessing")
    val isErroredOnProcessing: Boolean,
    @Json(name = "ErrorMessage")
    val errorMessage: List<String>?
)

@JsonClass(generateAdapter = true)
data class ParsedResult(
    @Json(name = "ParsedText")
    val parsedText: String
)