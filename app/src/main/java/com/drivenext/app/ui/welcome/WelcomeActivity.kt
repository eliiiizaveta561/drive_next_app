package com.drivenext.app.ui.welcome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.drivenext.app.databinding.ActivityWelcomeBinding
import com.drivenext.app.ui.common.openScreen
import com.drivenext.app.ui.login.LoginActivity
import com.drivenext.app.ui.registration.RegistrationActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignIn.setOnClickListener { openScreen(LoginActivity::class.java) }
        binding.btnSignUp.setOnClickListener { openScreen(RegistrationActivity::class.java) }
    }
}
