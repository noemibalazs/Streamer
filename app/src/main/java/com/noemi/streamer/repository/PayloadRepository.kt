package com.noemi.streamer.repository

import com.noemi.streamer.model.PayloadData
import kotlinx.coroutines.flow.Flow

interface PayloadRepository {

    fun observePayloads(): Flow<List<PayloadData>>

    suspend fun savePayload(payloadData: PayloadData)

    suspend fun deletePayloadData(id: Long)

    suspend fun clearDataBase()
}