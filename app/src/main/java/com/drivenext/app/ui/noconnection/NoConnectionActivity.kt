package com.drivenext.app.ui.noconnection

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.drivenext.app.databinding.ActivityNoConnectionBinding
import com.drivenext.app.ui.common.openScreen
import com.drivenext.app.ui.start.StartDestination
import com.drivenext.app.ui.start.StartViewModel
import com.drivenext.app.ui.start.screen

class NoConnectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoConnectionBinding
    private val viewModel: StartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRetry.setOnClickListener { viewModel.chooseDestination() }

        viewModel.destination.observe(this) { destination ->
            if (destination != StartDestination.NO_CONNECTION) {
                openScreen(destination.screen)
                finish()
            }
        }
    }
}
