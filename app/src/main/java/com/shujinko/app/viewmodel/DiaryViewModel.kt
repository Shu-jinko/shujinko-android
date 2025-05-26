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

class DiaryViewModel(private val diaryService: DiaryService) : ViewModel() {

    private val _diaryList = MutableStateFlow<List<DiaryResponse>>(emptyList())
    val diaryList: StateFlow<List<DiaryResponse>> = _diaryList

    fun loadDiaryList(token: String) {
        viewModelScope.launch {
            val response = diaryService.getDiaryList("Bearer $token")
            if (response.isSuccessful) {
                _diaryList.value = response.body() ?: emptyList()
            } else {
                Log.e("DiaryViewModel", "Failed to load diary list: ${response.code()}")
            }
        }
    }

    fun createDiary(token: String, request: DiaryRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val response = diaryService.createDiary("Bearer $token", request)
            if (response.isSuccessful) {
                onSuccess()
                loadDiaryList(token)
            } else {
                Log.e("DiaryViewModel", "Failed to create diary: ${response.code()}")
            }
        }
    }

    fun updateDiary(token: String, id: Long, request: DiaryRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val response = diaryService.updateDiary("Bearer $token", id, request)
            if (response.isSuccessful) {
                onSuccess()
                loadDiaryList(token)
            } else {
                Log.e("DiaryViewModel", "Failed to update diary: ${response.code()}")
            }
        }
    }

    fun deleteDiary(token: String, id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val response = diaryService.deleteDiary("Bearer $token", id)
            if (response.isSuccessful) {
                onSuccess()
                loadDiaryList(token)
            } else {
                Log.e("DiaryViewModel", "Failed to delete diary: ${response.code()}")
            }
        }
    }
}