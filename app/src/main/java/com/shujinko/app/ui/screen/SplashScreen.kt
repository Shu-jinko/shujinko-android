package com.shujinko.app.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shujinko.app.ui.components.LoadingIndicator
import com.shujinko.app.viewmodel.LoginViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    LaunchedEffect(true) {
        delay(500)

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
        LoadingIndicator(message = "일기 작성 중...")
    }
}