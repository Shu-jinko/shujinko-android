package com.shujinko.app.viewmodel

import android.content.Context
import android.util.Log
import com.shujinko.app.data.remote.AuthService
import com.shujinko.app.utils.TokenStore

class AuthViewModel(private val authService: AuthService) {

    suspend fun login(context: Context, idToken: String, birthday: String? = null): Boolean {
        return try {
            val response = authService.loginWithFirebaseToken(
                mapOf(
                    "idToken" to idToken,
                    "birthday" to (birthday ?: "")
                )
            )
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Log.d("AuthRepo", "AccessToken: ${body.accessToken}")
                Log.d("AuthRepo", "RefreshToken: ${body.refreshToken}")

                TokenStore.saveTokens(context, body.accessToken, body.refreshToken ?: "")
            }

            response.isSuccessful
        } catch (e: Exception) {
            Log.e("AuthRepo", "Login failed", e)
            false
        }
    }

    suspend fun refresh(context: Context, refreshToken: String): Boolean {
        return try {
            val response = authService.refreshAccessToken(mapOf("refreshToken" to refreshToken))
            val body = response.body()

            if (response.isSuccessful && body != null) {
                TokenStore.saveAccessToken(context, body.accessToken)
                Log.d("AuthRepo", "새 accessToken 저장됨")
                true
            } else {
                Log.w("AuthRepo", "refresh 실패, 서버 응답 문제")
                false
            }
        } catch (e: Exception) {
            Log.e("AuthRepo", "Refresh failed", e)
            false
        }
    }
}