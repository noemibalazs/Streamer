package com.noemi.streamer.room

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.noemi.streamer.model.PayloadAccount
import kotlinx.serialization.json.Json
import javax.inject.Inject

@ProvidedTypeConverter
class PayloadAccountConverter @Inject constructor(private val json: Json) {

    @TypeConverter
    fun toString(account: PayloadAccount): String {
        return json.encodeToString(PayloadAccount.serializer(), account)
    }

    @TypeConverter
    fun fromString(jsonStr: String): PayloadAccount {
        return json.decodeFromString(jsonStr)
    }
}