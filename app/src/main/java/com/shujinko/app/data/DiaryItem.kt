package com.shujinko.app.data

data class DiaryRequest(
    val title: String,
    val content: String,
    val date: String // 예: "2025-05-25"
)

data class DiaryResponse(
    val id: Long,
    val title: String,
    val content: String,
    val date: String
)