package ru.shelq.nework.model

import ru.shelq.nework.dto.Post

data class FeedModel(
    val posts:List<Post> = emptyList(),
    val empty:Boolean=false,
) {

}