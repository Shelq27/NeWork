package ru.shelq.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.shelq.nework.dto.Jobs

@Entity
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?,
    val userId: Long
) {
    fun toDto() = Jobs(id, name, position, start, finish, link, userId)

    companion object {
        fun fromDto(dto: Jobs, userId: Long) =
            JobEntity(
                dto.id,
                dto.name,
                dto.position,
                dto.start,
                dto.finish,
                dto.link,
                userId
            )
    }
}

fun List<JobEntity>.toDto(): List<Jobs> = map(JobEntity::toDto)
fun List<Jobs>.toEntity(userId: Long): List<JobEntity> {
    return this.map { job ->
        JobEntity(
            job.id,
            job.name,
            job.position,
            job.start,
            job.finish,
            job.link,
            userId
        )
    }
}

