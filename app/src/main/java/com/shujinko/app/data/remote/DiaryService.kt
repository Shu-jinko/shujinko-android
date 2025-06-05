package com.shujinko.app.data.remote

import com.shujinko.app.data.Item.DiaryRequest
import com.shujinko.app.data.Item.DiaryResponse
import com.shujinko.app.data.Item.DiaryUpdate
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    @Multipart
    @POST("/diary/photoDiary")
    suspend fun uploadPhotoDiary(
        @Header("Authorization") token: String,
        @Part createParam: MultipartBody.Part,
        @Part images: List<MultipartBody.Part>
    ): Response<Void>

    @PATCH("diary/{id}")
    suspend fun updateDiary(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: DiaryUpdate
    ): Response<Void>

    @DELETE("diary/{id}")
    suspend fun deleteDiary(@Header("Authorization") token: String, @Path("id") id: Long): Response<Void>
}