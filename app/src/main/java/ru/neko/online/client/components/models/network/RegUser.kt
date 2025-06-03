package ru.neko.online.client.components.models.network

import kotlinx.serialization.Serializable

@Serializable
data class RegUser(
    val name: String,
    val username: String,
    val password: String
)