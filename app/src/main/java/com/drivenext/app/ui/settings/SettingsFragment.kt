package com.drivenext.app.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.drivenext.app.R
import com.drivenext.app.databinding.FragmentPlaceholderBinding

class SettingsFragment : Fragment(R.layout.fragment_placeholder) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FragmentPlaceholderBinding.bind(view).tvTitle.setText(R.string.nav_settings)
    }
}
