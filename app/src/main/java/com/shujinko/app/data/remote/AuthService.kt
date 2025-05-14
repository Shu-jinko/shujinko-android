package com.shujinko.app.data.remote

import com.google.android.gms.common.api.Response

interface AuthService {
    @POST("api/auth/login")
    suspend fun loginWithFirebaseToken(@Body body: Map<String, String>): Response<AuthResponse>
}