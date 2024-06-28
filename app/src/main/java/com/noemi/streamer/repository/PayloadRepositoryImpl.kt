package com.noemi.streamer.repository

import com.noemi.streamer.model.PayloadData
import com.noemi.streamer.room.PayloadDAO
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PayloadRepositoryImpl @Inject constructor(private val payloadDAO: PayloadDAO) : PayloadRepository {

    override suspend fun clearDataBase() = payloadDAO.deleteAll()

    override suspend fun deletePayloadData(id: Long) = payloadDAO.deletePayload(id)

    override fun observePayloads(): Flow<List<PayloadData>> = payloadDAO.observePayloads()

    override suspend fun savePayload(payloadData: PayloadData) = payloadDAO.insertPayload(payloadData)
}