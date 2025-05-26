package com.shujinko.app.data

data class DiaryRequest(
    val rawDiary: String
)

data class DiaryResponse(
    val diaryId: Long,
    val rawDiary: String,
    val rephrasedDiary: String,
    val createdAt: String,
    val summary: String,
    val label: String,
    val keywords: List<String>,
    val emotions: List<Emotion>
)

data class Emotion(
    val emotion: String,
    val score: Double
)