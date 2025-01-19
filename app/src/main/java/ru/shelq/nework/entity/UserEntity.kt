package ru.shelq.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.shelq.nework.dto.User

@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String?
) {
    fun toDto() = User(id, login, name, avatar)

    companion object {
        fun fromDto(dto: User) =
            UserEntity(
                dto.id,
                dto.login,
                dto.name,
                dto.avatar
            )
    }
}

fun List<UserEntity>.toDto(): List<User> = map(UserEntity::toDto)
fun List<User>.toEntity(): List<UserEntity> = map(UserEntity.Companion::fromDto)