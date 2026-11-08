package com.drivenext.app.ui.registration

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.drivenext.app.R
import com.drivenext.app.databinding.FragmentStep2Binding
import com.drivenext.app.domain.model.Gender
import com.drivenext.app.ui.common.attachDatePicker
import com.drivenext.app.ui.common.showErrorDialog

class Step2Fragment : Fragment(R.layout.fragment_step2) {

    private val viewModel: RegistrationViewModel by activityViewModels()

    private var _binding: FragmentStep2Binding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentStep2Binding.bind(view)

        binding.etLastName.doAfterTextChanged { updateNextButton() }
        binding.etFirstName.doAfterTextChanged { updateNextButton() }
        binding.etBirthDate.doAfterTextChanged { updateNextButton() }
        binding.rgGender.setOnCheckedChangeListener { _, _ -> updateNextButton() }
        updateNextButton()

        attachDatePicker(binding.tilBirthDate, binding.etBirthDate)

        binding.btnNext.setOnClickListener {
            val error = viewModel.saveStep2(
                lastName = binding.etLastName.text.toString(),
                firstName = binding.etFirstName.text.toString(),
                middleName = binding.etMiddleName.text.toString(),
                birthDate = binding.etBirthDate.text.toString(),
                gender = selectedGender()
            )
            if (error != null) showErrorDialog(error) else openRegistrationStep(Step3Fragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun selectedGender(): Gender? = when (binding.rgGender.checkedRadioButtonId) {
        R.id.rbMale -> Gender.MALE
        R.id.rbFemale -> Gender.FEMALE
        else -> null
    }

    private fun updateNextButton() {
        binding.btnNext.isEnabled = viewModel.isStep2Filled(
            lastName = binding.etLastName.text.toString(),
            firstName = binding.etFirstName.text.toString(),
            birthDate = binding.etBirthDate.text.toString(),
            gender = selectedGender()
        )
    }
}
