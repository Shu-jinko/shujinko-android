package com.shujinko.app.data.remote

import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST

interface CalendarService {
    @POST("/calendar/load")
    suspend fun loadCalendar(
        @Header("Authorization") token: String,
    ): Response<Unit>
}