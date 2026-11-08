package com.drivenext.app.ui.login

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.drivenext.app.databinding.ActivityLoginBinding
import com.drivenext.app.ui.common.RequestState
import com.drivenext.app.ui.common.openScreen
import com.drivenext.app.ui.common.openScreenAndClearBackStack
import com.drivenext.app.ui.common.showErrorDialog
import com.drivenext.app.ui.main.MainActivity
import com.drivenext.app.ui.registration.RegistrationActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    private val email get() = binding.etEmail.text.toString()
    private val password get() = binding.etPassword.text.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etEmail.doAfterTextChanged { updateSignInButton() }
        binding.etPassword.doAfterTextChanged { updateSignInButton() }

        binding.btnSignIn.setOnClickListener {
            viewModel.signIn(email, password)?.let { showErrorDialog(it) }
        }
        binding.tvSignUp.setOnClickListener { openScreen(RegistrationActivity::class.java) }

        viewModel.state.observe(this) { state -> render(state) }
    }

    private fun render(state: RequestState) {
        binding.progress.isVisible = state == RequestState.Loading
        updateSignInButton()

        when (state) {
            RequestState.Success -> openScreenAndClearBackStack(MainActivity::class.java)
            is RequestState.Error -> {
                showErrorDialog(state.message)
                viewModel.onErrorShown()
            }
            else -> Unit
        }
    }

    private fun updateSignInButton() {
        val isLoading = viewModel.state.value == RequestState.Loading
        binding.btnSignIn.isEnabled = !isLoading && viewModel.isFormFilled(email, password)
    }
}
