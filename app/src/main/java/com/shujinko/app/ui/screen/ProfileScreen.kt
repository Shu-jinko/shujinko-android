package com.shujinko.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shujinko.app.navigation.Screen
import com.shujinko.app.viewmodel.UserViewModel
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import com.shujinko.app.ui.theme.PrimaryPurple
import com.shujinko.app.ui.theme.Pretendard

@Composable
fun ProfileScreen(
    navController: NavController,
    parentNavController: NavController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // 사용자 정보 불러오기
    LaunchedEffect(Unit) {
        userViewModel.fetchUserInfo { e, n, b ->
            email = e
            name = n ?: ""
            birthday = b ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "내 정보",
            fontSize = 22.sp,
            fontFamily = Pretendard,
            color = PrimaryPurple
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "이메일: ${email ?: "불러오는 중..."}",
            fontSize = 16.sp,
            fontFamily = Pretendard
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = birthday,
            onValueChange = { birthday = it },
            label = { Text("생일 (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isSaving = true
                userViewModel.updateUserInfo(name, birthday) { success ->
                    isSaving = false
                    if (success) {
                        Toast.makeText(context, "✅ 수정 완료!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "❌ 수정 실패!", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(
                text = if (isSaving) "저장 중..." else "정보 수정",
                fontFamily = Pretendard,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = {
                userViewModel.deleteUser { success ->
                    if (success) {
                        parentNavController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryPurple)
        ) {
            Text("로그아웃", fontFamily = Pretendard, fontSize = 16.sp)
        }
    }
}