package com.shujinko.app.data.Item

data class SuggestionRequest(
    val rawDiary: String,
    val diaryDate: String
)

data class SuggestionResponse (
    val suggestion: String
)