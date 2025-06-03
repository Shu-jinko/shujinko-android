package com.shujinko.app.data.remote

import com.shujinko.app.data.Item.AuthItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/auth/login")
    suspend fun loginWithFirebaseToken(@Body body: Map<String, String>): Response<AuthItem>

    @POST("/auth/refresh")
    suspend fun refreshAccessToken(@Body body: Map<String, String>): Response<AuthItem>
}