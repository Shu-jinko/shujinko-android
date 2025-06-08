package com.shujinko.app.ui.screen

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.shujinko.app.util.provideGoogleSignInOptions
import com.shujinko.app.viewmodel.LoginViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: () -> Unit) {
    val context = LocalContext.current

    val gso = remember { provideGoogleSignInOptions(context) }
    val signInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.setAccount(account)

            viewModel.firebaseAuthWithGoogle(account) { success, idToken ->
                if (success && idToken != null) {
                    Log.d("Login", "idToken = $idToken")

                    // ✅ 생일 먼저 받아오기
                    viewModel.fetchBirthday(account) { parsedBirthday ->
                        Log.d("Birthday", "🎂 생일 데이터: $parsedBirthday")

                        // ✅ 생일과 함께 서버 로그인 요청
                        viewModel.sendTokenToServer(idToken, parsedBirthday) { serverResult ->
                            if (serverResult) {
                                onLoginSuccess()
                            } else {
                                Log.e("Login", "서버 로그인 실패")
                            }
                        }
                    }
                } else {
                    Log.e("Login", "Firebase 인증 실패 또는 idToken 없음")
                }
            }
        } catch (e: ApiException) {
            Log.w("Login", "signInResult:failed code=${e.statusCode}")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "슈진코에 오신 걸 환영합니다", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        val isLoading by viewModel.isLoading.observeAsState(false)

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = {
                val signInIntent = signInClient.signInIntent
                launcher.launch(signInIntent)
            }) {
                Text("Google 계정으로 로그인")
            }
        }
    }
}


