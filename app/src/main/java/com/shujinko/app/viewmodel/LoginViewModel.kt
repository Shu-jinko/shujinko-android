package com.shujinko.app.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.shujinko.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _account = MutableLiveData<GoogleSignInAccount?>()
    val account: LiveData<GoogleSignInAccount?> = _account

    private val auth = FirebaseAuth.getInstance()
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun checkExistingSignIn() {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        _account.value = account
    }

    fun setAccount(account: GoogleSignInAccount?) {
        Log.d("AccountInfo", "email: ${account?.email}, name: ${account?.displayName}")
        _account.value = account
    }

    // ✅ 콜백 시그니처 변경: authCode도 같이 넘기기
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount, onResult: (Boolean, String?, String?) -> Unit) {
        _isLoading.value = true
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val idToken = account.idToken
                    val authCode = account.serverAuthCode  // ✅ Calendar API용
                    Log.d("Login", "Firebase 인증 성공, idToken: $idToken, authCode: $authCode")
                    _isLoading.value = false
                    onResult(true, idToken, authCode) // ✅ authCode 전달
                } else {
                    Log.e("Login", "Firebase 인증 실패", task.exception)
                    _isLoading.value = false
                    onResult(false, null, null) // ✅ 실패 시 null
                }
            }
    }


    // ✅ authCode 인자로 추가
    fun sendTokenToServer(idToken: String, authCode: String?, birthday: String?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.login(context, idToken, authCode, birthday) // ✅ 수정됨
            onComplete(result)
        }
    }


    fun tryAutoLogin(onSuccess: () -> Unit, onFail: () -> Unit) {
        viewModelScope.launch {
            val result = authRepository.refresh(context)
            if (result) {
                Log.d("AutoLogin", "자동 로그인 성공")
                onSuccess()
            } else {
                Log.d("AutoLogin", "자동 로그인 실패")
                onFail()
            }
        }
    }

    fun fetchBirthday(account: GoogleSignInAccount, onResult: (String?) -> Unit) {
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
