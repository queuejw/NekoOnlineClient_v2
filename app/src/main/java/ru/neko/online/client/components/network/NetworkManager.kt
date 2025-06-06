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
import io.ktor.http.URLParserException
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ClosedByteChannelException
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import ru.neko.online.client.components.AccountPrefs
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkManager(context: Context) {

    private var accountPrefs: AccountPrefs? = AccountPrefs(context)

    private val port: Int? = accountPrefs?.serverPort
    private val server: String? = accountPrefs?.serverAddress

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
        accountPrefs = null
    }

    suspend fun networkPost(path: String, body: Any): Pair<Any?, Int> {
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

            return when (path) {
                "cats" -> Pair(JSONArray(text), status)
                "controls/food" -> Pair(null, status)
                "controls/toy" -> Pair(null, status)
                "controls/water" -> Pair(null, status)
                else -> Pair(JSONObject(text), status)
            }

        } catch (e: SocketException) {
            Log.e("Network", e.stackTraceToString())
            return Pair(null, 503)
        } catch (e: SocketTimeoutException) {
            Log.e("Network", e.stackTraceToString())
            return Pair(null, 503)
        } catch (e: ClosedByteChannelException) {
            Log.e("Network", e.stackTraceToString())
            return Pair(null, 503)
        } catch (e: URLParserException) {
            Log.e("Network", e.stackTraceToString())
            return Pair(null, -1)
        } catch (e: UnknownHostException) {
            Log.e("Network", e.stackTraceToString())
            return Pair(null, -1)
        }
    }
}