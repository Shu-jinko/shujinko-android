package com.shujinko.app.data.remote

import com.shujinko.app.data.Item.UpdateUserRequest
import com.shujinko.app.data.Item.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.Header

interface UserService {

    @GET("/user")
    suspend fun getUserInfo(
        @Header("Authorization") token: String
    ): Response<User>

    @PATCH("/user")
    suspend fun updateUserInfo(
        @Header("Authorization") token: String,
        @Body request:  UpdateUserRequest
    ): Response<User>

    @DELETE("/user")
    suspend fun deleteUser(
        @Header("Authorization") token: String
    ): Response<Void>
}
