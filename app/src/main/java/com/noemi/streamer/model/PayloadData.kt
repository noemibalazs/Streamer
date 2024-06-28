package com.noemi.streamer.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.noemi.streamer.util.PAYLOAD_TABLE
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@Entity(tableName = PAYLOAD_TABLE)
data class PayloadData(

    @PrimaryKey
    @SerialName("id")
    val id: Long,

    @SerialName("url")
    val url: String,

    @SerialName("content")
    val content: String,

    @SerialName("account")
    val account: PayloadAccount
)