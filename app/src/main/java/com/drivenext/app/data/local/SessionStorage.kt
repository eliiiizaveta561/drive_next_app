package com.drivenext.app.data.local

import android.content.Context
import androidx.core.content.edit

class SessionStorage(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isOnboardingShown: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_SHOWN, false)
        set(value) = prefs.edit { putBoolean(KEY_ONBOARDING_SHOWN, value) }

    fun saveSession(accessToken: String, expiresAtMillis: Long) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putLong(KEY_EXPIRES_AT, expiresAtMillis)
        }
    }

    fun hasActiveSession(): Boolean {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        return token != null && expiresAt > System.currentTimeMillis()
    }

    private companion object {
        const val PREFS_NAME = "drive_next"
        const val KEY_ONBOARDING_SHOWN = "onboarding_shown"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_EXPIRES_AT = "token_expires_at"
    }
}
