package com.shujinko.app.data

import android.net.Uri

data class ImageItem(
    val uri: Uri,
    val name: String,
    val sizeKb: Long
)
