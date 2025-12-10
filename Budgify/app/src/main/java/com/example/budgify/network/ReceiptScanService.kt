package com.example.budgify.network

import com.example.budgify.data.ServerResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ReceiptScanService {
    @Multipart
    @POST("scan_receipt")
    suspend fun scanReceipt(@Part image: MultipartBody.Part): ServerResponse
}