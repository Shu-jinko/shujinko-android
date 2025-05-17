package com.shujinko.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _account = MutableLiveData<GoogleSignInAccount?>()
    val account: LiveData<GoogleSignInAccount?> = _account

    private val auth = FirebaseAuth.getInstance()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

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
                    onResult(true, idToken) // 👈 서버로 보낼 수 있도록 idToken 전달
                } else {
                    Log.e("Login", "Firebase 인증 실패", task.exception)
                    _isLoading.value = false
                    onResult(false, null)
                }
            }
    }
}