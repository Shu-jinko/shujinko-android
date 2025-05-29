package com.shujinko.app.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.shujinko.app.viewmodel.DiaryViewModel

@Composable
fun DiaryDeleteScreen(
    year: Int,
    month: Int,
    day: Int,
    navController: NavController,
    diaryViewModel: DiaryViewModel,
    token: String
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        diaryViewModel.getDiary(token, year, month, day) { success, diary ->
            if (success && diary != null) {
                diaryViewModel.deleteDiary(token, diary.diaryId) {
                    Toast.makeText(context, "일기 삭제 완료", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            } else {
                Toast.makeText(context, "오늘 작성된 일기가 없습니다.", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
