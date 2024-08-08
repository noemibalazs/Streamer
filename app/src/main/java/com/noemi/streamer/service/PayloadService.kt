package com.noemi.streamer.service

import com.noemi.streamer.model.Event
import kotlinx.coroutines.flow.Flow

interface PayloadService {

    fun observePayloads(query: String, reconnectDelayMillis: Long = 3000L): Flow<Event>
}