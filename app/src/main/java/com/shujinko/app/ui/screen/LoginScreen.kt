package com.shujinko.app.ui.screen

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.shujinko.app.ui.theme.Pretendard
import com.shujinko.app.ui.theme.PrimaryPurple
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

            viewModel.firebaseAuthWithGoogle(account) { success, idToken, authCode ->
                if (success && idToken != null && authCode != null) {
                    viewModel.fetchBirthday(account) { parsedBirthday ->
                        viewModel.sendTokenToServer(idToken, authCode, parsedBirthday) { serverResult ->
                            if (serverResult) {
                                viewModel.syncGoogleCalendar()
                                onLoginSuccess()
                            }
                            else Log.e("Login", "서버 로그인 실패")
                        }
                    }
                } else {
                    Log.e("Login", "Firebase 인증 실패 또는 토큰 누락")
                }
            }

        } catch (e: ApiException) {
            Log.w("Login", "signInResult:failed code=${e.statusCode}")
        }
    }

    val isLoading by viewModel.isLoading.observeAsState(false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "슈진코에 오신 걸 환영합니다",
            fontSize = 24.sp,
            fontFamily = Pretendard,
            color = PrimaryPurple
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator(color = PrimaryPurple)
        } else {
            Button(
                onClick = {
                    val signInIntent = signInClient.signInIntent
                    launcher.launch(signInIntent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = CircleShape,
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Google 계정으로 로그인",
                    fontFamily = Pretendard,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}


