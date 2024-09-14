package ru.shelq.nework.dto


data class User(
    override val id: Long,
    val login: String,
    val name: String,
    val avatar: String?,
) : AppItem() {

}
