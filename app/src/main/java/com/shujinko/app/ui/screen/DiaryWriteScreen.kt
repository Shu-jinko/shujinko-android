package com.shujinko.app.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shujinko.app.viewmodel.DiaryViewModel
import java.time.LocalDate

@Composable
fun DiaryWriteScreen(
    navController: NavController,
    diaryViewModel: DiaryViewModel,
    token: String,
    isEditMode: Boolean = false,
    diaryId: Long? = null,
    initialText: String = ""
) {
    var text by remember { mutableStateOf(initialText) }

    LaunchedEffect(Unit) {
        println("🧾 editMode: $isEditMode") // true 나와야 함
        println("🧾 initialText: $initialText") // 원본 일기 나와야 함
    }

    val isLoading by diaryViewModel.isLoading.collectAsState()
    val errorMessage by diaryViewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    var navigateTrigger by remember { mutableStateOf(false) }
    val today = LocalDate.now()

    LaunchedEffect(navigateTrigger) {
        if (navigateTrigger) {
            navController.navigate("diary_result/${today.year}/${today.monthValue}/${today.dayOfMonth}")
            navigateTrigger = false
        }
    }

    // 에러 발생 시 Toast로 표시
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = if (isEditMode) "일기를 수정하세요" else "일기를 입력하세요",
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                placeholder = { Text("오늘 하루를 자유롭게 적어보세요!") }
            )
        }

        Button(
            onClick = {
                if (isEditMode && diaryId != null) {
                    diaryViewModel.updateDiary(token, diaryId, text){
                        navController.popBackStack() // 수정 후 뒤로 이동
                    }
                } else {
                    diaryViewModel.createDiary(token, text) {
                        navigateTrigger = true
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (isEditMode) "수정 완료" else "작성 완료")
            }
        }
    }
}