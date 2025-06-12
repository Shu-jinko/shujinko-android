package com.shujinko.app.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shujinko.app.ui.screen.DiaryResultScreen
import com.shujinko.app.ui.screen.DiaryWriteScreen
import com.shujinko.app.viewmodel.DiaryViewModel
import com.shujinko.app.viewmodel.SuggestionViewModel
import java.net.URLDecoder
import java.time.LocalDate

@Composable
fun DiaryNavHost(
    diaryViewModel: DiaryViewModel,
    token: String,
    parentNavController: NavController,
    shouldNavigateAutomatically: Boolean = true
) {
    val diaryNavController = rememberNavController()
    var hasCheckedDiary by remember { mutableStateOf(false) }

    val navBackStackEntry by diaryNavController.currentBackStackEntryAsState()

    // 현재 화면 로깅
    LaunchedEffect(navBackStackEntry) {
        val route = navBackStackEntry?.destination?.route
        Log.d("NavDebug", "현재 화면: $route")
    }

    // 화면 이동 로그
    LaunchedEffect(Unit) {
        diaryNavController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("NavDebug", "이동된 화면: ${destination.route}")
        }
    }

    NavHost(
        navController = diaryNavController,
        startDestination = "diary_entry"
    ) {
        composable("diary_entry") {
            val today = LocalDate.now()
            val initialRoute by diaryViewModel.initialRoute.collectAsState()

            // ✅ 최초 1회만 check
            LaunchedEffect(Unit) {
                if (!hasCheckedDiary) {
                    hasCheckedDiary = true
                    diaryViewModel.checkTodayDiaryAndDecideRoute(
                        token,
                        today.year,
                        today.monthValue,
                        today.dayOfMonth
                    )
                }
            }

            // ✅ 상태 기반 navigate - 안정된 시점에 실행
            LaunchedEffect(initialRoute) {
                initialRoute?.let { target ->
                    diaryNavController.navigate(target) {
                        launchSingleTop = true
                    }
                    diaryViewModel.resetInitialRoute()
                }
            }
        }

        composable("diary_write") {
            val suggestionViewModel: SuggestionViewModel = hiltViewModel()

            DiaryWriteScreen(
                navController = diaryNavController,
                diaryViewModel = diaryViewModel,
                suggestionViewModel = suggestionViewModel,
                token = token,
                isEditMode = false,
                initialText = "",
                diaryId = null,
                parentNavController = parentNavController
            )
        }

        composable("diary_result") {
            val today = LocalDate.now()
            DiaryResultScreen(
                year = today.year,
                month = today.monthValue,
                day = today.dayOfMonth,
                token = token,
                diaryViewModel = diaryViewModel,
                navController = diaryNavController
            )
        }

        composable(
            "diary_edit/{id}/{year}/{month}/{day}/{rawDiary}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("year") { type = NavType.StringType },
                navArgument("month") { type = NavType.StringType }, 
                navArgument("day") { type = NavType.StringType },
                navArgument("rawDiary") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
            val year = backStackEntry.arguments?.getString("year")?.toIntOrNull() ?: return@composable
            val month = backStackEntry.arguments?.getString("month")?.toIntOrNull() ?: return@composable
            val day = backStackEntry.arguments?.getString("day")?.toIntOrNull() ?: return@composable
            val diaryDate = LocalDate.of(year, month, day)
            val rawDiary = backStackEntry.arguments?.getString("rawDiary") ?: ""
            val suggestionViewModel: SuggestionViewModel = hiltViewModel()

            DiaryWriteScreen(
                navController = diaryNavController,
                diaryViewModel = diaryViewModel,
                suggestionViewModel = suggestionViewModel,
                token = token,
                isEditMode = true,
                initialText = URLDecoder.decode(rawDiary, "UTF-8"),
                diaryId = id,
                diaryDate = diaryDate, // ✅ 여기 추가됨
                parentNavController = parentNavController
            )
        }

    }
}