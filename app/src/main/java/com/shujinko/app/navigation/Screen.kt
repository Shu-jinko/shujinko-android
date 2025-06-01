package com.shujinko.app.navigation

enum class Screen(val route: String) {
    Splash("splash"),
    Login("login"),
    Main("main"),
    Home("home"),

    // 일기 관련
    DiaryWrite("diary_write"),  // ✅ 내부탭용
    DiaryWriteStandalone("diary_write_standalone"),  // ✅ 독립화면용
    DiaryEdit("diary_edit/{id}/{year}/{month}/{day}/{rawDiary}"),
    DeleteDiary("delete_diary/{year}/{month}/{day}"),
    DiaryResult("diary_result"),
}
