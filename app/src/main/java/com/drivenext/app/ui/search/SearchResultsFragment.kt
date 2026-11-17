package com.drivenext.app.ui.search

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.drivenext.app.R
import com.drivenext.app.databinding.FragmentSearchResultsBinding
import com.drivenext.app.ui.cars.CarListFragment

class SearchResultsFragment : CarListFragment(R.layout.fragment_search_results) {

    override val viewModel: SearchResultsViewModel by viewModels()
    override val emptyTextRes = R.string.search_empty

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentSearchResultsBinding.bind(view)
        binding.tvQuery.text = getString(R.string.search_query_label, viewModel.query)
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        setupCarList(binding.carList)
    }

    companion object {
        const val ARG_QUERY = "query"

        fun newInstance(query: String) = SearchResultsFragment().apply {
            arguments = bundleOf(ARG_QUERY to query)
        }
    }
}
