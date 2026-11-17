package com.drivenext.app.ui.search

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.drivenext.app.R
import com.drivenext.app.domain.model.Car
import com.drivenext.app.ui.cars.CarListViewModel

/**
 * Запрос берётся из аргументов фрагмента через SavedStateHandle — он переживает поворот
 * и перезапуск процесса, а результаты каждый раз заново загружаются с сервера.
 */
class SearchResultsViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle
) : CarListViewModel(app, R.string.error_search) {

    val query: String = savedStateHandle[SearchResultsFragment.ARG_QUERY] ?: ""

    init {
        load()
    }

    override suspend fun requestCars(): Result<List<Car>> = carRepository.searchCars(query)
}
