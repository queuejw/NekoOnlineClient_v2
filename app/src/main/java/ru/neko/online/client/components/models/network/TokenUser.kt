package ru.neko.online.client.components.models.network

import kotlinx.serialization.Serializable

@Serializable
data class TokenUser(
    val token: String
)