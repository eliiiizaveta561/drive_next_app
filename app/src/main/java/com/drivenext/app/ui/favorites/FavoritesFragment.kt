package com.drivenext.app.ui.favorites

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.drivenext.app.R
import com.drivenext.app.databinding.FragmentPlaceholderBinding

/** Вкладка «Избранное» — сам экран реализуется в лабораторной работе №4. */
class FavoritesFragment : Fragment(R.layout.fragment_placeholder) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FragmentPlaceholderBinding.bind(view).tvTitle.setText(R.string.nav_favorites)
    }
}
