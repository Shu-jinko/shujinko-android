package com.shujinko.app.data.Item

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

// DTOs

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
    val emotions: List<Emotion>,
    val paragraph: List<Paragraph>
)

data class Keyword(
    val text: String,
    val label: String
)

data class Emotion(
    val emotion: String,
    val score: Double
)

data class Paragraph(
    val subject: String,
    val content: String,
    val matched_image: String,
    val image_caption: String
)

// Helper function for Multipart Upload
fun createDiaryMultipart(
    rawDiaryText: String,
    diaryDateText: String,
    imageFiles: List<File>
): Pair<RequestBody, List<MultipartBody.Part>> {
    val diaryJson = JSONObject().apply {
        put("rawDiary", rawDiaryText)
        put("diaryDate", diaryDateText)
    }.toString()

    val createParamBody = diaryJson
        .toRequestBody("application/json; charset=utf-8".toMediaType())

    val imageParts = imageFiles.map { file ->
        val requestFile = file.asRequestBody("image/*".toMediaType())
        MultipartBody.Part.createFormData("images", file.name, requestFile)
    }

    return Pair(createParamBody, imageParts)
}