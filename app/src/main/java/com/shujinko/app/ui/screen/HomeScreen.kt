package com.shujinko.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shujinko.app.ui.components.HomeActionCard
import com.shujinko.app.ui.components.TopTitle
import com.shujinko.app.viewmodel.StatisticsViewModel
import java.time.LocalDate
import java.time.temporal.WeekFields
import com.google.accompanist.pager.*
import com.shujinko.app.data.Item.EmotionStat
import com.shujinko.app.data.Item.KeywordStat
import com.shujinko.app.ui.theme.Pretendard
import kotlinx.coroutines.launch
import kotlin.collections.sortedByDescending

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
            "기대감" -> "🤩"
            "불안" -> "😱"
            "놀람" -> "😲"
            "기쁨" -> "😊"
            "평온함" -> "😌"
            else -> "🙂"
        }
    }

    TopTitle(title = "DAYKEEPER") {
        Column {
            Text(
                text = "오늘 하루도 수고했어요 ☕",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "💬 오늘의 한 문장",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "“ ${summary?.sentence ?: "한 문장 요약을 불러오는 중..."} ”",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }


            Spacer(modifier = Modifier.height(32.dp))


            Text(
                text = "TOP RANKING",
                style = MaterialTheme.typography.titleMedium
            )

            StatisticsTabSection(
                day7Emotions = day7Emotions,
                day30Emotions = day30Emotions,
                day7Keywords = day7Keywords,
                day30Keywords = day30Keywords,
                getEmotionEmoji = ::getEmotionEmoji
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                HomeActionCard(
                    icon = Icons.Default.BarChart,
                    title = "일기 통계 보기",
                    onClick = onClickStats
                )

                HomeActionCard(
                    icon = Icons.Default.CalendarMonth,
                    title = "일기 달력 보기",
                    onClick = onClickCalendar
                )
            }
        }
    }

}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun StatisticsTabSection(
    day7Emotions: List<EmotionStat>,
    day30Emotions: List<EmotionStat>,
    day7Keywords: List<KeywordStat>,
    day30Keywords: List<KeywordStat>,
    getEmotionEmoji: (String) -> String
) {
    val titles = listOf("최근 7일", "최근 30일")
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    Column {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = {}
        ) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(
                            text = title,
                            color = if (pagerState.currentPage == index)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.Gray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalPager(
            count = 2,
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val emotions = if (page == 0) day7Emotions else day30Emotions
            val keywords = if (page == 0) day7Keywords else day30Keywords

            val topEmotions = emotions.sortedByDescending { it.count }.take(3)
            val topKeywords = keywords.sortedByDescending { it.count }.take(3)

            Spacer(modifier = Modifier.height(12.dp))

            if (topEmotions.isEmpty() && topKeywords.isEmpty()) {
                // 아무것도 없을 때
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "작성된 일기가 없어요",
                            color = Color.DarkGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                // 원래 카드들
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StyledInfoCard(
                        title = "감정 비율",
                        contents = topEmotions.map { "${getEmotionEmoji(it.emotion)} ${it.emotion} (${it.count}회)" },
                        backgroundColor = Color(0xFFB6BAF2),
                        modifier = Modifier.weight(1f)
                    )

                    StyledInfoCard(
                        title = "키워드 빈도",
                        contents = topKeywords.map { "${it.keyword} (${it.count}회)" },
                        backgroundColor = Color(0xFFD9BFFF  ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

    @Composable
    fun StyledInfoCard(
        title: String,
        contents: List<String>,
        backgroundColor: Color,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier.height(140.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontFamily = Pretendard
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    contents.take(3).forEachIndexed { index, line ->
                        Text(
                            text = "${index + 1}위. $line",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontFamily = Pretendard
                        )
                    }
                }
            }
        }
    }