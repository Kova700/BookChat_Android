package com.kova700.bookchat.core.data.user.internal

import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.data.user.external.repository.UserRepository
import com.kova700.bookchat.core.database.chatting.external.user.UserDAO
import com.kova700.bookchat.core.database.chatting.external.user.mapper.toUser
import com.kova700.bookchat.core.database.chatting.external.user.mapper.toUserEntity
import com.kova700.bookchat.core.network.bookchat.user.UserApi
import com.kova700.bookchat.core.network.bookchat.user.model.mapper.toUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
	private val userApi: UserApi,
	private val userDao: UserDAO,
) : UserRepository {

	private val users = MutableStateFlow<Map<Long, User>>(emptyMap())

	private fun setUsers(users: List<User>) {
		this.users.update { this.users.value + users.associateBy { it.id } }
	}

	/** 로컬에 있는 유저 우선적으로 쿼리 */
	override suspend fun getUser(userId: Long): User {
		return getOfflineUser(userId) ?: getOnlineUser(userId)
	}

	override fun getUsersFlow(): Flow<Map<Long, User>> {
		return users.asStateFlow()
	}

	private suspend fun getOfflineUser(userId: Long): User? {
		return users.value[userId] ?: userDao.getUser(userId)?.toUser()
	}

	private suspend fun getOnlineUser(userId: Long): User {
		return runCatching { userApi.getUser(userId).toUser() }
			.onSuccess { upsertUser(it) }
			/** 탈퇴한 유저 혹은 잘못된 요청 */
			.getOrDefault(
				User.DEFAULT.copy(
					id = userId,
					nickname = UNKNOWN
				)
			)
	}

	override suspend fun upsertAllUsers(users: List<User>) {
		setUsers(users)
		userDao.upsertAllUsers(users.toUserEntity())
	}

	override suspend fun upsertUser(user: User) {
		upsertAllUsers(listOf(user))
	}

	override suspend fun clear() {
		users.update { emptyMap() }
		userDao.deleteAll()
	}

	companion object {
		private const val UNKNOWN = "알 수 없음"
	}
}