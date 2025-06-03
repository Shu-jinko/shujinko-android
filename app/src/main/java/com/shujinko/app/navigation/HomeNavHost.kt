package com.shujinko.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shujinko.app.ui.screen.DiaryCalendarScreen
import com.shujinko.app.ui.screen.DiaryResultScreen
import com.shujinko.app.ui.screen.HomeScreen
import com.shujinko.app.viewmodel.DiaryViewModel
import java.time.LocalDate
import androidx.compose.runtime.LaunchedEffect
import com.shujinko.app.ui.screen.DiaryWriteScreen
import com.shujinko.app.ui.screen.StatisticsScreen
import com.shujinko.app.viewmodel.StatisticsViewModel
import com.shujinko.app.viewmodel.SuggestionViewModel
import java.net.URLDecoder
import java.time.temporal.WeekFields


@Composable
fun HomeNavHost(
    token: String,
    diaryViewModel: DiaryViewModel,
    parentNavController: NavController
) {
    val homeNavController = rememberNavController()

    NavHost(
        navController = homeNavController,
        startDestination = "home"
    ) {
        composable("home") {
            val statsViewModel: StatisticsViewModel = hiltViewModel()
            HomeScreen(
                onClickStats = { homeNavController.navigate("diary_stats") },
                onClickCalendar = { homeNavController.navigate("diary_calendar") },
                viewModel = statsViewModel,
                token = token
            )
        }
        composable("diary_stats") {
            val statsViewModel: StatisticsViewModel = hiltViewModel()
            StatisticsScreen(viewModel = statsViewModel, token = token)
        }

        composable("diary_calendar") {
            val diaryViewModel: DiaryViewModel = hiltViewModel()
            val diaryMap = diaryViewModel.diaryMap.collectAsState().value

            LaunchedEffect(Unit) {
                val now = LocalDate.now()
                diaryViewModel.loadDiaryList(token, now.year, now.monthValue)
            }

            DiaryCalendarScreen(
                onClickMore = { selectedDate ->
                    homeNavController.navigate("diary_result/${selectedDate.year}/${selectedDate.monthValue}/${selectedDate.dayOfMonth}")
                },
                diaryMap = diaryMap,
                onMonthChange = { year, month ->
                    diaryViewModel.loadDiaryList(token, year, month)
                }
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
            val rawDiary = backStackEntry.arguments?.getString("rawDiary") ?: ""
            val suggestionViewModel: SuggestionViewModel = hiltViewModel()

            DiaryWriteScreen(
                navController = homeNavController,
                diaryViewModel = diaryViewModel,
                suggestionViewModel = suggestionViewModel,
                token = token,
                isEditMode = true,
                initialText = URLDecoder.decode(rawDiary, "UTF-8"),
                diaryId = id,
                parentNavController = parentNavController
            )
        }

        composable(
            route = "diary_result/{year}/{month}/{day}",
            arguments = listOf(
                navArgument("year") { type = NavType.IntType },
                navArgument("month") { type = NavType.IntType },
                navArgument("day") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val year = backStackEntry.arguments?.getInt("year") ?: return@composable
            val month = backStackEntry.arguments?.getInt("month") ?: return@composable
            val day = backStackEntry.arguments?.getInt("day") ?: return@composable

            DiaryResultScreen(
                year = year,
                month = month,
                day = day,
                token = token,
                diaryViewModel = diaryViewModel,
                navController = homeNavController
            )
        }
    }
}