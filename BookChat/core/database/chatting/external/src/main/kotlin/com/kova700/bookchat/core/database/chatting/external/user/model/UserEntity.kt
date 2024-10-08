package com.kova700.bookchat.core.database.chatting.external.user.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kova700.bookchat.core.data.user.external.model.UserDefaultProfileType

@Entity(tableName = USER_ENTITY_TABLE_NAME)
data class UserEntity(
	@PrimaryKey
	@ColumnInfo(name = "id") val id: Long,
	@ColumnInfo(name = "nickname") val nickname: String,
	@ColumnInfo(name = "profile_image_url") val profileImageUrl: String?,
	@ColumnInfo(name = "default_profile_image_type")
	val defaultProfileImageType: UserDefaultProfileType,
)

const val USER_ENTITY_TABLE_NAME = "User"