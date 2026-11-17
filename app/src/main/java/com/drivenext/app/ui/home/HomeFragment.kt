package com.drivenext.app.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.drivenext.app.R
import com.drivenext.app.databinding.FragmentHomeBinding
import com.drivenext.app.ui.cars.CarListFragment

class HomeFragment : CarListFragment(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModels()
    override val emptyTextRes = R.string.cars_empty

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentHomeBinding.bind(view)
        setupCarList(binding.carList)
    }
}
