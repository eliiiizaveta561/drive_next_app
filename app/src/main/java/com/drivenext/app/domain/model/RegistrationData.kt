package com.drivenext.app.domain.model

data class RegistrationData(
    val email: String,
    val password: String,
    val lastName: String,
    val firstName: String,
    val middleName: String,
    val birthDate: String,
    val gender: Gender,
    val licenseNumber: String,
    val licenseIssueDate: String
)
