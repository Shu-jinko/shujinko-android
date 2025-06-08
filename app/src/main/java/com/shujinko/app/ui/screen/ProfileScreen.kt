package com.shujinko.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shujinko.app.navigation.Screen
import com.shujinko.app.viewmodel.UserViewModel
import android.widget.Toast
import androidx.compose.material3.OutlinedTextField


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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("이메일: ${email ?: "불러오는 중..."}", fontSize = 16.sp)

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
                        Toast.makeText(context  , "❌ 수정 실패!", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = !isSaving
        ) {
            Text(if (isSaving) "저장 중..." else "정보 수정")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                userViewModel.deleteUser { success ->
                    if (success) {
                        parentNavController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    }
                }
            }
        ) {
            Text("로그아웃")
        }
    }
}


