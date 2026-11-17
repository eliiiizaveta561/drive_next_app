package com.drivenext.app.domain.model

data class Car(
    val id: Long,
    val brand: String,
    val model: String,
    val pricePerDay: Int,
    val transmission: String,
    val fuelType: String,
    val imageUrl: String?
)
