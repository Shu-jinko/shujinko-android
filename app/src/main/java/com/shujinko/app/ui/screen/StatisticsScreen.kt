package com.shujinko.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shujinko.app.viewmodel.StatisticsViewModel
import com.shujinko.app.data.Item.EmotionStat
import com.shujinko.app.data.Item.KeywordStat
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import kotlin.math.cos
import kotlin.math.sin

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

    val formattedDate = if (isMonthly) {
        DateTimeFormatter.ofPattern("yyyy년 M월").format(selectedDate)
    } else {
        val weekNumber = selectedDate.get(WeekFields.ISO.weekOfMonth())
        "${selectedDate.year}년 ${selectedDate.monthValue}월 ${weekNumber}주차"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .pointerInput(selectedDate) {
                detectDragGestures { _, dragAmount ->
                    if (dragAmount.x > 50) {
                        selectedDate = if (isMonthly) selectedDate.minusMonths(1) else selectedDate.minusWeeks(1)
                    } else if (dragAmount.x < -50) {
                        selectedDate = if (isMonthly) selectedDate.plusMonths(1) else selectedDate.plusWeeks(1)
                    }
                }
            },
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("통계", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            SegmentedButton(isMonthly) { isMonthly = it }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                selectedDate = if (isMonthly) selectedDate.minusMonths(1) else selectedDate.minusWeeks(1)
            }) { Text("이전") }
            Text(text = formattedDate)
            Button(onClick = {
                selectedDate = if (isMonthly) selectedDate.plusMonths(1) else selectedDate.plusWeeks(1)
            }) { Text("다음") }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (emotions.isNotEmpty()) EmotionCard(emotions) else Text("감정 데이터 없음", color = Color.Gray)
            if (keywords.isNotEmpty()) KeywordCard(keywords) else Text("키워드 데이터 없음", color = Color.Gray)
        }
    }
}

@Composable
fun SegmentedButton(isMonthly: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .background(Color.LightGray.copy(alpha = 0.3f))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onToggle(false) },
            colors = ButtonDefaults.buttonColors(containerColor = if (!isMonthly) Color.Blue else Color.LightGray)
        ) {
            Text("주간", color = if (!isMonthly) Color.White else Color.Black)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { onToggle(true) },
            colors = ButtonDefaults.buttonColors(containerColor = if (isMonthly) Color.Blue else Color.LightGray)
        ) {
            Text("월간", color = if (isMonthly) Color.White else Color.Black)
        }
    }
}

@Composable
fun EmotionCard(emotions: List<EmotionStat>) {
    val total = emotions.sumOf { it.count }.toFloat()
    val colors = listOf(Color(0xFFFFD700), Color(0xFF87CEFA), Color(0xFF32CD32), Color(0xFFB0C4DE))
    var startAngle = 0f

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp)) {
            Canvas(modifier = Modifier
                .size(180.dp)
                .padding(end = 16.dp)) {
                emotions.forEachIndexed { index, item ->
                    val sweep = (item.count / total) * 360f
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        size = Size(size.minDimension, size.minDimension)
                    )
                    startAngle += sweep
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("감정 분포", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                emotions.forEachIndexed { index, item ->
                    Text("${item.emotion}: ${"%.0f".format(item.count / total * 100)}% (${item.count}번)")
                }
            }
        }
    }
}

@Composable
fun KeywordCard(keywords: List<KeywordStat>) {
    val max = keywords.maxOfOrNull { it.count } ?: 1
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("키워드", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            keywords.forEach { keyword ->
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text("#${keyword.keyword} (${keyword.label}) - ${keyword.count}번")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((keyword.count / max.toFloat()).coerceAtLeast(0.05f))
                                .background(Color.Blue)
                        )
                    }
                }
            }
        }
    }
}