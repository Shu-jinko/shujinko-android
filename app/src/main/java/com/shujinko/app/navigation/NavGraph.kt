package com.shujinko.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shujinko.app.ui.screen.HomeScreen
import com.shujinko.app.ui.screen.MultiImagePickerScreen

object Routes {
    const val HOME = "home"
    const val IMAGE_PICKER = "image_picker"
}

@Composable
fun ShujinkoNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.IMAGE_PICKER) { MultiImagePickerScreen() }
    }
}
