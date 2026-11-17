package com.drivenext.app.domain.model

import java.time.LocalDate

data class UserProfile(
    val firstName: String,
    val lastName: String,
    val email: String,
    val gender: Gender?,
    val avatarUrl: String?,
    val registeredAt: LocalDate?
) {
    val fullName: String get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
}
