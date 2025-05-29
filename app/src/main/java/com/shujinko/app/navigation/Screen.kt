package com.shujinko.app.navigation

enum class Screen(val route: String) {
    Splash("splash"),
    Login("login"),
    Main("main"),
    Home("home"),
    DiaryWrite("diary_write"),
    DiaryEdit("diary_edit/{id}/{year}/{month}/{day}/{rawDiary}"),
    DeleteDiary("delete_diary/{year}/{month}/{day}")
}