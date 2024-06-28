package com.noemi.streamer.ktor

import com.noemi.streamer.model.Event
import kotlinx.coroutines.flow.Flow

interface KtorDataSource {

    fun observePayloads(query: String, reconnectDelayMillis: Long = 3000L): Flow<Event>
}