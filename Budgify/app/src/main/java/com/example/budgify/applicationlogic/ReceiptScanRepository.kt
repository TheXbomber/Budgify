package com.example.budgify.applicationlogic

import android.graphics.Bitmap
import com.example.budgify.data.OcrResponse
import com.example.budgify.data.TransactionResponse
import com.example.budgify.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern

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
        // This is a very basic implementation. You will likely need to make this more robust.
        val lines = text.split("\\r?\\n".toRegex())

        var description = "Scanned Receipt"
        var amount = 0.0
        var date = ""
        val category = "General" // Default category

        // Simple logic to find total amount
        val totalPattern = Pattern.compile("(?i)(total|amount|\\s)\\s*\\$?(\\d+\\.\\d{2})")
        for (line in lines) {
            val matcher = totalPattern.matcher(line)
            if (matcher.find()) {
                try {
                    amount = matcher.group(2)?.toDouble() ?: 0.0
                } catch (e: NumberFormatException) {
                    // Ignore
                }
            }
        }
        
        // You can add more logic here to find description and date
        if (lines.isNotEmpty()) {
            description = lines[0] // Use the first line as a description
        }


        return TransactionResponse(
            amount = amount,
            description = description,
            date = date, // You need to implement date parsing
            category = category
        )
    }
}