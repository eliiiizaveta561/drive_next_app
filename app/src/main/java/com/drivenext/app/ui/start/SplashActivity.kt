package com.drivenext.app.ui.start

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.drivenext.app.databinding.ActivitySplashBinding
import com.drivenext.app.ui.common.openScreen

class SplashActivity : AppCompatActivity() {

    private val viewModel: StartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(ActivitySplashBinding.inflate(layoutInflater).root)

        viewModel.destination.observe(this) { destination ->
            openScreen(destination.screen)
            finish()
        }

        if (savedInstanceState == null) {
            viewModel.chooseDestination(delayMillis = SPLASH_DURATION_MILLIS)
        }
    }

    private companion object {
        const val SPLASH_DURATION_MILLIS = 2500L
    }
}
