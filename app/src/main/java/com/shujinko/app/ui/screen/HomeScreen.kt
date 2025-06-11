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
    val day7Emotions by viewModel.day7Emotions.collectAsState()
    val day30Emotions by viewModel.day30Emotions.collectAsState()
    val day7Keywords by viewModel.day7Keywords.collectAsState()
    val day30Keywords by viewModel.day30Keywords.collectAsState()

    var isWeekly by remember { mutableStateOf(true) }

    val emotions = if (isWeekly) day7Emotions else day30Emotions
    val keywords = if (isWeekly) day7Keywords else day30Keywords    

    val topEmotions = emotions.sortedByDescending { it.count }.take(3)
    val topKeywords = keywords.sortedByDescending { it.count }.take(3)

    val today = LocalDate.now()
    val weekNumber = today.get(WeekFields.ISO.weekOfMonth())

    LaunchedEffect(Unit) {
        viewModel.loadOneSentence(token, today.year, today.monthValue, weekNumber)
        viewModel.loadDay7Statistics(token)
        viewModel.loadDay30Statistics(token)
    }

    fun getEmotionEmoji(emotion: String): String {
        return when (emotion) {
            "슬픔" -> "😢"
            "분노" -> "😡"
            "죄책감" -> "😞"
            "기대감" -> "🤩"
            "불쾌함" -> "😖"
            "두려움" -> "😱"
            "놀람" -> "😲"
            "사랑" -> "❤️"
            "기쁨" -> "😊"
            "수치심" -> "😳"
            else -> "🙂"
        }
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

            // 토글 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { isWeekly = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isWeekly) MaterialTheme.colorScheme.primary else Color.LightGray
                    )
                ) {
                    Text("최근 7일")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { isWeekly = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isWeekly) MaterialTheme.colorScheme.primary else Color.LightGray
                    )
                ) {
                    Text("최근 30일")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 감정/키워드 카드
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                S_Card(
                    title = "Mood Analysis",
                    content = topEmotions.joinToString("\n") { "${getEmotionEmoji(it.emotion)} ${it.emotion}" }
                )
                S_Card(
                    title = "Frequent Keywords",
                    content = topKeywords.joinToString("\n") { it.keyword }
                )
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
