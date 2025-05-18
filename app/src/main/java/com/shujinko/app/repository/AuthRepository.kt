package com.shujinko.app.repository

import android.content.Context
import android.util.Log
import com.shujinko.app.data.remote.AuthService
import com.shujinko.app.utils.TokenStore

class AuthRepository(private val authService: AuthService) {
    suspend fun login(context: Context, idToken: String): Boolean {
        return try {
            val response = authService.loginWithFirebaseToken(mapOf("idToken" to idToken))
            val jwt = response.body()?.token

            if (response.isSuccessful && jwt != null) {
                Log.d("AuthRepo", "JWT: $jwt")
                TokenStore.saveToken(context, jwt)
            }

            response.isSuccessful
        } catch (e: Exception) {
            Log.e("AuthRepo", "Login failed", e)
            false
        }
    }
}