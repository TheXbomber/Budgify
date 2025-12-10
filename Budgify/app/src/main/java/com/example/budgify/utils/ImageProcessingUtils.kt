package com.example.budgify.utils

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.budgify.applicationlogic.ReceiptScanRepository
import com.example.budgify.data.TransactionResponse

suspend fun processImageForReceipt(
    imageUri: Uri,
    context: Context,
    receiptScanRepository: ReceiptScanRepository,
    onLoading: (Boolean) -> Unit,
    onSuccess: (TransactionResponse) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)
    try {
        val bitmap = if (Build.VERSION.SDK_INT < 28) {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        }

        val result = receiptScanRepository.scanReceiptImage(bitmap)
        result.onSuccess {
            onSuccess(it)
        }.onFailure {
            onError(it.message ?: "Failed to scan receipt")
        }
    } catch (e: Exception) {
        onError("Failed to process image: ${e.message}")
    } finally {
        onLoading(false)
    }
}