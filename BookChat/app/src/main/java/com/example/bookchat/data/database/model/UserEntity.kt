package com.example.bookchat.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bookchat.domain.model.UserDefaultProfileType

@Entity(tableName = "User")
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "nickname") val nickname: String,
    @ColumnInfo(name = "profile_image_url") val profileImageUrl: String?,
    @ColumnInfo(name = "default_profile_image_type")
    val defaultProfileImageType: UserDefaultProfileType
)