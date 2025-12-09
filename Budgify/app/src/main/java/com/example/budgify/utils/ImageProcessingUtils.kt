package com.example.budgify.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.applicationlogic.ReceiptScanRepository
import com.example.budgify.data.TransactionResponse
import com.example.budgify.entities.CategoryType
import com.example.budgify.entities.TransactionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

suspend fun processImageForReceipt(
    imageUri: Uri,
    context: Context,
    viewModel: FinanceViewModel,
    receiptScanRepository: ReceiptScanRepository,
    onLoading: (Boolean) -> Unit,
    onSuccess: (TransactionResponse) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)
    try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        }

        receiptScanRepository.scanReceiptImage(bitmap)
            .onSuccess { transactionResponse ->
                onSuccess(transactionResponse)
            }
            .onFailure { e ->
                onError("Failed to scan receipt: ${e.message ?: "Unknown error"}")
            }
    } catch (e: Exception) {
        onError("Failed to load image: ${e.message ?: "Unknown error"}")
    } finally {
        onLoading(false)
    }
}

// Helper to parse date from TransactionResponse
fun parseTransactionDate(dateString: String): LocalDate? {
    return try {
        LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: DateTimeParseException) {
        println("Error parsing date: ${e.message}")
        null
    }
}

// Helper to map category type from parsed response
fun mapCategoryToTransactionType(categoryType: CategoryType): TransactionType {
    return when (categoryType) {
        CategoryType.EXPENSE -> TransactionType.EXPENSE
        CategoryType.INCOME -> TransactionType.INCOME
    }
}
