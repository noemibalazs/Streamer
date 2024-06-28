package com.noemi.streamer.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.noemi.streamer.model.PayloadData

@Database(entities = [PayloadData::class], version = 1, exportSchema = false)
@TypeConverters(PayloadAccountConverter::class)
abstract class PayloadDataBase : RoomDatabase() {

    abstract fun getPayloadDao(): PayloadDAO
}