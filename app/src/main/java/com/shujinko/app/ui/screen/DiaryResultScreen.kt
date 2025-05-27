package com.shujinko.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.*

@Composable
fun DiaryResultScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "✅ 일기 작성이 완료되었습니다!",
            fontSize = 20.sp
        )
    }
}