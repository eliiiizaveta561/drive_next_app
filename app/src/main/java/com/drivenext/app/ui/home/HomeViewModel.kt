package com.drivenext.app.ui.home

import android.app.Application
import com.drivenext.app.R
import com.drivenext.app.domain.model.Car
import com.drivenext.app.ui.cars.CarListViewModel

class HomeViewModel(app: Application) : CarListViewModel(app, R.string.error_load_data) {

    init {
        load() // список загружается с сервера сразу при открытии экрана
    }

    override suspend fun requestCars(): Result<List<Car>> = carRepository.getCars()
}
