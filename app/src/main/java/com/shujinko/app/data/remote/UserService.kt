package com.shujinko.app.data.remote

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Header

interface UserService {
    @DELETE("/user")
    suspend fun deleteUser(
        @Header("Authorization") token: String
    ): Response<Void>
}
