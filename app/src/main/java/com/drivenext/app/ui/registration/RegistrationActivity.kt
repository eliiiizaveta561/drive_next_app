package com.drivenext.app.ui.registration

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.drivenext.app.R
import com.drivenext.app.databinding.ActivityRegistrationBinding
import com.drivenext.app.ui.common.RequestState
import com.drivenext.app.ui.common.openScreenAndClearBackStack
import com.drivenext.app.ui.common.showErrorDialog

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private val viewModel: RegistrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.commit { replace(R.id.container, Step1Fragment()) }
        }

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        viewModel.state.observe(this) { state -> render(state) }
    }

    fun openStep(step: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.container, step)
            addToBackStack(null)
        }
    }

    private fun render(state: RequestState) {
        binding.loading.isVisible = state == RequestState.Loading

        when (state) {
            RequestState.Success -> openScreenAndClearBackStack(RegistrationSuccessActivity::class.java)
            is RequestState.Error -> {
                showErrorDialog(state.message)
                viewModel.onErrorShown()
            }
            else -> Unit
        }
    }
}

fun Fragment.openRegistrationStep(step: Fragment) {
    (requireActivity() as RegistrationActivity).openStep(step)
}
