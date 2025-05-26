package com.shujinko.app.data

data class DiaryRequest(
    val title: String,
    val content: String,
    val date: String
)

data class DiaryResponse(
    val id: Long,
    val title: String,
    val content: String,
    val date: String
)