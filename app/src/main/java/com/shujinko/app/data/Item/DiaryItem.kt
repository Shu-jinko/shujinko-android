package com.shujinko.app.data.Item

data class DiaryRequest(
    val rawDiary: String,
    val diaryDate: String
)
data class DiaryUpdate(
    val rawDiary: String
)

data class DiaryResponse(
    val diaryId: Long,
    val rawDiary: String,
    val rephrasedDiary: String,
    val createdAt: String,
    val summary: String,
    val label: String,
    val keywords: List<Keyword>,
    val emotions: List<Emotion>
)

data class Keyword(
    val text: String,
    val label: String
)

data class Emotion(
    val emotion: String,
    val score: Double
)