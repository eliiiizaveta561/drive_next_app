package com.drivenext.app.ui.cars

import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.drivenext.app.R
import com.drivenext.app.databinding.LayoutCarListBinding
import com.drivenext.app.domain.model.Car
import com.drivenext.app.ui.common.showErrorDialog
import com.drivenext.app.ui.noconnection.NoConnectionActivity
import com.drivenext.app.ui.stub.StubActivity

/**
 * Базовый фрагмент для экранов со списком автомобилей: настраивает RecyclerView
 * и рисует состояния (загрузка / список / пусто / ошибка / нет интернета).
 */
abstract class CarListFragment(@LayoutRes layout: Int) : Fragment(layout) {

    protected abstract val viewModel: CarListViewModel

    @get:StringRes
    protected abstract val emptyTextRes: Int

    // Экран «Нет подключения» возвращает RESULT_OK, когда интернет появился — тогда грузим заново
    private val noConnectionScreen = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) viewModel.load()
    }

    protected fun setupCarList(list: LayoutCarListBinding) {
        val adapter = CarAdapter(onBookClick = ::openRent, onDetailsClick = ::openDetails)
        list.rvCars.layoutManager = LinearLayoutManager(requireContext())
        list.rvCars.adapter = adapter
        list.tvEmpty.setText(emptyTextRes)
        list.btnRetry.setOnClickListener { viewModel.load() }

        viewModel.state.observe(viewLifecycleOwner) { state -> render(list, adapter, state) }
        viewModel.errorDialog.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                requireContext().showErrorDialog(message)
                viewModel.onErrorDialogShown()
            }
        }
    }

    private fun render(list: LayoutCarListBinding, adapter: CarAdapter, state: CarsState) {
        list.progress.isVisible = state is CarsState.Loading
        list.rvCars.isVisible = state is CarsState.Content && state.cars.isNotEmpty()
        list.tvEmpty.isVisible = state is CarsState.Content && state.cars.isEmpty()
        list.errorBlock.isVisible = state is CarsState.Error

        when (state) {
            is CarsState.Content -> adapter.submitList(state.cars)
            is CarsState.Error -> list.tvError.setText(state.messageRes)
            CarsState.NoConnection -> {
                viewModel.onNoConnectionShown()
                noConnectionScreen.launch(NoConnectionActivity.returnIntent(requireContext()))
            }
            CarsState.Loading -> Unit
        }
    }

    private fun openRent(car: Car) =
        StubActivity.open(requireContext(), R.string.rent_title, car.fullName())

    private fun openDetails(car: Car) =
        StubActivity.open(requireContext(), R.string.details_title, car.fullName())

    private fun Car.fullName() = "$brand $model"
}
