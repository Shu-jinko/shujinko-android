package com.shujinko.app.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.shujinko.app.viewmodel.DiaryViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import java.time.LocalDate

@Composable
fun MainScreen(
    diaryViewModel: DiaryViewModel,
    token: String
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
                token = token,
                diaryViewModel = diaryViewModel
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen()
            }
            composable("diary_write") {
                DiaryWriteScreen(
                    navController = bottomNavController,
                    diaryViewModel = diaryViewModel,
                    token = token,
                    isEditMode = false,
                    initialText = "",
                    diaryId = null
                )
            }
            composable("diary_edit/{id}/{year}/{month}/{day}/{rawDiary}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
                val year = backStackEntry.arguments?.getString("year")?.toIntOrNull() ?: return@composable
                val month = backStackEntry.arguments?.getString("month")?.toIntOrNull() ?: return@composable
                val day = backStackEntry.arguments?.getString("day")?.toIntOrNull() ?: return@composable
                val rawDiary = backStackEntry.arguments?.getString("rawDiary") ?: ""

                DiaryWriteScreen(
                    navController = bottomNavController,
                    diaryViewModel = diaryViewModel,
                    token = token,
                    isEditMode = true,
                    initialText = rawDiary,
                    diaryId = id
                )
            }
            composable("diary_result/{year}/{month}/{day}") { backStackEntry ->
                val year = backStackEntry.arguments?.getString("year")?.toIntOrNull() ?: return@composable
                val month = backStackEntry.arguments?.getString("month")?.toIntOrNull() ?: return@composable
                val day = backStackEntry.arguments?.getString("day")?.toIntOrNull() ?: return@composable

                DiaryResultScreen(
                    year = year,
                    month = month,
                    day = day,
                    token = token,
                    diaryViewModel = diaryViewModel,
                    navController = bottomNavController
                )
            }
            composable("profile") {
                ProfileScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    token: String,
    diaryViewModel: DiaryViewModel
) {
    val items = listOf(
        BottomNavItem("home", "홈", Icons.Default.Home),
        BottomNavItem("write", "쓰기", Icons.Default.Edit),
        BottomNavItem("profile", "내 정보", Icons.Default.Person)
    )

    val currentRoute = currentRoute(navController)
    val today = LocalDate.now()

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == "write") {
                        val today = LocalDate.now()
                        diaryViewModel.getDiary(token, today.year, today.monthValue, today.dayOfMonth) { exists, diary ->
                            if (exists && diary != null) {
                                // 일기 있으면 결과 화면으로 이동
                                navController.navigate("diary_result/${today.year}/${today.monthValue}/${today.dayOfMonth}")
                            } else {
                                // 없으면 작성 화면으로 이동
                                navController.navigate("diary_write")
                            }
                        }
                    } else {
                        navController.navigate(item.route) {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)
