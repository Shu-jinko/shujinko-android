package com.shujinko.app.data.Item

data class SuggestionRequest(
    val rawDiary: String
)

data class SuggestionResponse (
    val suggestion: String
)
