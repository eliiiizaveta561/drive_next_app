package com.drivenext.app.ui.stub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.drivenext.app.databinding.ActivityStubBinding

/**
 * Временный экран для переходов, которые по заданию реализуются в следующих лабораторных:
 * «Детали», «Оформление аренды», «Мои бронирования», «Добавление автомобиля».
 */
class StubActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityStubBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTitle.setText(intent.getIntExtra(EXTRA_TITLE, 0).takeIf { it != 0 } ?: return finish())
        intent.getStringExtra(EXTRA_SUBTITLE)?.let { binding.tvDescription.text = it }
        binding.btnBack.setOnClickListener { finish() }
    }

    companion object {
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_SUBTITLE = "subtitle"

        fun open(context: Context, @StringRes title: Int, subtitle: String? = null) {
            val intent = Intent(context, StubActivity::class.java)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_SUBTITLE, subtitle)
            context.startActivity(intent)
        }
    }
}
