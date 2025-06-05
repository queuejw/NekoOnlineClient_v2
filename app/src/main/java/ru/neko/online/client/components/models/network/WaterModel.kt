package ru.neko.online.client.components.models.network

import kotlinx.serialization.Serializable

@Serializable
data class WaterModel(
    val token: String,
    val amount: Int
)