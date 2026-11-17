package com.drivenext.app.data.mapper

import com.drivenext.app.domain.model.Car
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

/** Строка таблицы cars (JSON от PostgREST) → доменная модель Car. */
fun JsonObject.toCar(): Car = Car(
    id = getValue("id").jsonPrimitive.long,
    brand = string("brand"),
    model = string("model"),
    pricePerDay = getValue("price_per_day").jsonPrimitive.int,
    transmission = string("transmission"),
    fuelType = string("fuel_type"),
    imageUrl = string("image_url").ifBlank { null }
)

internal fun JsonObject.string(key: String): String = get(key)?.jsonPrimitive?.contentOrNull.orEmpty()
