package com.shujinko.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.shujinko.app.data.remote.RetrofitClient
import com.shujinko.app.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _account = MutableLiveData<GoogleSignInAccount?>()
    val account: LiveData<GoogleSignInAccount?> = _account

    private val auth = FirebaseAuth.getInstance()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val authRepository = AuthRepository(RetrofitClient.authService)

    fun checkExistingSignIn() {
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())
        _account.value = account
    }

    fun setAccount(account: GoogleSignInAccount?) {
        Log.d("AccountInfo", "email: ${account?.email}, name: ${account?.displayName}")
        _account.value = account
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount, onResult: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val idToken = account.idToken
                    Log.d("Login", "Firebase 인증 성공, idToken: $idToken")
                    _isLoading.value = false
                    onResult(true, idToken) // 서버로 보낼 수 있도록 idToken 전달
                } else {
                    Log.e("Login", "Firebase 인증 실패", task.exception)
                    _isLoading.value = false
                    onResult(false, null)
                }
            }
    }
    fun sendTokenToServer(idToken: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val result = authRepository.login(context, idToken)
            onComplete(result)
        }
    }
    fun tryAutoLogin(onSuccess: () -> Unit, onFail: () -> Unit) {
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch {
            try {
                val refreshToken = com.shujinko.app.utils.TokenStore
                    .getRefreshToken(context)
                    .firstOrNull()

                if (refreshToken.isNullOrBlank()) {
                    Log.d("AutoLogin", "리프레시 토큰 없음")
                    onFail()
                    return@launch
                }

                val result = authRepository.refresh(context, refreshToken)
                if (result) {
                    Log.d("AutoLogin", "자동 로그인 성공")
                    onSuccess()
                } else {
                    Log.d("AutoLogin", "자동 로그인 실패")
                    onFail()
                }
            } catch (e: Exception) {
                Log.e("AutoLogin", "자동 로그인 중 예외 발생", e)
                onFail()
            }
        }
    }
}