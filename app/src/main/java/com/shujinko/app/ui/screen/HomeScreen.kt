    package com.shujinko.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shujinko.app.ui.components.HomeActionCard
import com.shujinko.app.ui.components.S_Card
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

        LaunchedEffect(Unit) {
            viewModel.loadOneSentence(token, today.year, today.monthValue, weekNumber)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Shujinko",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
            ) {
                // 인사 + 요약 문장
                Text(
                    text = "오늘 하루도 수고했어요 ☕",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = summary?.sentence ?: "한 문장 요약을 불러오는 중...",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 감정/키워드 카드
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    S_Card("Mood Analysis", "😄 행복\n📈 상승세")
                    S_Card("Frequent Keywords", "☀️ 산책\n💻 공부")
                }

                Spacer(modifier = Modifier.weight(1f))

                // 기존 버튼 대신 아래로 교체
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    HomeActionCard(
                        emoji = "📊",
                        title = "일기 통계 보기",
                        onClick = onClickStats
                    )
                    HomeActionCard(
                        emoji = "🗓️",
                        title = "일기 달력 보기",
                        onClick = onClickCalendar
                    )
                }
            }
        }
    }

