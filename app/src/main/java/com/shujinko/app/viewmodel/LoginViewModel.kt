package com.shujinko.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.shujinko.app.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _account = MutableLiveData<GoogleSignInAccount?>()
    val account: LiveData<GoogleSignInAccount?> = _account

    private val auth = FirebaseAuth.getInstance()
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val authRepository = AuthViewModel(RetrofitClient.authService)

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
                    val idToken = account.idToken
                    Log.d("Login", "Firebase 인증 성공, idToken: $idToken")
                    _isLoading.value = false
                    onResult(true, idToken)
                } else {
                    Log.e("Login", "Firebase 인증 실패", task.exception)
                    _isLoading.value = false
                    onResult(false, null)
                }
            }
    }

    // ✅ 생일도 함께 보내도록 수정
    fun sendTokenToServer(idToken: String, birthday: String?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val result = authRepository.login(context, idToken, birthday)
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

    fun fetchBirthday(account: GoogleSignInAccount, onResult: (String?) -> Unit) {
        val context = getApplication<Application>().applicationContext
        val scope = "oauth2:https://www.googleapis.com/auth/user.birthday.read"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val googleAccount = account.account
                if (googleAccount == null) {
                    Log.e("Birthday", "account.account is null")
                    withContext(Dispatchers.Main) { onResult(null) }
                    return@launch
                }

                val token = GoogleAuthUtil.getToken(context, googleAccount, scope)
                val url = URL("https://people.googleapis.com/v1/people/me?personFields=birthdays")
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("Authorization", "Bearer $token")

                val response = connection.inputStream.bufferedReader().readText()
                Log.d("Birthday", "받은 생일 응답: $response")

                val parsedBirthday = parseBirthday(response)

                withContext(Dispatchers.Main) {
                    onResult(parsedBirthday)
                }
            } catch (e: Exception) {
                Log.e("Birthday", "생일 정보 가져오기 실패", e)
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    private fun parseBirthday(json: String?): String? {
        return try {
            val obj = org.json.JSONObject(json ?: return null)
            val birthdays = obj.getJSONArray("birthdays")
            for (i in 0 until birthdays.length()) {
                val birthdayObj = birthdays.getJSONObject(i)
                val dateObj = birthdayObj.getJSONObject("date")
                val year = dateObj.optInt("year", -1)
                val month = dateObj.getInt("month")
                val day = dateObj.getInt("day")

                return if (year != -1) {
                    "%04d-%02d-%02d".format(year, month, day)
                } else {
                    null
                }
            }
            null
        } catch (e: Exception) {
            Log.e("Birthday", "파싱 실패", e)
            null
        }
    }
}
