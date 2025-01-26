package ru.shelq.nework.dto

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String?,
    val authorAvatar: String?,
    val content: String,
    val published: String,
    val coords: Coordinates? = null,
    val link: String?,
    val mentionIds: List<Long>,
    val mentionedMe: Boolean,
    val likeOwnerIds: List<Long>,
    val likedByMe: Boolean,
    val attachment: Attachment? = null,
    val users: Map<String, UserPreview>,
    val ownedByMe: Boolean = false,
) : AppItem() {

    fun toPostApi() = PostApi(
        id,
        authorId,
        author,
        authorJob,
        authorAvatar,
        coords,
        content,
        published,
        link,
        mentionIds,
        mentionedMe,
        likeOwnerIds,
        likedByMe,
        attachment,
        users
    )
}

data class PostApi(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String? = "",
    val authorAvatar: String? = "",
    val coords: Coordinates? = null,
    val content: String,
    val published: String,
    val link: String? = null,
    val mentionIds: List<Long>,
    val mentionedMe: Boolean,
    val likeOwnerIds: List<Long>,
    val likedByMe: Boolean,
    val attachment: Attachment? = null,
    val users: Map<String, UserPreview>
) : AppItem()