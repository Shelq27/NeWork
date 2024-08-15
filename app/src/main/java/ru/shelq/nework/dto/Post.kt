package ru.shelq.nework.dto

data class Post(
    override val id: Long,
    val authorId :Long,
    val author: String,
    val authorJob: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val coords:String, // TODO
    val link: String,
    val mentionIds : Long, //TODO
    val mentionedMe : Boolean, //TODO
    val likeOwnerIds: Long,
    val likedByMe: Boolean,
    val attachment : String, //TODO
    val users:String ,//TODO
) : AppItem()