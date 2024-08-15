package ru.shelq.nework.dto

data class Event(
    override val id: Long,
    val authorId :Long,
    val author: String,
    val authorJob: String,
    val authorAvatar: String,
    val content: String,
    val datetime : String,
    val published: String,
    val coords:String, // TODO
    val type: String,
    val likeOwnerIds: Long,
    val likedByMe: Boolean,
    val participantsIds : Long, //TODO
    val participatedByMe : Boolean, //TODO
    val attachment : String, //TODO
    val link:String,
    val users:String,//TODO
) : AppItem() {
}