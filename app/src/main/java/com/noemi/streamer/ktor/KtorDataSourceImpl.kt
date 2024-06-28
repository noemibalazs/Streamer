package com.noemi.streamer.ktor

import com.google.gson.Gson
import com.noemi.streamer.model.Event
import com.noemi.streamer.model.EventType
import com.noemi.streamer.model.PayloadData
import com.noemi.streamer.model.toEventType
import com.noemi.streamer.util.BASE_URL
import com.noemi.streamer.util.TOKEN
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import timber.log.Timber
import javax.inject.Inject

class KtorDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val gson: Gson
) : KtorDataSource {

    override fun observePayloads(query: String, reconnectDelayMillis: Long): Flow<Event> = flow {
        coroutineScope {

            while (isActive) {
                prepareRequest(query).execute { response ->
                    if (!response.status.isSuccess()) {
                        Timber.e("Unauthorized error")
                    }
                    if (!response.isEventStream()) {
                        Timber.e("No event stream error")
                    }
                    response.bodyAsChannel()
                        .readSSEvent(
                            onSseEvent = { event ->
                                emit(event)
                            }
                        )
                }

                delay(reconnectDelayMillis)
            }
        }
    }

    private suspend fun prepareRequest(query: String): HttpStatement =
        httpClient.prepareGet(BASE_URL) {
            headers {
                append(HttpHeaders.Accept, "text/event-stream")
                append(HttpHeaders.Accept, "application/json")
                append(HttpHeaders.Authorization, TOKEN)
                append(HttpHeaders.CacheControl, "no-cache")
                append(HttpHeaders.Connection, "keep-alive")
            }
            this.url.parameters.append("q", query)
        }

    private fun HttpResponse.isEventStream(): Boolean {
        val contentType = contentType() ?: return false
        return contentType.contentType == "text" && contentType.contentSubtype == "event-stream"
    }

    private suspend inline fun ByteReadChannel.readSSEvent(onSseEvent: (Event) -> (Unit)) {
        var event = Event()

        while (!isClosedForRead) {
            val line = readUTF8Line()

            when {
                line?.startsWith("event:") == true -> {
                    val name = line.substring(6).trim()
                    event = event.copy(type = name.toEventType())
                }

                line?.startsWith("data:") == true -> {
                    val data = line.substring(5).trim()

                    if (event.type == EventType.UPDATE || event.type == EventType.STATUS_UPDATE) {
                        event = event.copy(payload = gson.fromJson(data, PayloadData::class.java))
                    }

                    if (event.type == EventType.DELETE) {
                        event = event.copy(id = data.toLong())
                    }
                }

                else -> {
                    onSseEvent(event)
                    event = Event()
                }
            }
        }
    }
}