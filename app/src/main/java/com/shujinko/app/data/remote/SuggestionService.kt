package com.shujinko.app.data.remote

import com.shujinko.app.data.Item.SuggestionRequest
import com.shujinko.app.data.Item.SuggestionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SuggestionService {
    @POST("suggestion")
    suspend fun createSuggestion(
        @Header("Authorization") token: String,
        @Body request: SuggestionRequest
    ): Response<SuggestionResponse>
}