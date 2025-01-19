package ru.shelq.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WallRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val id: Long,
    val userId: Long
) {
    enum class KeyType {
        AFTER, BEFORE
    }
}