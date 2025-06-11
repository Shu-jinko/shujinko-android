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

// 새로 추가된 day7, day30 StateFlow들
private val _day7Keywords = MutableStateFlow<List<KeywordStat>>(emptyList())
val day7Keywords: StateFlow<List<KeywordStat>> = _day7Keywords

private val _day7Emotions = MutableStateFlow<List<EmotionStat>>(emptyList())
val day7Emotions: StateFlow<List<EmotionStat>> = _day7Emotions

private val _day30Keywords = MutableStateFlow<List<KeywordStat>>(emptyList())
val day30Keywords: StateFlow<List<KeywordStat>> = _day30Keywords

private val _day30Emotions = MutableStateFlow<List<EmotionStat>>(emptyList())
val day30Emotions: StateFlow<List<EmotionStat>> = _day30Emotions

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

// 새로 추가된 day7 통계 로드 함수
fun loadDay7Statistics(token: String) {
    viewModelScope.launch {
        _isLoading.value = true
        try {
            val bearer = "Bearer $token"

            val day7KeywordDeferred = async { statisticsService.getDay7Keywords(bearer) }
            val day7EmotionDeferred = async { statisticsService.getDay7Emotions(bearer) }

            val (day7KeywordRes, day7EmotionRes) = awaitAll(day7KeywordDeferred, day7EmotionDeferred)

            if ((day7KeywordRes as retrofit2.Response<*>).isSuccessful) {
                val body = day7KeywordRes.body() as? List<KeywordStat>
                _day7Keywords.value = body ?: emptyList()
            }

            if ((day7EmotionRes as retrofit2.Response<*>).isSuccessful) {
                val body = day7EmotionRes.body() as? List<EmotionStat>
                _day7Emotions.value = body ?: emptyList()
            }

        } catch (e: Exception) {
            Log.e("StatisticsVM", "❌ Day7 Exception: ${e.message}")
            _errorMessage.value = "최근 7일 통계 데이터를 불러오는 중 오류 발생: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
}

// 새로 추가된 day30 통계 로드 함수
fun loadDay30Statistics(token: String) {
    viewModelScope.launch {
        _isLoading.value = true
        try {
            val bearer = "Bearer $token"

            val day30KeywordDeferred = async { statisticsService.getDay30Keywords(bearer) }
            val day30EmotionDeferred = async { statisticsService.getDay30Emotions(bearer) }

            val (day30KeywordRes, day30EmotionRes) = awaitAll(day30KeywordDeferred, day30EmotionDeferred)

            if ((day30KeywordRes as retrofit2.Response<*>).isSuccessful) {
                val body = day30KeywordRes.body() as? List<KeywordStat>
                _day30Keywords.value = body ?: emptyList()
            }

            if ((day30EmotionRes as retrofit2.Response<*>).isSuccessful) {
                val body = day30EmotionRes.body() as? List<EmotionStat>
                _day30Emotions.value = body ?: emptyList()
            }

        } catch (e: Exception) {
            Log.e("StatisticsVM", "❌ Day30 Exception: ${e.message}")
            _errorMessage.value = "최근 30일 통계 데이터를 불러오는 중 오류 발생: ${e.message}"
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