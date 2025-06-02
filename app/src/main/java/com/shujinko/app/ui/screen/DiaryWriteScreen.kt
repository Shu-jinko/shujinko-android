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
import kotlinx.coroutines.isActive
import java.time.LocalDate

@Composable
fun DiaryWriteScreen(
    navController: NavController,
    parentNavController: NavController,
    diaryViewModel: DiaryViewModel,
    token: String,
    isEditMode: Boolean = false,
    diaryId: Long? = null,
    initialText: String = ""
) {
    var text by remember { mutableStateOf(initialText) }
    var hasNavigated by remember { mutableStateOf(false) }

    val isLoading by diaryViewModel.isLoading.collectAsState()
    val errorMessage by diaryViewModel.errorMessage.collectAsState()
    val navigateState by diaryViewModel.shouldNavigate.collectAsState()
    val writeCompleted by diaryViewModel.writeCompleted.collectAsState()
    val context = LocalContext.current

    // ✅ 일기 없을 때 → diary_entry로 이동
    LaunchedEffect(navigateState) {
        if (navigateState && !hasNavigated && coroutineContext.isActive) {
            hasNavigated = true
            parentNavController.navigate("diary_entry") {
                launchSingleTop = true
            }
            diaryViewModel.resetShouldNavigate()
        }
    }

    // ✅ 작성 완료 → diary_result로 이동
    LaunchedEffect(Unit) {
        snapshotFlow { writeCompleted }
            .collect { completed ->
                if (completed) {
                    navController.navigate("diary_result") {
                        launchSingleTop = true
                    }
                    diaryViewModel.resetWriteCompleted()
                }
            }
    }

    // 에러 메시지 Toast
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
                val todayStr = LocalDate.now().toString()
                if (isEditMode && diaryId != null) {
                    println("✅ 수정 진행 중")
                    diaryViewModel.updateDiary(token, diaryId, text) {
                        println("✅ 수정 완료됨 - popBackStack 호출")
                        navController.popBackStack()
                    }
                } else {
                    println("✍️ createDiary 호출됨 - $todayStr")
                    diaryViewModel.createDiary(
                        token = token,
                        rawDiary = text,
                        diaryDate = todayStr,
                        onSuccess = {
                            println("✅ 작성 완료됨 - 상태 플래그 업데이트만")
                        },
                        onFailure = { error ->
                            println("❌ 작성 실패: $error")
                        }
                    )
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
