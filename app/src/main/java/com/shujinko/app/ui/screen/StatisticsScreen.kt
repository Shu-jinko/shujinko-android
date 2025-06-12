package com.shujinko.app.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shujinko.app.data.Item.EmotionStat
import com.shujinko.app.data.Item.KeywordStat
import com.shujinko.app.ui.components.TopTitle
import com.shujinko.app.viewmodel.StatisticsViewModel
import java.time.LocalDate
import java.time.temporal.WeekFields

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel, token: String) {
    var isMonthly by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val emotions by if (isMonthly) viewModel.monthEmotions.collectAsState() else viewModel.weekEmotions.collectAsState()
    val keywords by if (isMonthly) viewModel.monthKeywords.collectAsState() else viewModel.weekKeywords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(isMonthly, selectedDate) {
        val weekNumber = selectedDate.get(WeekFields.ISO.weekOfMonth())
        if (isMonthly) {
            viewModel.loadMonthlyStatistics(token, selectedDate.year, selectedDate.monthValue)
        } else {
            viewModel.loadWeeklyStatistics(token, selectedDate.year, selectedDate.monthValue, weekNumber)
        }
    }

    TopTitle(
        title = "일기 통계",
        content = {
            // (1) 토글
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                StatisticsToggle(
                    isMonthly = isMonthly,
                    onToggle = { isMonthly = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // (2) 날짜 텍스트
            Text(
                text = if (isMonthly)
                    "  ${selectedDate.year}년 ${selectedDate.monthValue}월"
                else {
                    val weekNumber = selectedDate.get(WeekFields.ISO.weekOfMonth())
                    "  ${selectedDate.year}년 ${selectedDate.monthValue}월 ${weekNumber}주차"
                },
                style = MaterialTheme.typography.titleMedium,
            )

            // (3) 좌우 스와이프 처리 → 단일 컴포넌트에만 적용
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(selectedDate) {
                        detectDragGestures { _, dragAmount ->
                            selectedDate = if (dragAmount.x > 50) {
                                if (isMonthly) selectedDate.minusMonths(1) else selectedDate.minusWeeks(1)
                            } else if (dragAmount.x < -50) {
                                if (isMonthly) selectedDate.plusMonths(1) else selectedDate.plusWeeks(1)
                            } else selectedDate
                        }
                    }
            ) {
                // 빈 Box여도 Modifier는 위에서 실행됨
            }

            Spacer(modifier = Modifier.height(12.dp))

            // (4) 데이터 영역
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (emotions.isNotEmpty()) EmotionCard(emotions)
                else Text("감정 데이터 없음", color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                if (keywords.isNotEmpty()) ExpandableKeywordCard(keywords)
                else Text("키워드 데이터 없음", color = Color.Gray)
            }
        }
    )
}

@Composable
fun StatisticsToggle(
    isMonthly: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val toggleOptions = listOf("주간", "월간")

    Surface(
        modifier = Modifier
            .height(36.dp)
            .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            toggleOptions.forEachIndexed { index, label ->
                val selected = (isMonthly && index == 1) || (!isMonthly && index == 0)

                val backgroundColor =
                    if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                val textColor =
                    if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onToggle(index == 1) } // 주간(false), 월간(true)
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor
                    )
                }
            }
        }
    }
}


@Composable
fun ExpandableKeywordCard(keywords: List<KeywordStat>) {
    var expanded by remember { mutableStateOf(false) }
    val max = keywords.maxOfOrNull { it.count } ?: 1
    val shownKeywords = if (expanded) keywords else keywords.take(5)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // 제목 + 더보기
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "키워드",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (keywords.size > 5) {
                    Text(
                        text = if (expanded) "접기 ▲" else "더보기 ▼",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 키워드 리스트
            shownKeywords.forEach { keyword ->
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "#${keyword.keyword} (${keyword.label})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${keyword.count}회",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 막대 그래프
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((keyword.count / max.toFloat()).coerceIn(0.05f, 1f))
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun EmotionCard(emotions: List<EmotionStat>) {
    val total = emotions.sumOf { it.count }.toFloat().coerceAtLeast(1f) // 0 나눗셈 방지
    val emotionColors = listOf(
        Color(0xFF2196F3), // 슬픔
        Color(0xFFF44336), // 분노
        Color(0xFFFFEB3B), // 기대감
        Color(0xFF795548), // 불안
        Color(0xFFFF9800), // 놀람
        Color(0xFF4CAF50), // 기쁨
        Color(0xFF9C27B0), // 평온함
    )

    val emotionEmojis = mapOf(
        "슬픔" to "😢",
        "분노" to "😡",
        "기대감" to "🤩",
        "불안" to "😱",
        "놀람" to "😲",
        "기쁨" to "😊",
        "평온함" to "😌"
    )

    var startAngle = 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("감정 분포", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(end = 16.dp)
                ) {
                    emotions.forEachIndexed { index, item ->
                        val sweep = (item.count / total) * 360f
                        drawArc(
                            color = emotionColors[index % emotionColors.size],
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = true,
                            size = Size(size.minDimension, size.minDimension)
                        )
                        startAngle += sweep
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    emotions.forEachIndexed { index, item ->
                        val percent = (item.count / total) * 100
                        val emoji = emotionEmojis[item.emotion] ?: "🙂"
                        val color = emotionColors[index % emotionColors.size]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "$emoji ${item.emotion}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = color
                            )
                            Text("${"%.0f".format(percent)}% (${item.count}회)", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

