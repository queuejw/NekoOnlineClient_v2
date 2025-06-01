package ru.neko.online.client.components.network.serializable

import kotlinx.serialization.Serializable

@Serializable
data class LoginUser(
    val username: String,
    val password: String
)