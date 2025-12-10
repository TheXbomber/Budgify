package com.example.budgify.applicationlogic

import android.graphics.Bitmap
import android.util.Log
import com.example.budgify.data.TransactionResponse
import com.example.budgify.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

private const val TAG = "ReceiptScanRepository"

class ReceiptScanRepository {

    suspend fun scanReceiptImage(bitmap: Bitmap): Result<TransactionResponse> {
        return try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val byteArray = stream.toByteArray()

            val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("receipt_image", "receipt.jpg", requestBody)

            val transactionResponse = RetrofitClient.receiptScanService.scanReceipt(imagePart)

            Log.d(TAG, "Received Parsed Transaction from Server: $transactionResponse")

            Result.success(transactionResponse)
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning receipt image", e)
            Result.failure(e)
        }
    }
}