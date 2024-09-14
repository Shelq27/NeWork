package ru.shelq.nework.dto

import ru.shelq.nework.enumer.AttachmentType

data class Attachment(
    val url: String,
    val type: AttachmentType,
)