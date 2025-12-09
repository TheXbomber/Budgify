package com.example.budgify.network

import com.example.budgify.data.ReceiptRequest
import com.example.budgify.data.TransactionResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ReceiptScanService {
    @POST("scan_receipt") // Replace with your actual server endpoint
    suspend fun scanReceipt(@Body request: ReceiptRequest): TransactionResponse
}