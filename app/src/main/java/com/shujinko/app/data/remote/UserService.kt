package com.shujinko.app.data.remote

import com.shujinko.app.data.Item.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.Header
    
interface UserService {

    @GET("/user")
    suspend fun getUserInfo(
        @Header("Authorization") token: String
    ): Response<User>

    @DELETE("/user")
    suspend fun deleteUser(
        @Header("Authorization") token: String
    ): Response<Void>
}
