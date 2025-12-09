package com.example.budgify.applicationlogic

import android.graphics.Bitmap
import android.util.Base64
import com.example.budgify.data.ReceiptRequest
import com.example.budgify.data.TransactionResponse
import com.example.budgify.network.RetrofitClient
import java.io.ByteArrayOutputStream

class ReceiptScanRepository {

    suspend fun scanReceiptImage(bitmap: Bitmap): Result<TransactionResponse> {
        return try {
            val imageData = bitmapToBase64(bitmap)
            val request = ReceiptRequest(imageData)
            val response = RetrofitClient.receiptScanService.scanReceipt(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        // Compress the image. You might want to adjust quality or format.
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}