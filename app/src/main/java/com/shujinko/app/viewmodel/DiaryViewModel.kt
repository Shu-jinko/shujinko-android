package com.shujinko.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shujinko.app.data.DiaryRequest
import com.shujinko.app.data.DiaryResponse
import com.shujinko.app.data.remote.DiaryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val diaryService: DiaryService
) : ViewModel() {

    private val _diaryList = MutableStateFlow<List<DiaryResponse>>(emptyList())
    val diaryList: StateFlow<List<DiaryResponse>> = _diaryList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadDiaryList(token: String) {
        viewModelScope.launch {
            val response = diaryService.getDiaryList("Bearer $token")
            if (response.isSuccessful) {
                _diaryList.value = response.body() ?: emptyList()
            } else {
                _errorMessage.value = "일기 목록 불러오기 실패: ${response.code()}"
                Log.e("DiaryViewModel", "Failed to load diary list: ${response.code()}")
            }
        }
    }

    fun createDiary(token: String, rawDiary: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val request = DiaryRequest(rawDiary)
            val response = diaryService.createDiary("Bearer $token", request)

            if (response.isSuccessful) {
                onSuccess()
                loadDiaryList(token)
            } else {
                _errorMessage.value = "일기 작성 실패: ${response.code()}"
                Log.e("DiaryViewModel", "Failed to create diary: ${response.code()}")
            }

            _isLoading.value = false
        }
    }

    fun updateDiary(token: String, id: Long, rawDiary: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val request = DiaryRequest(rawDiary)
            val response = diaryService.updateDiary("Bearer $token", id, request)

            if (response.isSuccessful) {
                onSuccess()
                loadDiaryList(token)
            } else {
                _errorMessage.value = "일기 수정 실패: ${response.code()}"
                Log.e("DiaryViewModel", "Failed to update diary: ${response.code()}")
            }

            _isLoading.value = false
        }
    }

    fun deleteDiary(token: String, id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val response = diaryService.deleteDiary("Bearer $token", id)
            if (response.isSuccessful) {
                onSuccess()
                loadDiaryList(token)
            } else {
                _errorMessage.value = "일기 삭제 실패: ${response.code()}"
                Log.e("DiaryViewModel", "Failed to delete diary: ${response.code()}")
            }

            _isLoading.value = false
        }
    }
}