package com.shujinko.app.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.shujinko.app.data.ImageItem
import com.shujinko.app.utils.extractExifInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MultiImageViewModel : ViewModel() {

    private val _images = MutableStateFlow<List<ImageItem>>(emptyList())
    val images: StateFlow<List<ImageItem>> = _images

    @RequiresApi(Build.VERSION_CODES.Q)
    fun addImages(context: Context, uris: List<Uri>) {
        val list = uris.mapNotNull { uri ->
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                val name = cursor.getString(nameIndex)
                val size = cursor.getLong(sizeIndex) / 1024

                val (dateTaken, latitude, longitude) = extractExifInfo(context, uri)

                ImageItem(uri, name, size, dateTaken, latitude, longitude)
            }
        }
        _images.value = list
    }
}
