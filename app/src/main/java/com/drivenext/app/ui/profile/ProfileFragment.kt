package com.drivenext.app.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.drivenext.app.R
import com.drivenext.app.databinding.FragmentProfileBinding
import com.drivenext.app.domain.model.Gender
import com.drivenext.app.domain.model.UserProfile
import com.drivenext.app.ui.common.RequestState
import com.drivenext.app.ui.common.loadRemote
import com.drivenext.app.ui.common.openScreenAndClearBackStack
import com.drivenext.app.ui.common.showErrorDialog
import com.drivenext.app.ui.login.LoginActivity
import java.time.LocalDate

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel: ProfileViewModel by viewModels()

    // Системный выбор фото: возвращает Uri картинки или null, если пользователь передумал
    private val pickAvatar = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) viewModel.uploadAvatar(uri)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentProfileBinding.bind(view)

        binding.avatar.setOnClickListener {
            pickAvatar.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.btnSignOut.setOnClickListener { viewModel.signOut() }

        viewModel.profile.observe(viewLifecycleOwner) { profile -> showProfile(binding, profile) }
        viewModel.isAvatarUploading.observe(viewLifecycleOwner) { isUploading ->
            binding.avatarProgress.isVisible = isUploading
            binding.avatar.isEnabled = !isUploading
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.loading.isVisible = state == RequestState.Loading
            if (state is RequestState.Error) {
                showErrorDialog(state.message)
                viewModel.onErrorShown()
            }
        }
        viewModel.signedOut.observe(viewLifecycleOwner) { signedOut ->
            // Экран входа открывается с очисткой стека: «Назад» не вернёт в профиль
            if (signedOut) requireActivity().openScreenAndClearBackStack(LoginActivity::class.java)
        }
    }

    private fun showProfile(binding: FragmentProfileBinding, profile: UserProfile) {
        binding.tvName.text = profile.fullName
        binding.tvEmail.text = profile.email
        binding.tvGender.text = when (profile.gender) {
            Gender.MALE -> getString(R.string.male)
            Gender.FEMALE -> getString(R.string.female)
            null -> getString(R.string.not_specified)
        }
        binding.tvJoined.isVisible = profile.registeredAt != null
        profile.registeredAt?.let { binding.tvJoined.text = joinedText(it) }
        binding.ivAvatar.loadRemote(profile.avatarUrl, R.drawable.avatar_placeholder)
    }

    /** «Присоединился в июле 2024» — месяц в предложном падеже из string-array. */
    private fun joinedText(date: LocalDate): String {
        val month = resources.getStringArray(R.array.months_prepositional)[date.monthValue - 1]
        return getString(R.string.joined_at, month, date.year)
    }
}
