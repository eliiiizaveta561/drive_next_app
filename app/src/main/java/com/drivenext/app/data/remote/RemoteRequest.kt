package com.drivenext.app.data.remote

import android.content.Context
import com.drivenext.app.R
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CancellationException
import java.io.IOException

/** Выполняет запрос к Supabase и превращает любое исключение в Result.failure с понятным текстом. */
suspend fun <T> runRequest(context: Context, request: suspend () -> T): Result<T> =
    try {
        Result.success(request())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(Exception(e.toUserMessage(context), e))
    }

/** Нет сохранённой сессии Supabase — пользователя нужно отправить на вход. */
class NotAuthorizedException : IllegalStateException()

private fun Exception.toUserMessage(context: Context): String = when (this) {
    is AuthRestException -> authErrorMessage(context, this)
    is RestException -> description ?: error
    is HttpRequestException, is IOException -> context.getString(R.string.error_network)
    is NotAuthorizedException -> context.getString(R.string.error_not_authorized)
    is IllegalStateException -> message ?: context.getString(R.string.error_unknown)
    else -> context.getString(R.string.error_unknown)
}

private fun authErrorMessage(context: Context, e: AuthRestException): String {
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
