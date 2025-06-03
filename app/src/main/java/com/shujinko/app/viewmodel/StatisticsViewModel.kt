package com.shujinko.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shujinko.app.data.Item.EmotionStat
import com.shujinko.app.data.Item.KeywordStat
import com.shujinko.app.data.Item.OneSentenceKeyword
import com.shujinko.app.data.remote.StatisticsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsService: StatisticsService
) : ViewModel() {

    private val _weekKeywords = MutableStateFlow<List<KeywordStat>>(emptyList())
    val weekKeywords: StateFlow<List<KeywordStat>> = _weekKeywords

    private val _monthKeywords = MutableStateFlow<List<KeywordStat>>(emptyList())
    val monthKeywords: StateFlow<List<KeywordStat>> = _monthKeywords

    private val _weekEmotions = MutableStateFlow<List<EmotionStat>>(emptyList())
    val weekEmotions: StateFlow<List<EmotionStat>> = _weekEmotions

    private val _monthEmotions = MutableStateFlow<List<EmotionStat>>(emptyList())
    val monthEmotions: StateFlow<List<EmotionStat>> = _monthEmotions

    private val _summary = MutableStateFlow<OneSentenceKeyword?>(null)
    val summary: StateFlow<OneSentenceKeyword?> = _summary

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadWeeklyStatistics(token: String, year: Int, month: Int, week: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bearer = "Bearer $token"

                val weekKeywordDeferred = async { statisticsService.getTopWeekKeywords(bearer, year, month, week) }
                val weekEmotionDeferred = async { statisticsService.getTopWeekEmotions(bearer, year, month, week) }

                val (weekKeywordRes, weekEmotionRes) = awaitAll(weekKeywordDeferred, weekEmotionDeferred)

                if ((weekKeywordRes as retrofit2.Response<*>).isSuccessful) {
                    val body = weekKeywordRes.body() as? List<KeywordStat>
                    _weekKeywords.value = body ?: emptyList()
                }

                if ((weekEmotionRes as retrofit2.Response<*>).isSuccessful) {
                    val body = weekEmotionRes.body() as? List<EmotionStat>
                    _weekEmotions.value = body ?: emptyList()
                }

            } catch (e: Exception) {
                Log.e("StatisticsVM", "❌ Weekly Exception: ${e.message}")
                _errorMessage.value = "주간 통계 데이터를 불러오는 중 오류 발생: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMonthlyStatistics(token: String, year: Int, month: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bearer = "Bearer $token"

                val monthKeywordDeferred = async { statisticsService.getTopMonthKeywords(bearer, year, month) }
                val monthEmotionDeferred = async { statisticsService.getTopMonthEmotions(bearer, year, month) }

                val (monthKeywordRes, monthEmotionRes) = awaitAll(monthKeywordDeferred, monthEmotionDeferred)

                if ((monthKeywordRes as retrofit2.Response<*>).isSuccessful) {
                    val body = monthKeywordRes.body() as? List<KeywordStat>
                    _monthKeywords.value = body ?: emptyList()
                }

                if ((monthEmotionRes as retrofit2.Response<*>).isSuccessful) {
                    val body = monthEmotionRes.body() as? List<EmotionStat>
                    _monthEmotions.value = body ?: emptyList()
                }

            } catch (e: Exception) {
                Log.e("StatisticsVM", "❌ Monthly Exception: ${e.message}")
                _errorMessage.value = "월간 통계 데이터를 불러오는 중 오류 발생: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadOneSentence(token: String, year: Int, month: Int, week: Int) {
        viewModelScope.launch {
            try {
                val bearer = "Bearer $token"
                val response = statisticsService.getOneSentenceSummary(bearer, year, month, week)
                if (response.isSuccessful) {
                    val body = response.body()
                    _summary.value = body
                }
            } catch (e: Exception) {
                Log.e("StatisticsVM", "❌ OneSentence Exception: ${e.message}")
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
