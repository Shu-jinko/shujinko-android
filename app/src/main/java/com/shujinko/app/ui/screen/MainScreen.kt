package com.shujinko.app.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Spacer
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
                    .offset(y = 53  .dp)
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
        NavigationBarItem(
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "홈",
                    tint = if (currentRoute == "home") PrimaryPurple else TextPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            label = {
                Text(
                    "홈",
                    fontFamily = Pretendard,
                    fontSize = 12.sp,
                    color = if (currentRoute == "home") PrimaryPurple else TextPrimary
                )
            },
            selected = currentRoute == "home",
            onClick = {
                if (currentRoute != "home") {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple,
                selectedTextColor = PrimaryPurple,
                indicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.weight(0.2f))

        NavigationBarItem(
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "내 정보",
                    tint = if (currentRoute == "profile") PrimaryPurple else TextPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            label = {
                Text(
                    "내 정보",
                    fontFamily = Pretendard,
                    fontSize = 12.sp,
                    color = if (currentRoute == "profile") PrimaryPurple else TextPrimary
                )
            },
            selected = currentRoute == "profile",
            onClick = {
                if (currentRoute != "profile") {
                    navController.navigate("profile") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
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


@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)
