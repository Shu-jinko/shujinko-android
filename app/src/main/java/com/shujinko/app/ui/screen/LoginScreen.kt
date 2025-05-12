package com.shujinko.app.ui.screen

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.shujinko.app.viewmodel.LoginViewModel
import com.shujinko.app.R

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: () -> Unit) {
    val context = LocalContext.current

    // Google Sign-In Options 설정
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    val signInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Activity Result Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.setAccount(account)
            viewModel.firebaseAuthWithGoogle(account) { success ->
                if (success) {
                    onLoginSuccess()
                } else {
                    Log.e("Login", "Firebase 인증 실패")
                }
            }
        } catch (e: ApiException) {
            Log.w("Login", "signInResult:failed code=${e.statusCode}")
        }
    }

    // UI 구성
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "슈진코에 오신 걸 환영합니다", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val signInIntent = signInClient.signInIntent
            launcher.launch(signInIntent)
        }) {
            Text("Google 계정으로 로그인")
        }
    }
}