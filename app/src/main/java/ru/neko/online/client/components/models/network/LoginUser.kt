package ru.neko.online.client.components.models.network

import kotlinx.serialization.Serializable

@Serializable
data class LoginUser(
    val username: String,
    val password: String
)