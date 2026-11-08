package com.drivenext.app.data.repository

import android.content.Context
import com.drivenext.app.R
import com.drivenext.app.data.local.SessionStorage
import com.drivenext.app.data.remote.SupabaseClientProvider
import com.drivenext.app.domain.model.RegistrationData
import com.drivenext.app.domain.repository.AuthRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.IOException

class SupabaseAuthRepository(private val context: Context) : AuthRepository {

    private val sessionStorage = SessionStorage(context)

    private val auth: Auth
        get() = SupabaseClientProvider.client.auth

    override suspend fun signIn(email: String, password: String): Result<Unit> = runRequest {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        saveCurrentSession()
    }

    override suspend fun signUp(registration: RegistrationData): Result<Unit> = runRequest {
        auth.signUpWith(Email) {
            email = registration.email
            password = registration.password
            data = registration.toUserMetadata()
        }
        saveCurrentSession()
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

    private inline fun runRequest(request: () -> Unit): Result<Unit> =
        try {
            request()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage(), e))
        }

    private fun Exception.toUserMessage(): String = when (this) {
        is AuthRestException -> authErrorMessage(this)
        is RestException -> description ?: error
        is IOException -> context.getString(R.string.error_network)
        is IllegalStateException -> message ?: context.getString(R.string.error_unknown)
        else -> context.getString(R.string.error_unknown)
    }

    private fun authErrorMessage(e: AuthRestException): String {
        val messageRes = when (e.errorCode) {
            AuthErrorCode.InvalidCredentials -> R.string.error_invalid_credentials
            AuthErrorCode.UserAlreadyExists, AuthErrorCode.EmailExists -> R.string.error_user_exists
            AuthErrorCode.WeakPassword -> R.string.error_weak_password
            AuthErrorCode.EmailNotConfirmed -> R.string.error_email_not_confirmed
            AuthErrorCode.EmailAddressInvalid -> R.string.error_email
            AuthErrorCode.OverRequestRateLimit, AuthErrorCode.OverEmailSendRateLimit -> R.string.error_rate_limit
            else -> return e.errorDescription
        }
        return context.getString(messageRes)
    }
}
