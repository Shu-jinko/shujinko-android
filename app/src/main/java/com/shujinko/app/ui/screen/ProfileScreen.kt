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

@Composable
fun ProfileScreen(
    navController: NavController,
    parentNavController: NavController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf<String?>(null) }
    var birthday by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userViewModel.fetchUserInfo { e, n, b ->
            email = e
            name = n
            birthday = b
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "이메일: ${email ?: "불러오는 중..."}", fontSize = 16.sp)
        Text(text = "이름: ${name ?: "불러오는 중..."}", fontSize = 16.sp)
        Text(text = "생일: ${birthday ?: "불러오는 중..."}", fontSize = 16.sp)

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
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("로그아웃")
        }
    }
}

