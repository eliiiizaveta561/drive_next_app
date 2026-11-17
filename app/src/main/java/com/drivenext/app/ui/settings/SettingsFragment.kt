package com.drivenext.app.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.drivenext.app.R
import com.drivenext.app.databinding.FragmentSettingsBinding
import com.drivenext.app.domain.model.UserProfile
import com.drivenext.app.ui.common.RequestState
import com.drivenext.app.ui.common.loadRemote
import com.drivenext.app.ui.common.showErrorDialog
import com.drivenext.app.ui.main.openInCurrentTab
import com.drivenext.app.ui.profile.ProfileFragment
import com.drivenext.app.ui.stub.StubActivity

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentSettingsBinding.bind(view)

        binding.profileRow.setOnClickListener { openInCurrentTab(ProfileFragment()) }
        binding.itemBookings.setOnClickListener { StubActivity.open(requireContext(), R.string.my_bookings) }
        binding.itemAddCar.setOnClickListener { StubActivity.open(requireContext(), R.string.add_car_title) }
        binding.itemNotifications.setOnClickListener { openNotificationSettings() }
        binding.itemInvite.setOnClickListener { inviteFriend() }
        binding.itemTheme.setOnClickListener { showInDevelopment() }
        binding.itemHelp.setOnClickListener { showInDevelopment() }

        viewModel.profile.observe(viewLifecycleOwner) { profile -> showProfile(binding, profile) }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state == RequestState.Loading
            if (state is RequestState.Error) {
                showErrorDialog(state.message)
                viewModel.onErrorShown()
            }
        }
        viewModel.loadProfile()
    }

    private fun showProfile(binding: FragmentSettingsBinding, profile: UserProfile) {
        binding.tvName.text = profile.fullName
        binding.tvEmail.text = profile.email
        binding.ivAvatar.loadRemote(profile.avatarUrl, R.drawable.avatar_placeholder)
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
        startActivity(intent)
    }

    private fun inviteFriend() {
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, getString(R.string.invite_text))
        startActivity(Intent.createChooser(intent, getString(R.string.invite_friend)))
    }

    private fun showInDevelopment() {
        Toast.makeText(requireContext(), R.string.in_development, Toast.LENGTH_SHORT).show()
    }
}
