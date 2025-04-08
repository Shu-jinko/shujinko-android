package com.shujinko.app.utils

import android.content.Context
import android.media.ExifInterface
import android.net.Uri
import java.io.InputStream

fun extractExifInfo(context: Context, uri: Uri): Triple<String?, Double?, Double?> {
    return try {
        val stream: InputStream? = context.contentResolver.openInputStream(uri)
        val exif = stream?.let { ExifInterface(it) }

        val dateTaken = exif?.getAttribute(ExifInterface.TAG_DATETIME)

        val lat = exif?.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
        val lon = exif?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
        val latRef = exif?.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
        val lonRef = exif?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)

        val latitude = convertToDecimal(lat, latRef)
        val longitude = convertToDecimal(lon, lonRef)

        Triple(dateTaken, latitude, longitude)
    } catch (e: Exception) {
        Triple(null, null, null)
    }
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
