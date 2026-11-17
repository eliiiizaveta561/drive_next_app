package com.drivenext.app.ui.cars

import androidx.annotation.StringRes
import com.drivenext.app.domain.model.Car

/** Состояние экрана со списком автомобилей. */
sealed class CarsState {
    data object Loading : CarsState()
    data class Content(val cars: List<Car>) : CarsState()
    data class Error(@StringRes val messageRes: Int) : CarsState()
    data object NoConnection : CarsState()
}
