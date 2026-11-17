package com.drivenext.app.domain.repository

import com.drivenext.app.domain.model.UserProfile

interface ProfileRepository {

    suspend fun getProfile(): Result<UserProfile>

    /** Загружает новое фото профиля (imageUri — адрес выбранной картинки) и возвращает ссылку на него. */
    suspend fun uploadAvatar(imageUri: String): Result<String>
}
