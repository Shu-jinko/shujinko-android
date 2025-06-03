package com.shujinko.app.data.remote

import com.shujinko.app.data.Item.EmotionStat
import com.shujinko.app.data.Item.KeywordStat
import com.shujinko.app.data.Item.OneSentenceKeyword
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface StatisticsService {

    @GET("/statistics/topWeekKeywords")
    suspend fun getTopWeekKeywords(
        @Header("Authorization") token: String,
        @Query("year") year: Int,
        @Query("month") month: Int,
        @Query("week") day: Int
    ): Response<List<KeywordStat>>

    @GET("/statistics/topWeekEmotions")
    suspend fun getTopWeekEmotions(
        @Header("Authorization") token: String,
        @Query("year") year: Int,
        @Query("month") month: Int,
        @Query("week") day: Int
    ): Response<List<EmotionStat>>

    @GET("/statistics/topMonthKeywords")
    suspend fun getTopMonthKeywords(
        @Header("Authorization") token: String,
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): Response<List<KeywordStat>>

    @GET("/statistics/topMonthEmotions")
    suspend fun getTopMonthEmotions(
        @Header("Authorization") token: String,
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): Response<List<EmotionStat>>

    @GET("/statistics/oneSentenceKeyword")
    suspend fun getOneSentenceSummary(
        @Header("Authorization") token: String,
        @Query("year") year: Int,
        @Query("month") month: Int,
        @Query("week") day: Int
    ): Response<OneSentenceKeyword>
}
