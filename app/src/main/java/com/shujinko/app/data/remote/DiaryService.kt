package com.shujinko.app.data.remote

import com.shujinko.app.data.DiaryRequest
import com.shujinko.app.data.DiaryResponse
import retrofit2.Response
import retrofit2.http.*

interface DiaryService {

    @GET("/diary")
    suspend fun getDiary(
        @Header("Authorization") token: String
    ): Response<DiaryResponse>

    @GET("/diary/diaries")
    suspend fun getDiaryList(
        @Header("Authorization") token: String
    ): Response<List<DiaryResponse>>

    @POST("/diary")
    suspend fun createDiary(
        @Header("Authorization") token: String,
        @Body diaryRequest: DiaryRequest
    ): Response<Unit>

    @PATCH("/diary/{id}")
    suspend fun updateDiary(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body diaryRequest: DiaryRequest
    ): Response<Unit>

    @DELETE("/diary/{id}")
    suspend fun deleteDiary(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
}
