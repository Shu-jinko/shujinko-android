package com.shujinko.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shujinko.app.ui.screen.HomeScreen
import com.shujinko.app.ui.screen.LoginScreen
import com.shujinko.app.ui.screen.MainScreen
import com.shujinko.app.ui.screen.MultiImagePickerScreen
import com.shujinko.app.ui.screen.SplashScreen
import com.shujinko.app.ui.screen.UsageStatsScreen
import com.shujinko.app.viewmodel.LoginViewModel

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val MAIN = "main"
    const val HOME = "home"
    const val IMAGE_PICKER = "image_picker"
    const val USAGE_STATS = "usage_stats"
}

@Composable
fun ShujinkoNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(navController)
        }
        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.MAIN) { MainScreen(navController) }
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.IMAGE_PICKER) { MultiImagePickerScreen() }
        composable(Routes.USAGE_STATS) { UsageStatsScreen() }
    }
}
