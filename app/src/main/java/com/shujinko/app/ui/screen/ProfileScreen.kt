package com.shujinko.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shujinko.app.navigation.Screen
import java.time.LocalDate

@Composable
fun ProfileScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = {
                val today = LocalDate.now()
                val route = Screen.DeleteDiary.route
                    .replace("{year}", today.year.toString())
                    .replace("{month}", today.monthValue.toString())
                    .replace("{day}", today.dayOfMonth.toString())

                navController.navigate(route)
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("오늘 일기 삭제하기")
        }
    }
}