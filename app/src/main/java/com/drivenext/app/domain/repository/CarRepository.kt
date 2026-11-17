package com.drivenext.app.domain.repository

import com.drivenext.app.domain.model.Car

interface CarRepository {

    suspend fun getCars(): Result<List<Car>>

    suspend fun searchCars(query: String): Result<List<Car>>
}
