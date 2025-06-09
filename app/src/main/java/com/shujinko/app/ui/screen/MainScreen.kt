package com.shujinko.app.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shujinko.app.navigation.DiaryNavHost
import com.shujinko.app.navigation.HomeNavHost
import com.shujinko.app.ui.theme.*
import com.shujinko.app.viewmodel.DiaryViewModel
import com.shujinko.app.viewmodel.UserViewModel
import java.time.LocalDate

@Composable
fun MainScreen(
    diaryViewModel: DiaryViewModel,
    token: String,
    parentNavController: NavController
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    bottomNavController.navigate("diary_entry") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                containerColor = PrimaryPurple,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .offset(y = 50.dp)
                    .size(72.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "일기 쓰기", modifier = Modifier.size(28.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeNavHost(
                    token = token,
                    diaryViewModel = diaryViewModel,
                    parentNavController = bottomNavController
                )
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
                val userViewModel: UserViewModel = hiltViewModel()

                ProfileScreen(
                    navController = bottomNavController,
                    parentNavController = parentNavController,
                    userViewModel = userViewModel
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
    navController: NavController
) {
    val items = listOf(
        BottomNavItem("home", "홈", Icons.Default.Home),
        BottomNavItem("profile", "내 정보", Icons.Default.Person)
    )
    val currentRoute = currentRoute(navController)

    NavigationBar(

        containerColor = BackgroundLight,
        tonalElevation = 6.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 34.dp),
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) PrimaryPurple else TextPrimary
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontFamily = Pretendard,
                        fontSize = 12.sp,
                        color = if (isSelected) PrimaryPurple else TextPrimary
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        try {
                            navController.navigate(item.route) {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            Log.e("BottomNav", "\uD83D\uDCA5 Navigation error: ${e.message}")
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryPurple,
                    selectedTextColor = PrimaryPurple,
                    indicatorColor = Color.Transparent
                )
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
