package ru.shelq.nework.dto

import ru.shelq.nework.enumer.AttachmentType

data class Attachment(
    val url: String,
    val type: AttachmentType,
    var isPlaying: Boolean = false,
    var isLoading: Boolean = false,
    var progress: Int = 0
)