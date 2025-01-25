package ru.shelq.nework.dto

data class Job(
    override val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?,
    val userId: Long,
    val ownedByMe: Boolean = false,
) : AppItem()
