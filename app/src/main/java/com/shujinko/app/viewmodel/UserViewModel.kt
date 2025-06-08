package com.shujinko.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.shujinko.app.data.remote.UserService
import com.shujinko.app.utils.TokenStore
import com.shujinko.app.util.provideGoogleSignInOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    private val userService: UserService
) : AndroidViewModel(application) {

    fun deleteUser(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext

            try {
                val token = TokenStore.getAccessToken(context).firstOrNull()

                if (token.isNullOrBlank()) {
                    Log.e("UserViewModel", "❌ 토큰이 없습니다.")
                    withContext(Dispatchers.Main) { onComplete(false) }
                    return@launch
                }

                val response = userService.deleteUser("Bearer $token")

                // regardless of response, clear tokens and Firebase sign-out
                TokenStore.clearAll(context)
                FirebaseAuth.getInstance().signOut()
                GoogleSignIn.getClient(context, provideGoogleSignInOptions(context)).signOut()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("UserViewModel", "✅ 로그아웃 성공 (서버)")
                        onComplete(true)
                    } else {
                        Log.e("UserViewModel", "❌ 서버 로그아웃 실패: ${response.code()}")
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "❌ 로그아웃 예외 발생", e)

                TokenStore.clearAll(context)
                FirebaseAuth.getInstance().signOut()
                GoogleSignIn.getClient(context, provideGoogleSignInOptions(context)).signOut()

                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }
}
