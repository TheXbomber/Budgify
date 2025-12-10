package com.example.budgify.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.budgify.applicationlogic.ReceiptScanRepository
import com.example.budgify.data.TransactionResponse
import kotlin.math.roundToInt

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
        val originalBitmap = if (Build.VERSION.SDK_INT < 28) {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        }

        // Resize the bitmap to ensure it's under the 1MB limit
        val resizedBitmap = scaleBitmap(originalBitmap, 1024)

        val result = receiptScanRepository.scanReceiptImage(resizedBitmap)
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

private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val originalWidth = bitmap.width
    val originalHeight = bitmap.height
    var resizedWidth = originalWidth
    var resizedHeight = originalHeight

    if (originalHeight > maxDimension || originalWidth > maxDimension) {
        if (originalWidth > originalHeight) {
            resizedWidth = maxDimension
            resizedHeight = (resizedWidth * originalHeight / originalWidth.toFloat()).roundToInt()
        } else {
            resizedHeight = maxDimension
            resizedWidth = (resizedHeight * originalWidth / originalHeight.toFloat()).roundToInt()
        }
    } else {
        return bitmap // No need to scale
    }

    return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, true)
}
