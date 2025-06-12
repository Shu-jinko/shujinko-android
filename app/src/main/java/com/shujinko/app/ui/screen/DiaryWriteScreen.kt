package com.shujinko.app.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.shujinko.app.ui.components.TopTitle
import com.shujinko.app.viewmodel.DiaryViewModel
import com.shujinko.app.viewmodel.SuggestionViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.shujinko.app.ui.theme.PrimaryPurple
import com.shujinko.app.ui.theme.Pretendard

@OptIn(FlowPreview::class, ExperimentalMaterial3Api::class)
@Composable
fun DiaryWriteScreen(
    navController: NavController,
    parentNavController: NavController,
    diaryViewModel: DiaryViewModel,
    suggestionViewModel: SuggestionViewModel,
    token: String,
    isEditMode: Boolean = false,
    diaryId: Long? = null,
    initialText: String = "" ,
    diaryDate: LocalDate = LocalDate.now()
) {
    var text by remember { mutableStateOf(initialText) }
    var hasNavigated by remember { mutableStateOf(false) }
    var lastSuggestedText by remember { mutableStateOf("") }
    var typingDelayProgress by remember { mutableFloatStateOf(0f) }
    var selectedDiaryDate by remember { mutableStateOf(diaryDate) }

    val isLoading by diaryViewModel.isLoading.collectAsState()
    val errorMessage by diaryViewModel.errorMessage.collectAsState()
    val navigateState by diaryViewModel.shouldNavigate.collectAsState()
    val writeCompleted by diaryViewModel.writeCompleted.collectAsState()
    val suggestion by suggestionViewModel.suggestion.collectAsState()
    val suggestionLoading by suggestionViewModel.isLoading.collectAsState()
    val suggestionError by suggestionViewModel.errorMessage.collectAsState()

    val diaryDateStr = diaryDate.toString()

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
            diaryViewModel.getDiary(
                token,
                diaryDate.year,
                diaryDate.monthValue,
                diaryDate.dayOfMonth
            ) { success, diary ->
                diary?.paragraph?.forEach { para ->
                    if (!para.matched_image.isNullOrBlank()) {
                        val imageUrl =
                            "http://43.201.212.34:8080/diary/images/" + para.matched_image.substringAfterLast(
                                "/"
                            )
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
                navController.navigate(
                    "diary_result/${selectedDiaryDate.year}/${selectedDiaryDate.monthValue}/${selectedDiaryDate.dayOfMonth}"
                )   {
                    launchSingleTop = true
                }
                diaryViewModel.resetWriteCompleted()
            }
        }
    }

    LaunchedEffect(Unit) {
        suggestionViewModel.fetchSuggestion(token, "", diaryDateStr)
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
            suggestionViewModel.fetchSuggestion(token, text, diaryDateStr)
            lastSuggestedText = text
        }
        typingDelayProgress = 0f
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, "일기 작성에 문제가 발생했습니다. 관리자에 문의해주세요", Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(suggestionError) {
        suggestionError?.let {
            Toast.makeText(context, "일기 어시스턴스 제안이 불가합니다. 관리자에 문의해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    fun TypingDots(modifier: Modifier = Modifier) {
        var dotCount by remember { mutableStateOf(0) }

        LaunchedEffect(Unit) {
            while (true) {
                dotCount = (dotCount + 1) % 4
                delay(400)
            }
        }

        Text(
            text = "AI가 제안을 작성 중" + ".".repeat(dotCount),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = modifier
        )
    }


    TopTitle(title = if (isEditMode) "일기 수정" else "일기 작성", bottomBar = {
        val coroutineScope = rememberCoroutineScope()

        Surface(
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val newImageFiles = imageUris.filter { !it.toString().startsWith("http") }
                                .mapNotNull { uri ->
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

                            val serverFileNames = imageUris
                                .filter { it.toString().startsWith("http") }
                                .mapNotNull { uri -> uri.toString().substringAfterLast("/").ifBlank { null } }

                            val downloadedFiles = diaryViewModel.fetchImagesAsFiles(
                                token = token,
                                fileNames = serverFileNames,
                                cacheDir = context.cacheDir
                            )

                            val allFiles = downloadedFiles + newImageFiles
                            val diaryDateStr = diaryDate.toString()

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
                                    diaryDate = diaryDateStr,
                                    imageFiles = allFiles,
                                    onSuccess = { println("✅ 작성 완료") },
                                    onFailure = { println("❌ 작성 실패: $it") }
                                )
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEditMode) "수정 중..." else "작성 중...",
                                fontFamily = Pretendard,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    } else {
                        Text(
                            text = if (isEditMode) "수정 완료" else "작성 완료",
                            fontFamily = Pretendard,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
    ) {
        // 로딩 인디케이터
        if (suggestionLoading) {
            TypingDots(modifier = Modifier.padding(bottom = 8.dp))
            Spacer(modifier = Modifier.height(12.dp))
        } else if (typingDelayProgress > 0f) {
            TypingDots(modifier = Modifier.padding(bottom = 8.dp))
            LinearProgressIndicator(
                progress = typingDelayProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .padding(bottom = 8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        suggestion?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEAF6FF)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // 앞에 아이콘
                    IconButton(
                        onClick = {
                            suggestionViewModel.fetchSuggestion(token, text, diaryDateStr)
                            lastSuggestedText = text
                        },
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp, top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = "AI 제안 새로고침",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // 텍스트
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 5,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 200.dp),
            placeholder = {
                Text(
                    "오늘 하루를 자유롭게 적어보세요!",
                    fontFamily = Pretendard,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                containerColor = Color(0xFFFDFDFD)
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontFamily = Pretendard
            ),
            singleLine = false,
            maxLines = 10
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF0F0FF),        // 연보라 배경
                    contentColor = PrimaryPurple               // 텍스트 & 아이콘 색상
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier.height(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = "사진 추가",
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 4.dp),
                    tint = PrimaryPurple
                )
                Text(
                    text = "사진 추가",
                    fontFamily = Pretendard,
                    fontSize = 14.sp,
                    color = PrimaryPurple
                )
            }



            if (imageUris.isNotEmpty()) {
                Text(
                    text = "${imageUris.size}장 선택됨",
                    fontFamily = Pretendard,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(end = 4.dp)
                )
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
                        modifier = Modifier.align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape).size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "삭제",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}