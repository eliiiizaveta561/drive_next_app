package com.drivenext.app.data.repository

import android.content.Context
import com.drivenext.app.data.mapper.toCar
import com.drivenext.app.data.remote.SupabaseClientProvider
import com.drivenext.app.data.remote.runRequest
import com.drivenext.app.domain.model.Car
import com.drivenext.app.domain.repository.CarRepository
import com.drivenext.app.domain.search.CarSearch
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject

class SupabaseCarRepository(private val context: Context) : CarRepository {

    private val cars get() = SupabaseClientProvider.client.postgrest.from(TABLE)

    override suspend fun getCars(): Result<List<Car>> = runRequest(context) {
        cars.select {
            filter { eq("is_available", true) }
            order("price_per_day", Order.DESCENDING)
        }.decodeList<JsonObject>().map { it.toCar() }
    }

    // SELECT * FROM cars WHERE is_available AND (brand ILIKE '%q%' OR model ILIKE '%q%')
    override suspend fun searchCars(query: String): Result<List<Car>> = runRequest(context) {
        val pattern = "%${CarSearch.normalize(query)}%"
        cars.select {
            filter {
                eq("is_available", true)
                or {
                    ilike("brand", pattern)
                    ilike("model", pattern)
                }
            }
            order("price_per_day", Order.DESCENDING)
        }.decodeList<JsonObject>().map { it.toCar() }
    }

    private companion object {
        const val TABLE = "cars"
    }
}
