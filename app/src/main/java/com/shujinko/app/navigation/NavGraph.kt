package com.shujinko.app.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
        composable(Screen.DiaryWrite.route) {
            val diaryViewModel: DiaryViewModel = hiltViewModel()
            DiaryWriteScreen(
                navController = navController,
                diaryViewModel = diaryViewModel,
                token = token,
                isEditMode = false,
                initialText = "",
                diaryId = null
            )
        }
        composable("diary_edit/{id}/{year}/{month}/{day}/{rawDiary}") { backStackEntry ->
            val diaryViewModel: DiaryViewModel = hiltViewModel()
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
            val year = backStackEntry.arguments?.getString("year")?.toIntOrNull() ?: return@composable
            val month = backStackEntry.arguments?.getString("month")?.toIntOrNull() ?: return@composable
            val day = backStackEntry.arguments?.getString("day")?.toIntOrNull() ?: return@composable
            val rawDiary = backStackEntry.arguments?.getString("rawDiary") ?: ""

            DiaryWriteScreen(
                navController = navController,
                diaryViewModel = diaryViewModel,
                token = token,
                isEditMode = true,
                initialText = java.net.URLDecoder.decode(rawDiary, "UTF-8"),
                diaryId = id
            )
        }
        composable(
            route = "delete_diary/{year}/{month}/{day}",
            arguments = listOf(
                navArgument("year") { type = NavType.IntType },
                navArgument("month") { type = NavType.IntType },
                navArgument("day") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val year = backStackEntry.arguments?.getInt("year") ?: return@composable
            val month = backStackEntry.arguments?.getInt("month") ?: return@composable
            val day = backStackEntry.arguments?.getInt("day") ?: return@composable

            val diaryViewModel: DiaryViewModel = hiltViewModel()
            val token = token

                DiaryDeleteScreen(
                    year = year,
                    month = month,
                    day = day,
                    navController = navController,
                    diaryViewModel = diaryViewModel,
                    token = token
                )
        }
    }
}