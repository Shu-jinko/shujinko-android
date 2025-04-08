package com.shujinko.app.data

import android.net.Uri

data class ImageItem(
    val uri: Uri,
    val name: String,
    val sizeKb: Long,
    val dateTaken: String?,          //촬영 시각
    val latitude: Double?,           //위도
    val longitude: Double?           //경도
)