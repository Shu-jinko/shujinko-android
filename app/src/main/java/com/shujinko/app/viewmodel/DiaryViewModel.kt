package com.shujinko.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shujinko.app.data.Item.DiaryRequest
import com.shujinko.app.data.Item.DiaryResponse
import com.shujinko.app.data.remote.DiaryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.shujinko.app.data.Item.DiaryUpdate
import com.shujinko.app.data.Item.createDiaryMultipartParts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.io.use


@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val diaryService: DiaryService
) : ViewModel() {

    private val _diaryList = MutableStateFlow<List<DiaryResponse>>(emptyList())
    val diaryList: StateFlow<List<DiaryResponse>> = _diaryList

    private val _diaryMap = MutableStateFlow<Map<LocalDate, DiaryResponse>>(emptyMap())
    val diaryMap: StateFlow<Map<LocalDate, DiaryResponse>> = _diaryMap

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _todayDiary = MutableStateFlow<DiaryResponse?>(null)
    val todayDiary: StateFlow<DiaryResponse?> = _todayDiary

    private val _shouldNavigate = MutableStateFlow(false)
    val shouldNavigate: StateFlow<Boolean> = _shouldNavigate

    fun resetShouldNavigate() {
        _shouldNavigate.value = false
    }

    private val _writeCompleted = MutableStateFlow(false)
    val writeCompleted: StateFlow<Boolean> = _writeCompleted

    fun resetWriteCompleted() {
        _writeCompleted.value = false
    }

    private val _initialRoute = MutableStateFlow<String?>(null)
    val initialRoute: StateFlow<String?> = _initialRoute

    fun checkTodayDiaryAndDecideRoute(token: String, year: Int, month: Int, day: Int) {
        viewModelScope.launch {
            try {
                val response = diaryService.getDiary("Bearer $token", year, month, day)
                _initialRoute.value = if (response.isSuccessful) "diary_result" else "diary_write"
            } catch (e: Exception) {
                _initialRoute.value = "diary_write" // 네트워크 오류 시에도 작성 화면으로
            }
        }
    }

    fun resetInitialRoute() {
        _initialRoute.value = null
    }

    fun getDiary(
        token: String,
        year: Int,
        month: Int,
        day: Int,
        onComplete: (Boolean, DiaryResponse?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = diaryService.getDiary("Bearer $token", year, month, day)
                if (response.isSuccessful) {
                    val diary = response.body()
                    _todayDiary.value = diary
                    Log.d("DiaryViewModel", "오름 일기 불러오기 성공")
                    diary?.let {
                        val date = LocalDate.of(year, month, day)
                        _diaryMap.value = _diaryMap.value.toMutableMap().apply {
                            put(date, it)
                        }
                    }
                    onComplete(true, diary)
                } else {
                    _todayDiary.value = null
                    val msg = "오름 일기 불러오기 실패: ${response.code()}"
                    _errorMessage.value = msg
                    Log.e("DiaryViewModel", msg)
                    onComplete(false, null)
                }
            } catch (e: Exception) {
                Log.e("DiaryViewModel", "getDiary 예제 발생", e)
                _errorMessage.value = "네트워크 오류 발생"
                _todayDiary.value = null
                onComplete(false, null)
            }

            _isLoading.value = false
        }
    }

    fun loadDiaryList(token: String, year: Int, month: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = diaryService.getDiaryList("Bearer $token", year, month)
                if (response.isSuccessful) {
                    val diaries = response.body() ?: emptyList()
                    _diaryList.value = diaries
                    _diaryMap.value = diaries.associateBy {
                        LocalDate.parse(it.createdAt.substringBefore("T"))
                    }
                    Log.d("DiaryViewModel", "일기 목록 불러오기 성공")
                } else {
                    _errorMessage.value = "일기 목록 불러오기 실패: ${response.code()}"
                    Log.e("DiaryViewModel", "Failed to load diary list: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류 발생"
                Log.e("DiaryViewModel", "loadDiaryList 예제", e)
            }

            _isLoading.value = false
        }
    }

    fun fetchDiaryImage(
        token: String,
        fileName: String,
        onSuccess: (ByteArray) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = diaryService.getDiaryImage("Bearer $token", fileName)
                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes()
                    if (bytes != null) {
                        onSuccess(bytes)
                    } else {
                        onFailure("이미지 데이터가 없습니다.")
                    }
                } else {
                    onFailure("이미지 요청 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("DiaryViewModel", "fetchDiaryImage 에러", e)
                onFailure("네트워크 오류: ${e.localizedMessage}")
            }
        }
    }

    fun createDiary(
        token: String,
        rawDiary: String,
        diaryDate: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _writeCompleted.value = false

            val request = DiaryRequest(rawDiary = rawDiary, diaryDate = diaryDate)

            try {
                val response = diaryService.createDiary("Bearer $token", request)

                if (response.isSuccessful) {
                    Log.d("DiaryViewModel", "✅ 일기 작성 성공")
                    _writeCompleted.value = true
                    onSuccess()
                } else {
                    val msg = "❌ 일기 작성 실패: ${response.code()}"
                    Log.e("DiaryViewModel", msg)
                    onFailure(msg)
                }

            } catch (e: Exception) {
                val error = "🔥 네트워크 에러: ${e.localizedMessage}"
                Log.e("DiaryViewModel", error)
                onFailure(error)
            }

            _isLoading.value = false
        }
    }

    fun createDiaryWithImages(
        token: String,
        rawDiary: String,
        diaryDate: String,
        imageFiles: List<File>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _writeCompleted.value = false

            val (createParamPart, imageParts) = createDiaryMultipartParts(rawDiary, diaryDate, imageFiles)

            try {
                val response = diaryService.uploadPhotoDiary(
                    token = "Bearer $token",
                    createParam = createParamPart,
                    images = imageParts
                )
                if (response.isSuccessful) {
                    _writeCompleted.value = true
                    onSuccess()
                } else {
                    onFailure("일기+이미지 작성 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                onFailure("네트워크 오류: ${e.localizedMessage}")
            }

            _isLoading.value = false
        }
    }

    fun updateDiaryWithImages(
        token: String,
        id: Long,
        rawDiary: String,
        imageFiles: List<File>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // JSON 문자열 생성
                val updateJson = JSONObject().apply {
                    put("rawDiary", rawDiary)
                }.toString()

                val updateRequestBody = updateJson
                    .toRequestBody("application/json".toMediaType())
                val updateParamPart = MultipartBody.Part.createFormData("updateParam", null, updateRequestBody)

                // 이미지들 처리
                val imageParts = imageFiles.map { file ->
                    val requestFile = file.asRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("images", file.name, requestFile)
                }

                val response = diaryService.updatePhotoDiary(
                    token = "Bearer $token",
                    id = id,
                    updateParam = updateParamPart,
                    images = imageParts
                )

                if (response.isSuccessful) {
                    _writeCompleted.value = true
                    onSuccess()
                } else {
                    onFailure("❌ 수정 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                onFailure("🔥 네트워크 오류: ${e.localizedMessage}")
            }

            _isLoading.value = false
        }
    }

    fun deleteDiary(token: String, diaryId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = diaryService.deleteDiary("Bearer $token", diaryId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    Log.e("DiaryViewModel", "일기 삭제 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("DiaryViewModel", "일기 삭제 예제", e)
            }
        }
    }

    suspend fun fetchImagesAsFiles(
        token: String,
        fileNames: List<String>,
        cacheDir: File
    ): List<File> = withContext(Dispatchers.IO) {
        fileNames.mapNotNull { fileName ->
            try {
                val response = diaryService.getDiaryImage("Bearer $token", fileName)
                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes()
                    if (bytes != null) {
                        val file = File(cacheDir, fileName)
                        FileOutputStream(file).use { it.write(bytes) }
                        file
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("DiaryViewModel", "fetchImagesAsFiles 실패: $fileName", e)
                null
            }
        }
    }

}