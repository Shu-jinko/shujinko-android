//package com.shujinko.app.repository
//
//import android.util.Log
//import com.shujinko.app.data.remote.AuthService
//
//class AuthRepository(private val authService: AuthService) {
//    suspend fun login(idToken: String): Boolean {
//        return try {
//            val response = authService.loginWithFirebaseToken(mapOf("idToken" to idToken))
//            response.isSuccessful // 필요시 response.body()로 JWT 등도 저장
//        } catch (e: Exception) {
//            Log.e("AuthRepo", "Login failed", e)
//            false
//        }
//    }
//}
