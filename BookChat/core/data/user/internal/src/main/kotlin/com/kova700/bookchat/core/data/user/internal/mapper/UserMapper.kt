import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.data.user.internal.mapper.toDomain
import com.kova700.bookchat.core.database.chatting.external.user.model.UserEntity
import com.kova700.bookchat.core.network.bookchat.model.response.UserResponse

fun User.toUserEntity(): UserEntity {
	return UserEntity(
		id = id,
		nickname = nickname,
		profileImageUrl = profileImageUrl,
		defaultProfileImageType = defaultProfileImageType
	)
}

fun UserEntity.toUser(): User {
	return User(
		id = id,
		nickname = nickname,
		profileImageUrl = profileImageUrl,
		defaultProfileImageType = defaultProfileImageType
	)
}

fun UserResponse.toUser(): User {
	return User(
		id = userId,
		nickname = userNickname,
		profileImageUrl = userProfileImageUri,
		defaultProfileImageType = defaultProfileImageType.toDomain()
	)
}

fun UserResponse.toUserEntity(): UserEntity {
	return UserEntity(
		id = userId,
		nickname = userNickname,
		profileImageUrl = userProfileImageUri,
		defaultProfileImageType = defaultProfileImageType.toDomain()
	)
}

fun List<UserEntity>.toUser() = this.map { it.toUser() }
fun List<User>.toUserEntity() = this.map { it.toUserEntity() }
