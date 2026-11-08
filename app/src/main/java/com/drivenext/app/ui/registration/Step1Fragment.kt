package com.drivenext.app.ui.registration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.drivenext.app.R
import com.drivenext.app.databinding.FragmentStep1Binding
import com.drivenext.app.ui.common.showErrorDialog

class Step1Fragment : Fragment(R.layout.fragment_step1) {

    private val viewModel: RegistrationViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentStep1Binding.bind(view)

        binding.btnNext.setOnClickListener {
            val error = viewModel.saveStep1(
                email = binding.etEmail.text.toString(),
                password = binding.etPassword.text.toString(),
                repeatPassword = binding.etRepeatPassword.text.toString(),
                isAgreed = binding.cbAgreement.isChecked
            )
            if (error != null) showErrorDialog(error) else openRegistrationStep(Step2Fragment())
        }
    }
}
