package com.shujinko.app.data.Item

data class AuthItem(
    val accessToken: String,
    val refreshToken: String? = null
)