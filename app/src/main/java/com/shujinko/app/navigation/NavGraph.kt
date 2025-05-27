package com.shujinko.app.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shujinko.app.ui.screen.*
import com.shujinko.app.viewmodel.DiaryViewModel
import com.shujinko.app.viewmodel.LoginViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun ShujinkoNavGraph(
    navController: NavHostController,
    token: String
) {
    NavHost(navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Main.route) {
            val diaryViewModel: DiaryViewModel = hiltViewModel()
            MainScreen(
                diaryViewModel = diaryViewModel,
                token = token
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.ImagePicker.route) {
            MultiImagePickerScreen()
        }
        composable(Screen.UsageStats.route) {
            UsageStatsScreen()
        }
        composable(Screen.DiaryWrite.route) {
            val diaryViewModel: DiaryViewModel = hiltViewModel()
            DiaryWriteScreen(navController, diaryViewModel, token)
        }
        composable(Screen.DiaryResult.route) {
            DiaryResultScreen()
        }
    }
}