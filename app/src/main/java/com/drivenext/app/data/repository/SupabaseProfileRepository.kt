package com.drivenext.app.data.repository

import android.content.Context
import com.drivenext.app.data.local.ImageCompressor
import com.drivenext.app.data.mapper.toUserProfile
import com.drivenext.app.data.remote.NotAuthorizedException
import com.drivenext.app.data.remote.SupabaseClientProvider
import com.drivenext.app.data.remote.runRequest
import com.drivenext.app.domain.model.UserProfile
import com.drivenext.app.domain.repository.ProfileRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseProfileRepository(private val context: Context) : ProfileRepository {

    private val client get() = SupabaseClientProvider.client
    private val profiles get() = client.postgrest.from(PROFILES_TABLE)

    override suspend fun getProfile(): Result<UserProfile> = runRequest(context) {
        val user = currentUser()
        val row = profiles.select { filter { eq("id", user.id) } }.decodeSingleOrNull<JsonObject>()
        // Если строки в profiles нет (например, триггер не создан), берём имя из метаданных регистрации
        (row ?: user.userMetadata ?: JsonObject(emptyMap())).toUserProfile(user.email.orEmpty())
    }

    override suspend fun uploadAvatar(imageUri: String): Result<String> = runRequest(context) {
        val user = currentUser()
        val jpeg = withContext(Dispatchers.IO) { ImageCompressor(context).toJpeg(imageUri) }

        // Файл кладём в свою папку {user_id}/ — так требует политика бакета avatars
        val path = "${user.id}/$AVATAR_FILE"
        val bucket = client.storage.from(AVATARS_BUCKET)
        bucket.upload(path, jpeg) {
            upsert = true
            contentType = ContentType.Image.JPEG
        }

        // Метка времени в ссылке нужна, чтобы кэш картинок показал новое фото, а не старое
        val avatarUrl = "${bucket.publicUrl(path)}?v=${System.currentTimeMillis()}"
        profiles.update(buildJsonObject { put("avatar_url", avatarUrl) }) {
            filter { eq("id", user.id) }
        }
        avatarUrl
    }

    private suspend fun currentUser(): UserInfo {
        client.auth.awaitInitialization() // сессия подгружается из памяти телефона асинхронно
        return client.auth.currentUserOrNull() ?: throw NotAuthorizedException()
    }

    private companion object {
        const val PROFILES_TABLE = "profiles"
        const val AVATARS_BUCKET = "avatars"
        const val AVATAR_FILE = "avatar.jpg"
    }
}
