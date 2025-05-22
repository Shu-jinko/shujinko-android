package com.shujinko.app.data.remote

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String? = null
)