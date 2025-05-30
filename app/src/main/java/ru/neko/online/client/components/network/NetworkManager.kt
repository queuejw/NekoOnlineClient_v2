package ru.neko.online.client.components.network

import android.content.Context
import io.ktor.client.HttpClient
import ru.neko.online.client.components.AccountPrefs

class NetworkManager(context: Context) {

    private val accountPrefs: AccountPrefs = AccountPrefs(context)

    private val port: Int = accountPrefs.serverPort
    private val server: String? = accountPrefs.serverAddress

    private val ktorClient: HttpClient = HttpClient()

}