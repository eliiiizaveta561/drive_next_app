package com.drivenext.app.ui.common

import android.content.Context
import android.widget.ImageView
import androidx.annotation.DrawableRes
import coil.ImageLoader
import coil.load
import okhttp3.OkHttpClient

/**
 * Один загрузчик картинок (Coil) на всё приложение: кэш в памяти и на диске.
 * Wikimedia, откуда берутся фото машин, требует понятный User-Agent — добавляем его к каждому запросу.
 */
object RemoteImages {

    private const val USER_AGENT = "DriveNextApp/1.0 (Android; student lab project)"

    @Volatile
    private var imageLoader: ImageLoader? = null

    fun loader(context: Context): ImageLoader =
        imageLoader ?: synchronized(this) {
            imageLoader ?: buildLoader(context.applicationContext).also { imageLoader = it }
        }

    private fun buildLoader(context: Context): ImageLoader = ImageLoader.Builder(context)
        .okHttpClient {
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder().header("User-Agent", USER_AGENT).build()
                    chain.proceed(request)
                }
                .build()
        }
        .crossfade(true)
        .build()
}

/** Загружает картинку по ссылке; пока грузится или при ошибке показывает заглушку. */
fun ImageView.loadRemote(url: String?, @DrawableRes placeholder: Int) {
    load(url, RemoteImages.loader(context)) {
        placeholder(placeholder)
        error(placeholder)
        fallback(placeholder)
    }
}
