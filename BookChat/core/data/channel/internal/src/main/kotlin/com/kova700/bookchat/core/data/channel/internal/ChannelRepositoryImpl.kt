package com.kova700.bookchat.core.data.channel.internal

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.channel.external.model.ChannelDefaultImageType
import com.kova700.bookchat.core.data.channel.external.model.ChannelInfo
import com.kova700.bookchat.core.data.channel.external.model.ChannelIsExplodedException
import com.kova700.bookchat.core.data.channel.external.model.ChannelIsFullException
import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.data.channel.external.model.UserIsBannedException
import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.channel.internal.mapper.toChannelEntity
import com.kova700.bookchat.core.data.channel.internal.mapper.toDomain
import com.kova700.bookchat.core.data.common.model.network.BookChatApiResult
import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.database.chatting.external.channel.ChannelDAO
import com.kova700.bookchat.core.database.chatting.external.channel.mapper.toChannel
import com.kova700.bookchat.core.database.chatting.external.channel.mapper.toChannelEntity
import com.kova700.bookchat.core.network.bookchat.channel.ChannelApi
import com.kova700.bookchat.core.network.bookchat.channel.model.both.ChannelMemberAuthorityNetwork
import com.kova700.bookchat.core.network.bookchat.channel.model.mapper.toChannel
import com.kova700.bookchat.core.network.bookchat.channel.model.mapper.toNetwork
import com.kova700.bookchat.core.network.bookchat.channel.model.request.RequestChangeChannelSetting
import com.kova700.bookchat.core.network.bookchat.channel.model.request.RequestMakeChannel
import com.kova700.bookchat.core.network.bookchat.channel.model.response.BookChatFailResponseBody
import com.kova700.bookchat.core.network.bookchat.common.mapper.toBookRequest
import com.kova700.bookchat.core.network.util.multipart.toMultiPartBody
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds

class ChannelRepositoryImpl @Inject constructor(
	private val channelApi: ChannelApi,
	private val channelDAO: ChannelDAO,
	private val jsonSerializer: Json,
) : ChannelRepository {

	//TODO :
	// TopPinNum 내림 차순 정렬
	//  lastMessageAt(Date Type) 내림 차순 정렬 ?: createdAt(Date Type) 내림 차순 정렬로 추후 수정
	//  (lastChatId가 null인 신생 채널보다, 더 최신에 채팅이 발생한 채팅방이 더 아래에 노출되는 상황이 생김)
	private val mapChannels = MutableStateFlow<Map<Long, Channel>>(emptyMap())//(channelId, Channel)
	private val sortedChannels = mapChannels.map { it.values }
		.map { channels ->
			//ORDER BY top_pin_num DESC, last_chat_id IS NULL DESC, last_chat_id DESC, room_id DESC
			channels.sortedWith(
				compareBy<Channel> { channel -> channel.topPinNum.unaryMinus() }
					.then(nullsFirst(compareBy { channel -> channel.lastChat?.chatId?.unaryMinus() }))
					.thenBy { channel -> channel.roomId.unaryMinus() }
			)
		}

	private var currentPage: Long? = null
	private var isEndPage = false

	override fun getChannelsFlow(): Flow<List<Channel>> {
		return sortedChannels
	}

	override fun getChannelFlow(channelId: Long): Flow<Channel> {
		return mapChannels.map { it[channelId] }.filterNotNull().distinctUntilChanged()
	}

	private fun setChannels(newChannels: Map<Long, Channel>) {
		mapChannels.update { newChannels }
	}

	/** 로컬에 있는 채널 우선적으로 쿼리
	 * (API를 통해 받아온 Channel에는 LastChat을 비롯한 detail정보가 없음) */
	override suspend fun getChannel(channelId: Long): Channel {
		mapChannels.value[channelId]?.let { return it }
		val channel = getOfflineChannel(channelId) ?: getOnlineChannel(channelId)
		return channel.also { setChannels(mapChannels.value + (channelId to it)) }
	}

	/** DB에 갱신된 LastChat, Participants와 기존에 존재하던
	 * 채널 부가 정보들을 반영해서 반환 (ex : topPin, roomTags, ..) */
	private suspend fun getOfflineChannel(channelId: Long): Channel? {
		return channelDAO.getChannel(channelId)?.toChannel()
	}

	/** LastChat을 비롯한 Channel detail 정보가 없음
	 * + 해당 채팅방에 입장하지 않은 채로 해당 API 호출하면 예외 던짐
	 * {"errorCode":"4040400","message":"채팅방을 찾을 수 없습니다."}*/
	private suspend fun getOnlineChannel(channelId: Long): Channel {
		val response = runCatching { channelApi.getChannel(channelId).toChannel() }
			.getOrDefault(
				Channel.DEFAULT.copy(
					roomId = channelId,
					roomName = "존재하지 않는 채팅방"
				)
			)
		channelDAO.upsertChannel(response.toChannelEntity())
		return response
	}

	/** 지수 백오프 적용 */
	override suspend fun getChannelInfo(
		channelId: Long,
		maxAttempts: Int,
	): ChannelInfo? {
		for (attempt in 0 until maxAttempts) {
			val response = runCatching { channelApi.getChannelInfo(channelId) }
				.onFailure { delay((DEFAULT_RETRY_ATTEMPT_DELAY_TIME * (1.5).pow(attempt))) }
				.getOrNull() ?: continue

			when (response) {
				is BookChatApiResult.Success -> {
					val channelInfo = response.data.toDomain()
					channelDAO.updateDetailInfo(
						roomId = channelId,
						roomName = channelInfo.roomName,
						roomHostId = channelInfo.roomHost.id,
						roomMemberCount = channelInfo.participants.size,
						participantIds = channelInfo.participantIds,
						participantAuthorities = channelInfo.participantAuthorities,
						bookTitle = channelInfo.bookTitle,
						bookAuthors = channelInfo.bookAuthors,
						bookCoverImageUrl = channelInfo.bookCoverImageUrl,
						roomTags = channelInfo.roomTags,
						roomCapacity = channelInfo.roomCapacity,
					)
					val updatedChannel = getOfflineChannel(channelId) ?: return null
					setChannels(mapChannels.value + (channelId to updatedChannel))
					return channelInfo
				}

				is BookChatApiResult.Failure -> {
					val failBody =
						response.body?.let { jsonSerializer.decodeFromString<BookChatFailResponseBody>(it) }
					when (failBody?.errorCode) {
						RESPONSE_CODE_CHANNEL_PARTICIPANT_NOT_FOUND -> Unit
						RESPONSE_CODE_CHANNEL_IS_BANNED -> banChannelClient(channelId = channelId)
						RESPONSE_CODE_CHANNEL_IS_EXPLODED -> leaveChannelHost(channelId)
						else -> throw Exception("failed to get channel info")
					}
					return null
				}
			}
		}
		throw IOException("failed to get channel info")
	}

	override suspend fun makeChannel(
		channelTitle: String,
		channelSize: Int,
		channelDefaultImageType: ChannelDefaultImageType,
		channelTags: List<String>,
		selectedBook: Book,
		channelImage: ByteArray?,
	): Channel {
		val response = channelApi.makeChannel(
			chatRoomImage = channelImage?.toMultiPartBody(
				contentType = CONTENT_TYPE_IMAGE_WEBP,
				multipartName = IMAGE_MULTIPART_NAME,
				fileName = IMAGE_FILE_NAME,
				fileExtension = IMAGE_FILE_EXTENSION_WEBP
			),
			requestMakeChannel = RequestMakeChannel(
				roomName = channelTitle,
				roomSize = channelSize,
				defaultRoomImageType = channelDefaultImageType.toNetwork(),
				hashTags = channelTags,
				bookRequest = selectedBook.toBookRequest()
			),
		)
		val createdChannelId = response.locationHeader
			?: throw Exception("ChannelId does not exist in Http header.")
		return getChannel(createdChannelId)
	}

	override suspend fun changeChannelSetting(
		channelId: Long,
		channelTitle: String,
		channelCapacity: Int,
		channelTags: List<String>,
		channelImage: ByteArray?,
		isProfileChanged: Boolean,
	) {
		channelApi.changeChannelSetting(
			channelId = channelId,
			requestChangeChannelSetting = RequestChangeChannelSetting(
				channelId = channelId,
				channelTitle = channelTitle,
				channelCapacity = channelCapacity,
				channelTags = channelTags,
				isProfileChanged = isProfileChanged
			),
			chatRoomImage = channelImage?.toMultiPartBody(
				contentType = CONTENT_TYPE_IMAGE_WEBP,
				multipartName = IMAGE_MULTIPART_NAME,
				fileName = IMAGE_FILE_NAME,
				fileExtension = IMAGE_FILE_EXTENSION_WEBP
			)
		)
		getOnlineChannel(channelId)
	}

	//TODO : 방장 나갈 경우 받는 메세지에 넘어오는 메세지 nullable타입 서버 수정 대기 중
	override suspend fun leaveChannel(channelId: Long) {
		val response = channelApi.leaveChannel(channelId)

		suspend fun onSuccess() {
			channelDAO.delete(channelId)
			setChannels(mapChannels.value - channelId)
		}

		when (response) {
			is BookChatApiResult.Success -> onSuccess()

			is BookChatApiResult.Failure -> {
				val failBody =
					response.body?.let { jsonSerializer.decodeFromString<BookChatFailResponseBody>(it) }
				when (failBody?.errorCode) {
					RESPONSE_CODE_CHANNEL_PARTICIPANT_NOT_FOUND -> onSuccess()
					RESPONSE_CODE_CHANNEL_IS_EXPLODED -> onSuccess()
					else -> throw IOException("failed to leave channel")
				}
			}
		}
	}

	override suspend fun leaveChannelMember(channelId: Long, targetUserId: Long) {
		val channel = getChannel(channelId)
		val newParticipants = channel.participants?.filter { it.id != targetUserId }
		val newParticipantAuthorities = channel.participantAuthorities?.minus(targetUserId)

		val newChannel = channel.copy(
			participants = newParticipants,
			roomMemberCount = newParticipants?.size ?: 0,
			participantAuthorities = newParticipantAuthorities
		)
		channelDAO.updateChannelMember(
			channelId = channelId,
			participantIds = newParticipants?.map { it.id },
			roomMemberCount = newParticipants?.size ?: 0,
			participantAuthorities = newParticipantAuthorities
		)
		setChannels(mapChannels.value + mapOf(channelId to newChannel))
	}

	override suspend fun leaveChannelHost(channelId: Long) {
		channelDAO.updateExploded(channelId)
		val updatedChannel = getOfflineChannel(channelId) ?: return
		setChannels(mapChannels.value + (channelId to updatedChannel))
	}

	override suspend fun muteChannel(channelId: Long) {
		channelDAO.updateNotificationFlag(
			channelId = channelId,
			isNotificationOn = false
		)
		val updatedChannel = getOfflineChannel(channelId) ?: return
		setChannels(mapChannels.value + (channelId to updatedChannel))
	}

	override suspend fun unMuteChannel(channelId: Long) {
		channelDAO.updateNotificationFlag(
			channelId = channelId,
			isNotificationOn = true
		)
		val updatedChannel = getOfflineChannel(channelId) ?: return
		setChannels(mapChannels.value + (channelId to updatedChannel))
	}

	override suspend fun topPinChannel(channelId: Long) {
		val maxTopPinNum = channelDAO.getMaxTopPinNum()
		channelDAO.updateTopPin(
			channelId = channelId,
			isTopPinNum = maxTopPinNum + 1
		)
		val updatedChannel = getOfflineChannel(channelId) ?: return
		setChannels(mapChannels.value + (channelId to updatedChannel))
	}

	override suspend fun unPinChannel(channelId: Long) {
		channelDAO.updateTopPin(
			channelId = channelId,
			isTopPinNum = 0
		)
		val updatedChannel = getOfflineChannel(channelId) ?: return
		setChannels(mapChannels.value + (channelId to updatedChannel))
	}

	override suspend fun enterChannel(channel: Channel) {
		if (channelDAO.isChannelExist(channel.roomId)) return

		val response = channelApi.enterChannel(channel.roomId)
		when (response) {
			is BookChatApiResult.Success -> {
				channelDAO.upsertChannel(channel.toChannelEntity())
				setChannels(mapChannels.value + mapOf(channel.roomId to channel))
			}

			is BookChatApiResult.Failure -> {
				val failBody =
					response.body?.let { jsonSerializer.decodeFromString<BookChatFailResponseBody>(it) }
				when (failBody?.errorCode) {
					RESPONSE_CODE_ALREADY_ENTERED_CHANNEL -> Unit
					RESPONSE_CODE_CHANNEL_IS_FULL -> throw ChannelIsFullException(failBody.message)
					RESPONSE_CODE_CHANNEL_IS_BANNED -> throw UserIsBannedException(failBody.message)
					RESPONSE_CODE_CHANNEL_IS_EXPLODED -> throw ChannelIsExplodedException(failBody.message)
					else -> throw IOException("failed to enter channel")
				}
			}
		}
	}

	override suspend fun enterChannelMember(channelId: Long, targetUserId: Long) {
		val channel = getChannel(channelId)
		val targetUser = User.DEFAULT.copy(id = targetUserId)
		val newParticipants = channel.participants?.plus(targetUser)
		val newParticipantAuthorities = channel.participantAuthorities
			?.plus(targetUser.id to ChannelMemberAuthority.GUEST)

		val newChannel = channel.copy(
			participants = newParticipants,
			roomMemberCount = newParticipants?.size ?: 0,
			participantAuthorities = newParticipantAuthorities
		)
		channelDAO.updateChannelMember(
			channelId = channelId,
			participantIds = newParticipants?.map { it.id },
			roomMemberCount = newParticipants?.size ?: 0,
			participantAuthorities = newParticipantAuthorities
		)
		setChannels(mapChannels.value + mapOf(channelId to newChannel))
	}

	override suspend fun banChannelClient(channelId: Long) {
		val channel = getChannel(channelId)

		val newChannel = channel.copy(
			participants = emptyList(),
			roomMemberCount = 0,
			participantAuthorities = mapOf(),
			isBanned = true
		)
		channelDAO.banChannelMember(
			channelId = channelId,
			participantIds = emptyList(),
			roomMemberCount = 0,
			participantAuthorities = mapOf(),
			isBanned = true
		)
		setChannels(mapChannels.value + mapOf(channelId to newChannel))
	}

	override suspend fun banChannelMember(
		channelId: Long,
		targetUserId: Long,
		needServer: Boolean,
	) {
		val channel = getChannel(channelId)

		if (needServer) {
			channelApi.banChannelMember(
				channelId = channel.roomId,
				userId = targetUserId,
			)
		}

		val newParticipants = channel.participants?.filter { it.id != targetUserId }
		val newParticipantAuthorities = channel.participantAuthorities?.minus(targetUserId)
		val newChannel = channel.copy(
			participants = newParticipants,
			roomMemberCount = newParticipants?.size ?: 0,
			participantAuthorities = newParticipantAuthorities,
			isBanned = false
		)
		channelDAO.banChannelMember(
			channelId = channelId,
			participantIds = newParticipants?.map { it.id },
			roomMemberCount = newParticipants?.size ?: 0,
			participantAuthorities = newParticipantAuthorities,
			isBanned = false
		)
		setChannels(mapChannels.value + mapOf(channelId to newChannel))
	}

	override suspend fun updateChannelMemberAuthority(
		channelId: Long,
		targetUserId: Long,
		channelMemberAuthority: ChannelMemberAuthority,
		needServer: Boolean,
	) {

		if (needServer) {
			channelApi.updateChannelMemberAuthority(
				channelId = channelId,
				targetUserId = targetUserId,
				authority = channelMemberAuthority.toNetwork()
			)
		}

		val channel = getChannel(channelId)
		val newParticipantAuthorities = channel.participantAuthorities
			?.plus(targetUserId to channelMemberAuthority)
		val newChannel = channel.copy(
			participantAuthorities = newParticipantAuthorities
		)

		channelDAO.updateChannelMemberAuthorities(
			channelId = channelId,
			participantAuthorities = newParticipantAuthorities
		)
		setChannels(mapChannels.value + mapOf(channelId to newChannel))
	}

	override suspend fun updateChannelHost(
		channelId: Long,
		targetUserId: Long,
		needServer: Boolean,
	) {
		if (needServer) {
			channelApi.updateChannelMemberAuthority(
				channelId = channelId,
				targetUserId = targetUserId,
				authority = ChannelMemberAuthorityNetwork.HOST
			)
		}

		val channel = getChannel(channelId)
		val previousHostId = channel.host?.id ?: throw IOException("hostId does not exist")
		val newParticipantAuthorities = channel.participantAuthorities
			?.plus(previousHostId to ChannelMemberAuthority.GUEST)
			?.plus(targetUserId to ChannelMemberAuthority.HOST)

		val newChannel = channel.copy(
			host = User.DEFAULT.copy(id = targetUserId),
			participantAuthorities = newParticipantAuthorities
		)

		channelDAO.updateChannelHost(
			channelId = channelId,
			targetUserId = targetUserId,
			participantAuthorities = newParticipantAuthorities
		)
		setChannels(mapChannels.value + mapOf(channelId to newChannel))
	}

	override suspend fun updateLastReadChatIdIfValid(channelId: Long, chatId: Long) {
		val channel = channelDAO.getChannel(channelId)
		val existingLastReadChatId = channel?.lastReadChatId
		if (existingLastReadChatId != null && existingLastReadChatId >= chatId) return

		channelDAO.updateLastReadChat(
			channelId = channelId,
			lastReadChatId = chatId
		)
		val updatedChannel = getOfflineChannel(channelId) ?: return
		setChannels(mapChannels.value + mapOf(channelId to updatedChannel))
	}

	/** 지수 백오프 적용 */
	override suspend fun getMostActiveChannels(
		loadSize: Int,
		maxAttempts: Int,
	): List<Channel> {

		setChannels(
			mapChannels.value + getOfflineMostActiveChannels(
				loadSize = loadSize
			).associateBy { it.roomId }
		)

		for (attempt in 0 until maxAttempts) {
			val response = runCatching {
				channelApi.getChannels(
					postCursorId = null,
					size = loadSize
				)
			}.onFailure { delay((DEFAULT_RETRY_ATTEMPT_DELAY_TIME * (1.5).pow(attempt))) }
				.getOrNull() ?: continue

			channelDAO.upsertAllChannels(response.channels.toChannelEntity())
			isEndPage = response.cursorMeta.last
			currentPage = response.cursorMeta.nextCursorId
			val newChannels = response.channels.map { getOfflineChannel(it.roomId) ?: it.toChannel() }
			setChannels(mapChannels.value + newChannels.associateBy { it.roomId })
			return response.channels.map { it.toChannel() }
		}
		throw IOException("failed to retrieve most active channels")
	}

	//TODO : 서버측에서 lastChatID가 가장 높은 순으로 쿼리되게 혹은 쿼리 옵션을 주게 수정 대기
	//  페이징 방식 수정되면 수정된 offset방식으로 getMostActiveChannels처럼 로컬 우선 쿼리로 수정
	/** 로컬 데이터 우선적으로 쿼리 */
	/** Channel 세부 정보는 채팅방 들어 가면 getChannelInfo에 의해 갱신될 예정 */
	override suspend fun getChannels(loadSize: Int): List<Channel>? {
		if (isEndPage) return null

		val response = channelApi.getChannels(
			postCursorId = currentPage,
			size = loadSize
		)

		channelDAO.upsertAllChannels(response.channels.toChannelEntity())
		isEndPage = response.cursorMeta.last
		currentPage = response.cursorMeta.nextCursorId
		val newChannels = response.channels.map { getOfflineChannel(it.roomId) ?: it.toChannel() }
		setChannels(mapChannels.value + newChannels.associateBy { it.roomId })
		return response.channels.map { it.toChannel() }
	}

	private suspend fun getOfflineMostActiveChannels(loadSize: Int): List<Channel> {
		return channelDAO.getMostActiveChannels(loadSize, 0)
			.map { it.toChannel() }
	}

	override suspend fun updateChannelLastChatIfValid(channelId: Long, chatId: Long) {
		val existingLastChatId = channelDAO.getChannel(channelId)?.lastChatId
		if (existingLastChatId != null && chatId <= existingLastChatId) return

		channelDAO.updateLastChat(
			channelId = channelId,
			lastChatId = chatId,
		)

		val updatedChannel = getOfflineChannel(channelId) ?: return
		setChannels(mapChannels.value + (channelId to updatedChannel))
	}

	private fun clearCachedData() {
		mapChannels.update { emptyMap() }
		currentPage = null
		isEndPage = false
	}

	/** 로그아웃 + 회원탈퇴시에 모든 repository 일괄 호출 */
	override suspend fun clear() {
		clearCachedData()
		channelDAO.deleteAll()
	}

	companion object {
		private const val CONTENT_TYPE_IMAGE_WEBP = "image/webp"
		private const val IMAGE_FILE_NAME = "profile_img"
		private const val IMAGE_FILE_EXTENSION_WEBP = ".webp"
		private const val IMAGE_MULTIPART_NAME = "chatRoomImage"
		private val DEFAULT_RETRY_ATTEMPT_DELAY_TIME = 1.seconds
	}

}