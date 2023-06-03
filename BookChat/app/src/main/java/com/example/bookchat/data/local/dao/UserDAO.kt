package com.example.bookchat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookchat.data.local.entity.UserEntity
import com.example.bookchat.utils.UserDefaultProfileImageType

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(user: UserEntity): Long

    suspend fun insertOrUpdateAllUser(users: List<UserEntity>) {
        for (user in users) insertOrUpdateUser(user)
    }

    suspend fun insertOrUpdateUser(user: UserEntity) {
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
        defaultProfileImageType: UserDefaultProfileImageType
    )
}