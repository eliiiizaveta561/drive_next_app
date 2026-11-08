package com.drivenext.app.domain.repository

import com.drivenext.app.domain.model.RegistrationData

interface AuthRepository {

    suspend fun signIn(email: String, password: String): Result<Unit>

    suspend fun signUp(registration: RegistrationData): Result<Unit>
}
