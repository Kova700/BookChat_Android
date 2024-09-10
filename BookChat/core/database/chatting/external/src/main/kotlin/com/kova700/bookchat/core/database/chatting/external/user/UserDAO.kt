package com.kova700.bookchat.core.database.chatting.external.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kova700.bookchat.core.data.user.external.model.UserDefaultProfileType
import com.kova700.bookchat.core.database.chatting.external.user.model.USER_ENTITY_TABLE_NAME
import com.kova700.bookchat.core.database.chatting.external.user.model.UserEntity

@Dao
interface UserDAO {

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertIgnore(user: UserEntity): Long

	suspend fun upsertAllUsers(users: List<UserEntity>) {
		for (user in users) upsertUser(user)
	}

	suspend fun upsertUser(user: UserEntity) {
		val id = insertIgnore(user)
		if (id != -1L) return

		updateForInsert(
			id = user.id,
			nickname = user.nickname,
			profileImageUrl = user.profileImageUrl,
			defaultProfileImageType = user.defaultProfileImageType
		)
	}

	@Query(
		"UPDATE User SET " +
						"nickname = :nickname, " +
						"profile_image_url = :profileImageUrl, " +
						"default_profile_image_type = :defaultProfileImageType " +
						"WHERE id = :id"
	)
	suspend fun updateForInsert(
		id: Long,
		nickname: String,
		profileImageUrl: String?,
		defaultProfileImageType: UserDefaultProfileType,
	)

	@Query(
		"SELECT * FROM User " +
						"WHERE id = :userId"
	)
	suspend fun getUser(userId: Long): UserEntity?

	@Query("DELETE FROM $USER_ENTITY_TABLE_NAME")
	suspend fun deleteAll()
}