package com.shujinko.app.utils

import android.content.Context
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.InputStream


@RequiresApi(Build.VERSION_CODES.Q)
fun extractExifInfo(context: Context, uri: Uri): Triple<String?, Double?, Double?> {
    try {
        // 1차 시도: 원본 URI
        val originalUri = MediaStore.setRequireOriginal(uri)
        val stream = context.contentResolver.openInputStream(originalUri)
        if (stream != null) {
            val exif = ExifInterface(stream)
            return extractExifData(exif)
        }
    } catch (e: Exception) {
        Log.w("EXIF_FALLBACK", "원본 접근 실패, 일반 URI로 재시도: ${e.message}")
    }

    return try {
        // 2차 시도: 일반 URI
        val stream = context.contentResolver.openInputStream(uri)
        if (stream != null) {
            val exif = ExifInterface(stream)
            return extractExifData(exif)
        } else {
            Triple(null, null, null) // ✅ stream이 null인 경우
        }
    } catch (e: Exception) {
        Log.e("EXIF_FAIL", "Exif 최종 실패", e)
        Triple(null, null, null)
    }
}

fun extractExifData(exif: ExifInterface): Triple<String?, Double?, Double?> {
    val dateTaken = exif.getAttribute(ExifInterface.TAG_DATETIME)

    val latRaw = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
    val lonRaw = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
    val latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
    val lonRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)

    Log.d("EXIF_RAW", "LAT: $latRaw, LON: $lonRaw, LAT_REF: $latRef, LON_REF: $lonRef")

    val latitude = if (!latRaw.isNullOrBlank() && !latRef.isNullOrBlank() && latRaw != "0/1,0/1,0/1") {
        convertToDecimal(latRaw, latRef)
    } else null

    val longitude = if (!lonRef.isNullOrBlank() && !lonRef.isNullOrBlank() && lonRaw != "0/1,0/1,0/1") {
        convertToDecimal(lonRaw, lonRef)
    } else null

    return Triple(dateTaken, latitude, longitude)
}

fun convertToDecimal(coord: String?, ref: String?): Double? {
    if (coord == null || ref == null) return null
    val parts = coord.split(",")
    if (parts.size != 3) return null

    val d = parts[0].split("/").let { it[0].toDouble() / it[1].toDouble() }
    val m = parts[1].split("/").let { it[0].toDouble() / it[1].toDouble() }
    val s = parts[2].split("/").let { it[0].toDouble() / it[1].toDouble() }

    val decimal = d + (m / 60) + (s / 3600)
    return if (ref == "S" || ref == "W") -decimal else decimal
}
