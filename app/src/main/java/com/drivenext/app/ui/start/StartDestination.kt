package com.drivenext.app.ui.start

import android.app.Activity
import com.drivenext.app.ui.main.MainActivity
import com.drivenext.app.ui.noconnection.NoConnectionActivity
import com.drivenext.app.ui.onboarding.OnboardingActivity
import com.drivenext.app.ui.welcome.WelcomeActivity

enum class StartDestination {
    NO_CONNECTION,
    ONBOARDING,
    WELCOME,
    MAIN
}

val StartDestination.screen: Class<out Activity>
    get() = when (this) {
        StartDestination.NO_CONNECTION -> NoConnectionActivity::class.java
        StartDestination.ONBOARDING -> OnboardingActivity::class.java
        StartDestination.WELCOME -> WelcomeActivity::class.java
        StartDestination.MAIN -> MainActivity::class.java
    }
