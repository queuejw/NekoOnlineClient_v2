package ru.neko.online.client.components.network.serializable

import kotlinx.serialization.Serializable

@Serializable
data class TokenUser(
    val token: String
)