package com.drivenext.app.ui.common

import android.graphics.ImageDecoder
import android.net.Uri
import android.widget.ImageView
import com.google.android.material.imageview.ShapeableImageView

private const val MAX_PHOTO_SIDE_PX = 512

fun ShapeableImageView.showPhoto(uri: Uri) {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    val bitmap = runCatching {
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val longestSide = maxOf(info.size.width, info.size.height)
            decoder.setTargetSampleSize(maxOf(1, longestSide / MAX_PHOTO_SIDE_PX))
        }
    }.getOrNull() ?: return

    setContentPadding(0, 0, 0, 0)
    scaleType = ImageView.ScaleType.CENTER_CROP
    setImageBitmap(bitmap)
}
