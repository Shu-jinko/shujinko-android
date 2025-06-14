package com.shujinko.app.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shujinko.app.viewmodel.DiaryViewModel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.LocalImageLoader
import coil.compose.rememberAsyncImagePainter
import com.shujinko.app.data.Item.Emotion
import com.shujinko.app.ui.components.TopTitle
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
    navController: NavController,
    parentNavController: NavController
) {
    val diary by diaryViewModel.todayDiary.collectAsState()
    val error by diaryViewModel.errorMessage.collectAsState()
    var showRaw by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var navigateEdit by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        diaryViewModel.getDiary(token, year, month, day)
    }

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
            title = { Text("일기를 삭제할까요?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text("${year}년 ${month}월 ${day}일의 일기를 삭제하면 복구할 수 없어요.\n정말 삭제하시겠어요?", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    diaryViewModel.deleteDiary(token, diary!!.diaryId) {
                        showDeleteDialog = false
                        Toast.makeText(context, "일기가 삭제되었어요.", Toast.LENGTH_SHORT).show()
                        parentNavController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }) { Text("삭제", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("취소") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showEditDialog && diary != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("일기를 수정할까요?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text("${year}년 ${month}월 ${day}일의 일기를 수정할 수 있어요.\n바꾸시겠어요?", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    showEditDialog = false
                    navigateEdit = true
                }) { Text("수정") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("취소") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    TopTitle(
        title = "일기 정리",
    ) {
        when {
            diary == null && error == null -> {
                CircularProgressIndicator()
            }

            error != null -> {
                Text("⚠️ 오류: $error")
            }

            diary != null -> {
                val it = diary!!
                val emotionIcon = getEmotionEmoji(it.emotions.maxByOrNull { e -> e.score }?.emotion ?: "")
                Text("${year}년 ${month}월 ${day}일, 나의 하루 $emotionIcon", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))
                    
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    it.paragraph.forEachIndexed { index, paragraph ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // 타임라인 점과 선
                            Column(
                                modifier = Modifier.padding(top = 12.dp).width(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                if (index != it.paragraph.lastIndex) {
                                    Spacer(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(160.dp)
                                            .background(Color.LightGray)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    paragraph.subject?.let {
                                        Text(it, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    paragraph.content?.let {
                                        Text(it, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (!paragraph.matched_image.isNullOrBlank()) {
                                        val fileName = paragraph.matched_image.substringAfterLast("/")
                                        val imageUrl = "http://43.201.212.34:8080/diary/images/$fileName"
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
                                        CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(1f)
                                            ) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(imageUrl),
                                                    contentDescription = paragraph.image_caption ?: "이미지",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
//                                        paragraph.image_caption?.let {
//                                            Spacer(modifier = Modifier.height(4.dp))
//                                            Text(it, fontSize = 12.sp, color = Color.Gray)
//                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("요약", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF3F5FF)  // 부드러운 연보라
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Text(
                        text = it.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }


                Spacer(modifier = Modifier.height(24.dp))

                Text("주요 키워드", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))

                val groupedKeywords = it.keywords.groupBy { k -> k.label }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    groupedKeywords.forEach { (label, keywords) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F5FF))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = label, // 예: 감정, 활동, 장소 등
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    keywords.forEach { keyword ->
                                        AssistChip(
                                            onClick = {},
                                            label = {
                                                Text(text = "#${keyword.text}")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("감정 분석", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAFE))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Spacer(modifier = Modifier.height(16.dp))

                        EmotionPieChart(emotions = it.emotions)

                        // 비율 표기 추가
                        it.emotions
                            .filter { e -> e.score > 0 }
                            .sortedByDescending { e -> e.score }
                            .forEach { emotion ->
                                val emoji = getEmotionEmoji(emotion.emotion)
                                val percent = (emotion.score * 100).toInt()
                                Text(
                                    text = "$emoji ${emotion.emotion} ${percent}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { showRaw = !showRaw },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(text = if (showRaw) "원본 숨기기" else "원본 보기")
                }

                AnimatedVisibility(visible = showRaw) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = it.rawDiary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showEditDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("수정하기", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("삭제하기", color = Color.White)
                }
            }
        }
    }
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


@Composable
fun EmotionPieChart(
    emotions: List<Emotion>,
    modifier: Modifier = Modifier,
    radius: Float = 150f
) {
    val filtered = emotions.filter { it.score > 0 }
    val total = filtered.sumOf { it.score.toDouble() }.toFloat()

    val emotionColorMap = mapOf(
        "슬픔" to Color(0xFF90CAF9),
        "분노" to Color(0xFFEF9A9A),
        "기대감" to Color(0xFFFFF59D),
        "불안" to Color(0xFFCE93D8),
        "놀람" to Color(0xFFFFCC80),
        "기쁨" to Color(0xFFA5D6A7),
        "평온함" to Color(0xFFB0BEC5)
    )

    val emotionMap = filtered.map { it to (emotionColorMap[it.emotion] ?: Color.Gray) }

    Canvas(modifier = modifier.height(300.dp).fillMaxWidth()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        var startAngle = -90f

        emotionMap.forEach { (emotion, color) ->
            val sweep = ((emotion.score / total) * 360f).toFloat()
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2)
            )
            startAngle += sweep
        }

        var labelAngle = -90f
        emotionMap.forEach { (emotion, _) ->
            val sweep = ((emotion.score / total) * 360f).toFloat()
            val percent = (emotion.score * 100).toInt()
            if (percent >= 30) {
                val angle = labelAngle + sweep / 2
                val rad = Math.toRadians(angle.toDouble())

                val labelX = (centerX + cos(rad) * (radius + 20)).toFloat()
                val labelY = (centerY + sin(rad) * (radius + 20)).toFloat()

                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        "${emotion.emotion} $percent%",
                        labelX,
                        labelY,
                        android.graphics.Paint().apply {
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 36f
                            isFakeBoldText = true
                            color = android.graphics.Color.DKGRAY
                        }
                    )
                }
            }
            labelAngle += sweep
        }
    }

}


