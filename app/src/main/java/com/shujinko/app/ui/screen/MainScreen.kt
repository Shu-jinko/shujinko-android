package com.shujinko.app.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shujinko.app.viewmodel.DiaryViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import com.shujinko.app.navigation.DiaryNavHost
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
            composable("diary_entry") {
                DiaryNavHost(
                    diaryViewModel = diaryViewModel,
                    token = token,
                    parentNavController = bottomNavController,
                    shouldNavigateAutomatically = false
                )
            }
            composable("profile") {
                ProfileScreen(
                    navController = bottomNavController
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

                DiaryDeleteScreen(
                    year = year,
                    month = month,
                    day = day,
                    navController = bottomNavController,
                    diaryViewModel = diaryViewModel,
                    token = token
                )
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

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == "write") {
                        navController.navigate("diary_entry") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
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
