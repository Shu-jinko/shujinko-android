package com.shujinko.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.shujinko.app.data.Item.UpdateUserRequest
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

    fun fetchUserInfo(onResult: (email: String?, name: String?, birthday: String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext

            try {
                val token = TokenStore.getAccessToken(context).firstOrNull()

                if (token.isNullOrBlank()) {
                    Log.e("UserViewModel", "❌ 토큰이 없습니다.")
                    withContext(Dispatchers.Main) { onResult(null, null, null) }
                    return@launch
                }

                val response = userService.getUserInfo("Bearer $token")

                if (response.isSuccessful) {
                    val user = response.body()
                    Log.d("UserViewModel", "✅ 사용자 정보: $user")
                    withContext(Dispatchers.Main) {
                        onResult(user?.email, user?.name, user?.birthday)
                    }
                } else {
                    Log.e("UserViewModel", "❌ 사용자 정보 불러오기 실패: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        onResult(null, null, null)
                    }
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "❌ 사용자 정보 불러오기 예외", e)
                withContext(Dispatchers.Main) {
                    onResult(null, null, null)
                }
            }
        }
    }

    fun updateUserInfo(name: String, birthday: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            try {
                val token = TokenStore.getAccessToken(context).firstOrNull()
                if (token.isNullOrBlank()) {
                    Log.e("UserViewModel", "❌ 토큰 없음")
                    withContext(Dispatchers.Main) { onResult(false) }
                    return@launch
                }

                val request = UpdateUserRequest(name, birthday)
                val response = userService.updateUserInfo("Bearer $token", request)

                if (response.isSuccessful) {
                    Log.d("UserViewModel", "✅ 사용자 정보 수정 성공")
                    withContext(Dispatchers.Main) { onResult(true) }
                } else {
                    Log.e("UserViewModel", "❌ 수정 실패: ${response.code()}")
                    withContext(Dispatchers.Main) { onResult(false) }
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "❌ 사용자 정보 수정 예외", e)
                withContext(Dispatchers.Main) { onResult(false) }
            }
        }
    }

}