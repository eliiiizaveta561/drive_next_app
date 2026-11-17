package com.drivenext.app.data.repository

import android.content.Context
import com.drivenext.app.data.local.SessionStorage
import com.drivenext.app.data.remote.SupabaseClientProvider
import com.drivenext.app.data.remote.runRequest
import com.drivenext.app.domain.model.RegistrationData
import com.drivenext.app.domain.repository.AuthRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseAuthRepository(private val context: Context) : AuthRepository {

    private val sessionStorage = SessionStorage(context)

    private val auth: Auth
        get() = SupabaseClientProvider.client.auth

    override suspend fun signIn(email: String, password: String): Result<Unit> = runRequest(context) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        saveCurrentSession()
    }

    override suspend fun signUp(registration: RegistrationData): Result<Unit> = runRequest(context) {
        auth.signUpWith(Email) {
            email = registration.email
            password = registration.password
            data = registration.toUserMetadata()
        }
        saveCurrentSession()
    }

    /**
     * Выход: пробуем завершить сессию на сервере, но локально данные очищаем в любом случае —
     * даже без интернета пользователь должен выйти из аккаунта.
     */
    override suspend fun signOut(): Result<Unit> {
        val serverResult = runRequest(context) { auth.signOut() }
        if (serverResult.isFailure) runRequest(context) { auth.clearSession() }
        sessionStorage.clearSession()
        return Result.success(Unit)
    }

    private fun RegistrationData.toUserMetadata(): JsonObject = buildJsonObject {
        put("last_name", lastName)
        put("first_name", firstName)
        put("middle_name", middleName)
        put("birth_date", birthDate)
        put("gender", gender.value)
        put("license_number", licenseNumber)
        put("license_issue_date", licenseIssueDate)
    }

    private fun saveCurrentSession() {
        val session = auth.currentSessionOrNull() ?: return
        val expiresAtMillis = System.currentTimeMillis() + session.expiresIn * 1000
        sessionStorage.saveSession(session.accessToken, expiresAtMillis)
    }
}
