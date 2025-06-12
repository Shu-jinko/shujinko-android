package com.shujinko.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shujinko.app.data.Item.SuggestionRequest
import com.shujinko.app.data.remote.SuggestionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuggestionViewModel @Inject constructor(
    private val suggestionService: SuggestionService
) : ViewModel() {

    private val _suggestion = MutableStateFlow<String?>(null)
    val suggestion: StateFlow<String?> = _suggestion

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchSuggestion(token: String, rawDiary: String, diaryDate: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = suggestionService.createSuggestion(
                    token = "Bearer $token",
                    request = SuggestionRequest(rawDiary = rawDiary, diaryDate = diaryDate)
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    _suggestion.value = body?.suggestion
                } else {
                    _errorMessage.value = "서버 오류: ${response.code()}"
                    _suggestion.value = null
                }
            } catch (e: Exception) {
                Log.e("SuggestionVM", "❌ Exception: ${e.message}")
                _errorMessage.value = "네트워크 오류: ${e.message}"
                _suggestion.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}
