package com.shujinko.app.data.remote

import com.shujinko.app.data.DiaryRequest
import com.shujinko.app.data.DiaryResponse
import retrofit2.Response
import retrofit2.http.*

interface DiaryService {

    @GET("/diary")
    suspend fun getDiary(
        @Header("Authorization") token: String,
        @Query("year") year: Int,
        @Query("month") month: Int,
        @Query("day") day: Int
    ): Response<DiaryResponse>

    @GET("/diary/diaries")
    suspend fun getDiaryList(
        @Header("Authorization") token: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<List<DiaryResponse>>

    @POST("diary")
    suspend fun createDiary(
        @Header("Authorization") token: String,
        @Body request: DiaryRequest
    ): Response<Void>

    @PATCH("diary/{id}")
    suspend fun updateDiary(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: DiaryRequest
    ): Response<Void>

    @DELETE("diary/{id}")
    suspend fun deleteDiary(@Header("Authorization") token: String, @Path("id") id: Long): Response<Void>
}