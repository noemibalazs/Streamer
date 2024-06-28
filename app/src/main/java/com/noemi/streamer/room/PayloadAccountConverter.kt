package com.noemi.streamer.room

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noemi.streamer.model.PayloadAccount
import javax.inject.Inject

@ProvidedTypeConverter
class PayloadAccountConverter @Inject constructor(private val gson: Gson) {

    @TypeConverter
    fun toString(account: PayloadAccount): String {
        return gson.toJson(account)
    }

    @TypeConverter
    fun fromString(json: String): PayloadAccount {
        val type = object : TypeToken<PayloadAccount>() {}.type
        return gson.fromJson(json, type)
    }
}