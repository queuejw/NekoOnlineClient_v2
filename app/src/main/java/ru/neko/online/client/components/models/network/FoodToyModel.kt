package ru.neko.online.client.components.models.network

import kotlinx.serialization.Serializable

@Serializable
data class FoodToyModel(
    val token: String,
    val state: Boolean
)