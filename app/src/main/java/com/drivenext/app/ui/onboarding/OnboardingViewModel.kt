package com.drivenext.app.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.drivenext.app.data.local.SessionStorage

class OnboardingViewModel(app: Application) : AndroidViewModel(app) {

    private val sessionStorage = SessionStorage(app)

    fun finishOnboarding() {
        sessionStorage.isOnboardingShown = true
    }
}
