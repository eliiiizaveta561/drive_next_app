package com.drivenext.app.ui.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import com.drivenext.app.R
import com.drivenext.app.databinding.FragmentHomeBinding
import com.drivenext.app.ui.cars.CarListFragment
import com.drivenext.app.ui.common.showErrorDialog
import com.drivenext.app.ui.main.openInCurrentTab
import com.drivenext.app.ui.search.SearchResultsFragment

class HomeFragment : CarListFragment(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModels()
    override val emptyTextRes = R.string.cars_empty

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentHomeBinding.bind(view)
        setupCarList(binding.carList)
        setupSearch(binding.searchView)
    }

    private fun setupSearch(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Нажата «Поиск» на клавиатуре — ищем по окончательному запросу на отдельном экране
            override fun onQueryTextSubmit(query: String?): Boolean {
                val searchQuery = viewModel.prepareSearchQuery(query)
                if (searchQuery == null) {
                    showErrorDialog(R.string.error_search_empty)
                } else {
                    searchView.clearFocus()
                    openInCurrentTab(SearchResultsFragment.newInstance(searchQuery))
                }
                return true
            }

            // Текст меняется по мере ввода — сам запрос отправляем только по подтверждению
            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }
}
