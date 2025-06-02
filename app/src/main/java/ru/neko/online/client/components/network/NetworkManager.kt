package ru.neko.online.client.components.network

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.json.JSONObject
import ru.neko.online.client.components.AccountPrefs
import java.net.SocketException
import java.net.SocketTimeoutException

class NetworkManager(context: Context) {

    private val accountPrefs: AccountPrefs = AccountPrefs(context)

    private val port: Int = accountPrefs.serverPort
    private val server: String? = accountPrefs.serverAddress

    private val ktorClient: HttpClient = createClient()

    private fun createClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    prettyPrint = true
                })
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }
    }

    fun closeClient() {
        ktorClient.close()
    }

    suspend fun networkPost(path: String, body: Any): Pair<JSONObject?, Int> {
        try {
            val response = ktorClient.post("http://$server:$port/$path") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            val status = response.status.value

            if (status != HttpStatusCode.OK.value) {
                return Pair(null, status)
            }
            val text = response.bodyAsText()
            val jsonObject = JSONObject(text)

            return Pair(jsonObject, status)

        } catch (e: SocketException) {
            Log.e("Network", e.stackTraceToString())
            return Pair(null, 503)
        } catch (e: SocketTimeoutException) {
            Log.e("Network", e.stackTraceToString())
            return Pair(null, 503)
        }
    }
}