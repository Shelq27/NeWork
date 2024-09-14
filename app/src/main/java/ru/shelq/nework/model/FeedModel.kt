package ru.shelq.nework.model

import ru.shelq.nework.dto.Event
import ru.shelq.nework.dto.Post

data class FeedModel<T>(
    val data: List<T> = emptyList(),
    val empty: Boolean = false,
) {

}