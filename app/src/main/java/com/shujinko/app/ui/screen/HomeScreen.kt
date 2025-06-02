package com.shujinko.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onClickStats: () -> Unit,
    onClickCalendar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Shujinko",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 문구
            Text(
                text = "오늘 하루도 수고했어요 ☕",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 32.dp)
            )

            // Spacer로 중앙 공간 확보
            Spacer(modifier = Modifier.weight(1f))

            // 버튼 두 개
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onClickStats,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text("일기 통계")
                }
                Button(
                    onClick = onClickCalendar,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text("일기 달력")
                }
            }
        }
    }
}