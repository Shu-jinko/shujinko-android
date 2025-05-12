package com.shujinko.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import androidx.compose.ui.platform.LocalContext

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _account = MutableLiveData<GoogleSignInAccount?>()
    val account: LiveData<GoogleSignInAccount?> = _account

    fun checkExistingSignIn() {
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())
        _account.value = account
    }

    fun setAccount(account: GoogleSignInAccount?) {
        Log.d("AccountInfo", "email: ${account?.email}, name: ${account?.displayName}")
        _account.value = account
    }

    @Composable
    fun getSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount, onResult: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    private fun saveUserToFirestore(user: FirebaseUser?, onComplete: (Boolean) -> Unit) {
        user ?: return onComplete(false)
        val db = FirebaseFirestore.getInstance()
        val userInfo = mapOf(
            "uid" to user.uid,
            "email" to user.email,
            "name" to user.displayName,
            "photoUrl" to user.photoUrl?.toString(),
            "createdAt" to Timestamp.now()
        )

        db.collection("users").document(user.uid)
            .set(userInfo)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

}
