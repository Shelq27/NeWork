package ru.shelq.nework.dto

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val coords: Coordinates? = null,
    val link: String,
    val mentionIds: List<Long>,
    val mentionedMe: Boolean,
    val likeOwnerIds: List<Long>,
    val likedByMe: Boolean,
    val attachment: Attachment? = null,
    val users: Map<Long,UserPreview>,
) : AppItem()