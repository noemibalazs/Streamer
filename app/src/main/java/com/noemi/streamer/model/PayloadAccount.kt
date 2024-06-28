package com.noemi.streamer.model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class PayloadAccount(
    @SerialName("id")
    val id: Long,

    @SerialName("avatar")
    val avatar: String,

    @SerialName("username")
    val username: String
)