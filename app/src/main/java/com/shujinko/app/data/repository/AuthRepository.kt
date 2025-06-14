    package com.shujinko.app.data.repository
    
    import android.content.Context
    import com.shujinko.app.data.remote.AuthService
    import com.shujinko.app.utils.TokenStore
    import kotlinx.coroutines.flow.firstOrNull
    import javax.inject.Inject

    class AuthRepository @Inject constructor(
        private val authService: AuthService
    ) {
        suspend fun login(context: Context, idToken: String, authCode: String?, birthday: String?): Boolean {
            return try {
                val requestBody = mapOf(
                    "idToken" to idToken,
                    "birthday" to (birthday ?: ""),
                    "accessToken" to (authCode ?: "") // ✅ authCode를 accessToken으로 보냄
                )

                val response = authService.loginWithFirebaseToken(requestBody)
                val body = response.body()

                if (response.isSuccessful && body != null) {
                    TokenStore.saveTokens(context, body.accessToken, body.refreshToken ?: "")
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }

        suspend fun refresh(context: Context): Boolean {
            val refreshToken = TokenStore.getRefreshToken(context).firstOrNull() ?: return false
            return try {
                val response = authService.refreshAccessToken(mapOf("refreshToken" to refreshToken))
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    TokenStore.saveAccessToken(context, body.accessToken)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }

