package com.example.budgify.applicationlogic

import android.graphics.Bitmap
import android.util.Log
import com.example.budgify.data.TransactionResponse
import com.example.budgify.network.RetrofitClient
import com.example.budgify.utils.findDateInText
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import kotlin.math.max

private const val TAG = "ReceiptScanRepository"

class ReceiptScanRepository {

    suspend fun scanReceiptImage(bitmap: Bitmap): Result<TransactionResponse> {
        return try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val byteArray = stream.toByteArray()

            val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("receipt_image", "receipt.jpg", requestBody)

            val serverResponse = RetrofitClient.receiptScanService.scanReceipt(imagePart)

            if (serverResponse.status != "success" || serverResponse.data == null) {
                val errorMessage = serverResponse.error ?: "Failed to process receipt on server."
                return Result.failure(Exception(errorMessage))
            }

            val ocrResponse = serverResponse.data
            if (ocrResponse.isErroredOnProcessing || ocrResponse.parsedResults.isNullOrEmpty()) {
                val errorMessage = ocrResponse.errorMessage?.joinToString() ?: "Failed to parse receipt."
                return Result.failure(Exception(errorMessage))
            }

            val parsedText = ocrResponse.parsedResults[0].parsedText
            val transactionResponse = parseOcrText(parsedText)
            Result.success(transactionResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseOcrText(text: String): TransactionResponse {
        val lines = text.split("\\r?\\n".toRegex())
        var highestAmount = 0.0

        // Regex to find monetary values (supports both . and , as decimal separators)
        val amountPattern = Pattern.compile("(\\d+[,.]\\d{2})")

        for (line in lines) {
            val matcher = amountPattern.matcher(line)
            while (matcher.find()) {
                try {
                    // Normalize the found amount string by replacing comma with a dot
                    val amountString = matcher.group(1)?.replace(',', '.')
                    val currentAmount = amountString?.toDouble() ?: 0.0
                    highestAmount = max(highestAmount, currentAmount)
                } catch (e: NumberFormatException) {
                    // Ignore if parsing fails
                }
            }
        }

        // Find the first non-empty line for the description (usually the store name)
        val description = lines.firstOrNull { it.isNotBlank() }?.trim() ?: "Scanned Receipt"

        // Use the utility to find the date
        val date = findDateInText(text)
        val formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        Log.d(TAG, "Parsed Description: $description")
        Log.d(TAG, "Parsed Amount: $highestAmount")
        Log.d(TAG, "Parsed Date: $formattedDate")

        return TransactionResponse(
            amount = highestAmount,
            description = description,
            date = formattedDate,
            category = "General" // Default category
        )
    }
}