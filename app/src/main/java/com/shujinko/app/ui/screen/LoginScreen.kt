package com.shujinko.app.ui.screen

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.shujinko.app.util.provideGoogleSignInOptions
import com.shujinko.app.viewmodel.LoginViewModel
import com.shujinko.app.R
import com.shujinko.app.ui.components.LoadingIndicator

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

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_screen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(250.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }

            } else {
                Image(
                    painter = painterResource(id = R.drawable.login_button),
                    contentDescription = null,
                    modifier = Modifier
                        .width(110.dp)
                        .height(70.dp)
                        .clickable { launcher.launch(signInClient.signInIntent) }
                )

            }
        }
    }
}


