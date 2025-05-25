package com.shujinko.app.data

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String? = null
)