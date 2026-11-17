package com.drivenext.app.ui.noconnection

import android.app.Activity
import android.content.Context
import android.content.Intent
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
            if (destination == StartDestination.NO_CONNECTION) return@observe
            if (isReturnMode) {
                // Открыты с экрана приложения: просто возвращаемся туда, интернет уже есть
                setResult(Activity.RESULT_OK)
            } else {
                openScreen(destination.screen)
            }
            finish()
        }
    }

    private val isReturnMode get() = intent.getBooleanExtra(EXTRA_RETURN_RESULT, false)

    companion object {
        private const val EXTRA_RETURN_RESULT = "return_result"

        /** Интент для открытия поверх текущего экрана с возвратом результата RESULT_OK. */
        fun returnIntent(context: Context): Intent =
            Intent(context, NoConnectionActivity::class.java).putExtra(EXTRA_RETURN_RESULT, true)
    }
}
