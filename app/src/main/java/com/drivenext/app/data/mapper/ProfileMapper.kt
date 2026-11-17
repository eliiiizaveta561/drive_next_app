package com.drivenext.app.data.mapper

import com.drivenext.app.domain.model.Gender
import com.drivenext.app.domain.model.UserProfile
import kotlinx.serialization.json.JsonObject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

/** Строка таблицы profiles → UserProfile. Email берём из auth, если в строке его нет. */
fun JsonObject.toUserProfile(fallbackEmail: String): UserProfile = UserProfile(
    firstName = string("first_name"),
    lastName = string("last_name"),
    email = string("email").ifBlank { fallbackEmail },
    gender = Gender.entries.firstOrNull { it.value == string("gender") },
    avatarUrl = string("avatar_url").ifBlank { null },
    registeredAt = parseTimestamp(string("created_at"))
)

/** PostgREST отдаёт timestamptz как «2026-11-08T18:53:16.123+00:00». */
internal fun parseTimestamp(text: String): LocalDate? =
    try {
        OffsetDateTime.parse(text).toLocalDate()
    } catch (e: DateTimeParseException) {
        null
    }
