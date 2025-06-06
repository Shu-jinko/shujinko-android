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
import androidx.compose.foundation.Image
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.LocalImageLoader
import coil.compose.rememberAsyncImagePainter
import com.shujinko.app.data.Item.Emotion
import okhttp3.OkHttpClient
import java.net.URLEncoder
import kotlin.math.cos
import kotlin.math.sin


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiaryResultScreen(
    year: Int,
    month: Int,
    day: Int,
    token: String,
    diaryViewModel: DiaryViewModel,
    navController: NavController
) {
    val diary by diaryViewModel.todayDiary.collectAsState()
    val error by diaryViewModel.errorMessage.collectAsState()
    var showRaw by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        diaryViewModel.getDiary(token, year, month, day)
    }

    var navigateEdit by remember { mutableStateOf(false) }

    diary?.let { currentDiary ->
        if (navigateEdit) {
            LaunchedEffect(Unit) {
                val encodedRaw = URLEncoder.encode(currentDiary.rawDiary, "UTF-8")
                    .replace("+", "%20")
                    .replace("\n", "%0A")
                navController.navigate(
                    "diary_edit/${currentDiary.diaryId}/${year}/${month}/${day}/$encodedRaw"
                )
                navigateEdit = false
            }
        }
    }

    if (showDeleteDialog && diary != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("일기 삭제") },
            text = { Text("${year}년 ${month}월 ${day}일 일기를 정말 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    diaryViewModel.deleteDiary(token, diary!!.diaryId) {
                        showDeleteDialog = false
                        navController.popBackStack()
                    }
                }) {
                    Text("삭제", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            diary == null && error == null -> {
                CircularProgressIndicator()
            }

            error != null -> {
                Text("⚠️ 오류: $error")
            }

            diary != null -> {
                val it = diary!!  // Smart cast 보장

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
                    text = it.rephrasedDiary.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )

                Text("📄 요약:", fontSize = 16.sp)
                Text(
                    text = it.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )

                Text("📌 주요 키워드:", fontSize = 16.sp)
                val groupedKeywords = it.keywords.groupBy { keyword -> keyword.label }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    groupedKeywords.forEach { (label, keywords) ->
                        Column {
                            Text(text = label, fontSize = 14.sp, style = MaterialTheme.typography.titleMedium)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                keywords.forEach { keyword ->
                                    AssistChip(onClick = {}, label = { Text(keyword.text) })
                                }
                            }
                        }
                    }
                }

                Text("🧠 감정 분석 (비율):", fontSize = 16.sp)
                EmotionPieChart(emotions = it.emotions)

                Text("🖼️ 문단별 이미지 및 내용:", fontSize = 16.sp)

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    it.paragraph.forEachIndexed { index, paragraph ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("• 주제 ${index + 1}: ${paragraph.subject ?: "제목 없음"}")
                            Text("내용: ${paragraph.content ?: "내용 없음"}")

                            if (!paragraph.matched_image.isNullOrBlank()) {
                                Text("이미지 설명: ${paragraph.image_caption ?: "설명 없음"}")

                                // ✅ 새로운 보안 이미지 API 경로 구성
                                val fileName = paragraph.matched_image.substringAfterLast("/")
                                val imageUrl = "http://43.201.212.34:8080/diary/images/$fileName"

                                // ✅ 인증 헤더 포함 ImageLoader 생성
                                val context = LocalContext.current
                                val imageLoader = remember {
                                    ImageLoader.Builder(context)
                                        .okHttpClient {
                                            OkHttpClient.Builder()
                                                .addInterceptor { chain ->
                                                    val request = chain.request().newBuilder()
                                                        .addHeader("Authorization", "Bearer $token")
                                                        .build()
                                                    chain.proceed(request)
                                                }
                                                .build()
                                        }
                                        .build()
                                }

                                // ✅ Coil에 ImageLoader 주입 후 이미지 로딩
                                CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                                    Image(
                                        painter = rememberAsyncImagePainter(imageUrl),
                                        contentDescription = paragraph.image_caption ?: "이미지",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            navigateEdit = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("수정하기")
                    }

                    Button(
                        onClick = {
                            showDeleteDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("삭제하기", color = Color.White)
                    }
                }
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
