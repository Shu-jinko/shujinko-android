package com.shujinko.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shujinko.app.viewmodel.DiaryViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.shujinko.app.data.Emotion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiaryResultScreen(
    year: Int,
    month: Int,
    day: Int,
    token: String,
    diaryViewModel: DiaryViewModel
) {
    val diary by diaryViewModel.todayDiary.collectAsState()
    val error by diaryViewModel.errorMessage.collectAsState()
    var showRaw by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        diaryViewModel.getDiary(token, year, month, day)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (diary == null && error == null) {
            CircularProgressIndicator()
            return@Column
        }

        error?.let {
            Text("⚠️ 오류: $it")
            return@Column
        }

        diary?.let {
            Text("✅ 일기 분석 결과", fontSize = 20.sp)
            Text("라벨: ${it.label}", style = MaterialTheme.typography.bodyLarge)

            Button(onClick = { showRaw = !showRaw }) {
                Text(if (showRaw) "원본 숨기기" else "원본 보기")
            }

            if (showRaw) {
                Text(
                    text = it.rawDiary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Text("✍️ 재작성된 일기:", fontSize = 16.sp)
            Text(
                text = it.rephrasedDiary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )

            Text("📌 주요 키워드:", fontSize = 16.sp)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                it.keywords.forEach { keyword ->
                    AssistChip(onClick = {}, label = { Text(keyword) })
                }
            }

            Text("🧠 감정 분석 (비율):", fontSize = 16.sp)
            EmotionPieChart(emotions = it.emotions)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("수정하기")
            }
        }
    }
}

@Composable
fun EmotionPieChart(
    emotions: List<Emotion>,
    modifier: Modifier = Modifier,
    radius: Float = 150f
) {
    val filtered = emotions.filter { it.score > 0 }
    val total = filtered.sumOf { it.score.toDouble() }.toFloat()

    val colors = listOf(
        Color(0xFF2196F3), // 기쁨
        Color(0xFF9C27B0), // 슬픔
        Color(0xFFF44336), // 분노
        Color(0xFF795548), // 두려움
        Color(0xFFFF9800), // 놀람
        Color(0xFF607D8B), // 불쾌함
        Color(0xFF3F51B5), // 죄책감
        Color(0xFF4CAF50), // 사랑
        Color(0xFF673AB7), // 수치심
        Color(0xFFFFEB3B)  // 기대감
    )

    val emotionMap = filtered.zip(colors)

    Canvas(modifier = modifier.height(300.dp).fillMaxWidth()) {
        var startAngle = -90f

        emotionMap.forEach { (emotion, color) ->
            val sweep = ((emotion.score / total) * 360f).toFloat()  // <-- 여기!
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(size.width / 2 - radius, 40f),
                size = Size(radius * 2, radius * 2)
            )
            startAngle += sweep
        }

        // 감정 이름 레이블
        var labelAngle = -90f
        emotionMap.forEach { (emotion, _) ->
            val sweep = ((emotion.score / total) * 360f).toFloat()
            val angle = labelAngle + sweep / 2
            val rad = Math.toRadians(angle.toDouble())

            val labelX = (size.width / 2 + cos(rad) * (radius + 20)).toFloat()
            val labelY = (40 + radius + sin(rad) * (radius + 20)).toFloat()

            drawIntoCanvas {
                it.nativeCanvas.drawText(
                    "${emotion.emotion} (${(emotion.score * 100).toInt()}%)",
                    labelX,
                    labelY,
                    android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 30f
                        color = android.graphics.Color.BLACK
                    }
                )
            }

            labelAngle += sweep
        }
    }
}
