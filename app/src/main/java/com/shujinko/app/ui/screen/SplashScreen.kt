package com.shujinko.app.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shujinko.app.viewmodel.LoginViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(true) {
        delay(500) // UX용 약간의 지연

        loginViewModel.tryAutoLogin(
            onSuccess = {
                Log.d("Splash", "자동 로그인 성공 → 메인 이동")

                loginViewModel.syncGoogleCalendar()

                navController.navigate("main") {
                    popUpTo("splash") { inclusive = true }
                }
            },
            onFail = {
                Log.d("Splash", "자동 로그인 실패 → 로그인 이동")
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Shujinko", fontSize = 30.sp)
            Spacer(modifier = Modifier.height(20.dp))
            CircularProgressIndicator()
        }
    }
}
