package com.shujinko.app.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.shujinko.app.R
import com.shujinko.app.navigation.Screen
import com.shujinko.app.ui.components.TopTitle
import com.shujinko.app.ui.theme.Pretendard
import com.shujinko.app.ui.theme.PrimaryPurple
import com.shujinko.app.viewmodel.UserViewModel

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
    var photoUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userViewModel.fetchUserInfo { e, n, b, p ->
            email = e
            name = n ?: ""
            birthday = b ?: ""
            photoUrl = p ?: ""
        }
    }
    
    TopTitle(title = "PROFILE") {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .fillMaxWidth()
            ) {
                photoUrl?.let {
                    val safeUrl = if (it.contains("googleusercontent")) {
                        if (it.contains("?")) "$it&sz=400" else "$it?sz=400"
                    } else it

                    val painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(safeUrl)
                            .crossfade(true)
                            .placeholder(R.drawable.default_user)
                            .error(R.drawable.default_user)
                            .build()
                    )

                    Image(
                        painter = painter,
                        contentDescription = "프로필 이미지",
                        modifier = Modifier.size(120.dp).clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "환영합니다,", fontFamily = Pretendard, fontSize = 14.sp)
                    Text(text = name, fontFamily = Pretendard, fontSize = 18.sp, color = PrimaryPurple)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "이메일",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = email ?: "불러오는 중...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = {
                    Text(
                        "이름",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = birthday,
                onValueChange = { birthday = it },
                label = {
                    Text(
                        "생일 (YYYY-MM-DD)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(80.dp))

            Button(
                onClick = {
                    isSaving = true
                    userViewModel.updateUserInfo(name, birthday) { success ->
                        isSaving = false
                        if (success) {
                            Toast.makeText(context, "정보 수정이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "정보 수정에 실패하였습니다", Toast.LENGTH_SHORT).show()
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

            Spacer(modifier = Modifier.height(12.dp))

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
            Spacer(modifier = Modifier.height(12.dp))
        }
    }


}