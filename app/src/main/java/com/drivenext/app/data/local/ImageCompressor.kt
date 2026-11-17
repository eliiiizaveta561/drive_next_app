package com.drivenext.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.core.net.toUri
import java.io.ByteArrayOutputStream

/** Читает выбранное фото по Uri, уменьшает его и сжимает в JPEG перед отправкой на сервер. */
class ImageCompressor(private val context: Context) {

    fun toJpeg(imageUri: String): ByteArray {
        val source = ImageDecoder.createSource(context.contentResolver, imageUri.toUri())
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val longestSide = maxOf(info.size.width, info.size.height)
            decoder.setTargetSampleSize(maxOf(1, longestSide / MAX_SIDE_PX))
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
            stream.toByteArray()
        }
    }

    private companion object {
        const val MAX_SIDE_PX = 512
        const val JPEG_QUALITY = 85
    }
}
