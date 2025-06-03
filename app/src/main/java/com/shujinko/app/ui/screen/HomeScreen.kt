package com.shujinko.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shujinko.app.viewmodel.StatisticsViewModel
import java.time.LocalDate
import java.time.temporal.WeekFields

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onClickStats: () -> Unit,
    onClickCalendar: () -> Unit,
    viewModel: StatisticsViewModel,
    token: String
) {
    val summary by viewModel.summary.collectAsState()
    val today = LocalDate.now()
    val weekNumber = today.get(WeekFields.ISO.weekOfMonth())

    // 페이지 진입 시마다 새로 호출
    LaunchedEffect(Unit) {
        viewModel.loadOneSentence(token, today.year, today.monthValue, weekNumber)
    }

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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Text(
                    text = "오늘 하루도 수고했어요 ☕",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = summary?.sentence?.takeIf { it.isNotBlank() } ?: "한 문장 요약을 불러오는 중...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

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
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("일기 통계")
                }
                Button(
                    onClick = onClickCalendar,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text("일기 달력")
                }
            }
        }
    }
}
