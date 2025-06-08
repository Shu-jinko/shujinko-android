package com.shujinko.app.data.Item

data class User(
    val email: String,
    val name: String,
    val birthday: String
)

data class UpdateUserRequest(
    val name: String,
    val birthday: String
)