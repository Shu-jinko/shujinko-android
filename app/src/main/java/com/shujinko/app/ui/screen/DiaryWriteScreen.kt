package com.shujinko.app.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shujinko.app.viewmodel.DiaryViewModel
import com.shujinko.app.viewmodel.SuggestionViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import java.time.LocalDate
import androidx.compose.foundation.lazy.items
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope


@OptIn(FlowPreview::class)
@Composable
fun DiaryWriteScreen(
    navController: NavController,
    parentNavController: NavController,
    diaryViewModel: DiaryViewModel,
    suggestionViewModel: SuggestionViewModel,
    token: String,
    isEditMode: Boolean = false,
    diaryId: Long? = null,
    initialText: String = ""
) {
    var text by remember { mutableStateOf(initialText) }
    var hasNavigated by remember { mutableStateOf(false) }
    var lastSuggestedText by remember { mutableStateOf("") }
    var typingDelayProgress by remember { mutableFloatStateOf(0f) }

    val isLoading by diaryViewModel.isLoading.collectAsState()
    val errorMessage by diaryViewModel.errorMessage.collectAsState()
    val navigateState by diaryViewModel.shouldNavigate.collectAsState()
    val writeCompleted by diaryViewModel.writeCompleted.collectAsState()
    val suggestion by suggestionViewModel.suggestion.collectAsState()
    val suggestionLoading by suggestionViewModel.isLoading.collectAsState()
    val suggestionError by suggestionViewModel.errorMessage.collectAsState()

    val context = LocalContext.current

    val imageUris = remember { mutableStateListOf<Uri>() }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            if (!imageUris.contains(uri)) imageUris.add(uri)
        }
    }

    LaunchedEffect(isEditMode, diaryId) {
        if (isEditMode && diaryId != null) {
            diaryViewModel.getDiary(token, LocalDate.now().year, LocalDate.now().monthValue, LocalDate.now().dayOfMonth) { success, diary ->
                diary?.paragraph?.forEach { para ->
                    if (!para.matched_image.isNullOrBlank()) {
                        val imageUrl = "http://43.201.212.34:8080/diary/images/" + para.matched_image.substringAfterLast("/")
                        imageUris.add(Uri.parse(imageUrl))
                    }
                }
                text = diary?.rawDiary ?: ""
            }
        }
    }

    LaunchedEffect(navigateState) {
        if (navigateState && !hasNavigated && coroutineContext.isActive) {
            hasNavigated = true
            parentNavController.navigate("diary_entry") {
                launchSingleTop = true
            }
            diaryViewModel.resetShouldNavigate()
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { writeCompleted }.collect {
            if (it) {
                navController.navigate("diary_result") {
                    launchSingleTop = true
                }
                diaryViewModel.resetWriteCompleted()
            }
        }
    }

    LaunchedEffect(Unit) {
        suggestionViewModel.fetchSuggestion(token, "")
        lastSuggestedText = ""
    }

    LaunchedEffect(text) {
        if (text.isBlank()) {
            typingDelayProgress = 0f
            return@LaunchedEffect
        }
        var elapsed = 0
        while (elapsed < 3000) {
            delay(100)
            elapsed += 100
            typingDelayProgress = elapsed / 3000f
        }
        if (text != lastSuggestedText) {
            suggestionViewModel.fetchSuggestion(token, text)
            lastSuggestedText = text
        }
        typingDelayProgress = 0f
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(suggestionError) {
        suggestionError?.let {
            Toast.makeText(context, "AI 제안 오류: $it", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = if (isEditMode) "일기를 수정하세요" else "일기를 입력하세요",
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (suggestionLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
            } else if (typingDelayProgress > 0f) {
                LinearProgressIndicator(typingDelayProgress, Modifier.fillMaxWidth().padding(bottom = 8.dp))
            }
            suggestion?.let {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF6FF))
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text("✍️ AI 제안: $it", Modifier.align(Alignment.CenterStart).padding(16.dp), fontSize = 14.sp)
                        IconButton(
                            onClick = {
                                suggestionViewModel.fetchSuggestion(token, text)
                                lastSuggestedText = text
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Outlined.Lightbulb, contentDescription = "AI 제안 새로고침")
                        }
                    }
                }
            }
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().height(300.dp),
                placeholder = { Text("오늘 하루를 자유롭게 적어보세요!") }
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("사진 추가")
            }
            if (imageUris.isNotEmpty()) {
                Text("${imageUris.size}장 선택됨", Modifier.align(Alignment.CenterVertically))
            }
        }

        LazyRow(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            items(imageUris) { uri ->
                Box(Modifier.padding(end = 8.dp).size(80.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "이미지",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                    )
                    IconButton(
                        onClick = { imageUris.remove(uri) },
                        modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.5f), CircleShape).size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "삭제", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        val coroutineScope = rememberCoroutineScope()

        Button(
            onClick = {
                coroutineScope.launch {
                    val todayStr = LocalDate.now().toString()

                    // 새로 선택한 이미지 (갤러리에서 고른 content:// 등)
                    val newImageFiles = imageUris.filter { !it.toString().startsWith("http") }.mapNotNull { uri ->
                        try {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val tempFile = kotlin.io.path.createTempFile(suffix = ".jpg").toFile()
                            inputStream?.use { input -> tempFile.outputStream().use { input.copyTo(it) } }
                            tempFile
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }

                    // 기존 서버 이미지 처리
                    val serverFileNames = imageUris
                        .filter { it.toString().startsWith("http") }
                        .mapNotNull { uri -> uri.toString().substringAfterLast("/").ifBlank { null } }

                    val downloadedFiles = diaryViewModel.fetchImagesAsFiles(
                        token = token,
                        fileNames = serverFileNames,
                        cacheDir = context.cacheDir
                    )

                    val allFiles = downloadedFiles + newImageFiles

                    if (isEditMode && diaryId != null) {
                        diaryViewModel.updateDiaryWithImages(
                            token = token,
                            id = diaryId,
                            rawDiary = text,
                            imageFiles = allFiles,
                            onSuccess = { println("✅ 수정 성공") },
                            onFailure = { println("❌ 수정 실패: $it") }
                        )
                    } else {
                        diaryViewModel.createDiaryWithImages(
                            token = token,
                            rawDiary = text,
                            diaryDate = todayStr,
                            imageFiles = allFiles,
                            onSuccess = { println("✅ 작성 완료") },
                            onFailure = { println("❌ 작성 실패: $it") }
                        )
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(if (isEditMode) "수정 완료" else "작성 완료")
            }
        }

    }
}
