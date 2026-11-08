package com.drivenext.app.ui.registration

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.drivenext.app.databinding.ActivityRegistrationSuccessBinding
import com.drivenext.app.ui.common.openScreenAndClearBackStack
import com.drivenext.app.ui.main.MainActivity

class RegistrationSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRegistrationSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNext.setOnClickListener { openScreenAndClearBackStack(MainActivity::class.java) }
    }
}
