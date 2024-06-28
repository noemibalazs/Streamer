package com.noemi.streamer.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.noemi.streamer.model.PayloadData
import com.noemi.streamer.util.PAYLOAD_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface PayloadDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayload(country: PayloadData)

    @Query("SELECT * FROM $PAYLOAD_TABLE")
    fun observePayloads(): Flow<List<PayloadData>>

    @Query("DELETE FROM $PAYLOAD_TABLE")
    suspend fun deleteAll()

    @Query("DELETE FROM $PAYLOAD_TABLE WHERE id = :id")
    suspend fun deletePayload(id: Long)
}