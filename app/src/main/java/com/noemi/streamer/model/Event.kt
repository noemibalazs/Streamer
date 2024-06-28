package com.noemi.streamer.model

data class Event(val type: EventType? = null, val payload: PayloadData? = null, val id: Long? = null)

enum class EventType {
    UPDATE,
    STATUS_UPDATE,
    DELETE;
}

fun String.toEventType(): EventType = when (this) {
    "update" -> EventType.UPDATE
    "status.update" -> EventType.STATUS_UPDATE
    else -> EventType.DELETE
}
