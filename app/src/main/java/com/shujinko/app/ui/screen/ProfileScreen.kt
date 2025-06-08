package com.shujinko.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shujinko.app.navigation.Screen
import java.time.LocalDate
import com.shujinko.app.viewmodel.LoginViewModel
import com.shujinko.app.viewmodel.UserViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    parentNavController: NavController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
