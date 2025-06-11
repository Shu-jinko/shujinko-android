    package com.shujinko.app.data.Item

data class KeywordStat(
    val keyword: String,
    val label: String,
    val count: Int
)

data class EmotionStat(
    val emotion: String,
    val count: Int
)

data class OneSentenceKeyword(
    val sentence: String
)