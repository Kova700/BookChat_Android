package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.api.BookChatApi
import com.example.bookchat.data.database.dao.ChannelDAO
import com.example.bookchat.data.mapper.toChannel
import com.example.bookchat.data.mapper.toChannelEntity
import com.example.bookchat.data.request.RequestMakeChatRoom
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.RespondChatRoomInfo
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import okhttp3.Headers
import okhttp3.MultipartBody
import javax.inject.Inject

//TODO : 채팅방 정보 조회 API 실패 시 재시도 로직 필요함 (채팅 전송 재시도 로직같은)
//TODO : 채팅방 사전에 전부 로드해오는 로직 추가필요 ( FCM 통해 받은 채팅 저장 하기 위해서  OR FCM 통해 받은 채팅의 채팅방 정보 보여주기 위해)
//TODO : 채널 ID, 메세지 ID를 FCM을 통해 받고 추가 정보를 API로 요청 후 Noti를 띄우는 방식하면 전부 가져오지 않아도 됨
class ChannelRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val channelDAO: ChannelDAO,
	private val userRepository: UserRepository,
	private val chatRepository: ChatRepository
) : ChannelRepository {

	private val mapChannels = MutableStateFlow<Map<Long, Channel>>(emptyMap())//(channelId, Channel)
	private val channels = mapChannels.map {
		//ORDER BY top_pin_num DESC, last_chat_id DESC, room_id DESC
		it.values.toList().sortedWith(
			compareBy(
				{ channel -> -channel.topPinNum },
				{ channel -> channel.lastChat?.chatId?.unaryMinus() },
				{ channel -> -channel.roomId })
		)
	}.onEach { cachedChannels = it }

	private var cachedChannels: List<Channel> = emptyList()
	private val currentChannelId = MutableStateFlow<Long?>(null)
	private val currentChannel = channels.combine(currentChannelId) { channels, channelId ->
		channels.firstOrNull { channel -> channel.roomId == channelId }
	}.filterNotNull()

	private var currentPage: Long? = null
	private var isEndPage = false

	//TODO : 상태 clear 함수 필요
	override fun getChannelsFlow(): Flow<List<Channel>> {
		return channels
	}

	override fun getChannelFlow(channelId: Long): Flow<Channel> {
		currentChannelId.value = channelId
		return currentChannel
	}

	private fun setChannels(newChannels: Map<Long, Channel>) {
		mapChannels.update { newChannels }
	}

	override suspend fun getChannel(channelId: Long): Channel {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		getChannelInfo(channelId)
		val updatedChannel = getChannelWithInfo(channelId)
		setChannels(mapChannels.value + (channelId to updatedChannel))
		return updatedChannel
	}

	//Channel 세부 정보는 채팅방 들어 가면 getChannel에 의해 갱신될 예정
	override suspend fun getChannels(loadSize: Int): List<Channel> {
		if (isEndPage) return cachedChannels
		if (isNetworkConnected().not()) throw NetworkIsNotConnectedException()

		val response = bookChatApi.getChannels(
			postCursorId = currentPage,
			size = loadSize
		)
		isEndPage = response.cursorMeta.last
		currentPage = response.cursorMeta.nextCursorId

		channelDAO.upsertAllChannels(response.channels.toChannelEntity())
		val chats = response.channels.map {
			Chat.DEFAULT.copy(
				chatId = it.lastChatId,
				chatRoomId = it.roomId,
				message = it.lastChatContent
			)
		}
		chatRepository.insertAllChats(chats)
		val newChannels = response.channels.map { getChannelWithInfo(it.roomId) }
		setChannels(mapChannels.value + newChannels.associateBy { it.roomId })
		return newChannels
	}

	//DB에 저장된 LastChat과, 기존에 저장되어있던 채널 정보들을 묶어서 반환 (like topPin..)
	private suspend fun getChannelWithInfo(channelId: Long): Channel {
		val channel =
			channelDAO.getChannel(channelId)?.toChannel() ?: Channel.DEFAULT.copy(roomId = channelId)
		return channel.copy(
			lastChat = channel.lastChat?.chatId?.let { chatRepository.getChat(it) },
			host = channel.host?.id?.let { userRepository.getUser(it) },
			subHosts = channel.subHosts?.map { user -> userRepository.getUser(user.id) },
			guests = channel.guests?.map { user -> userRepository.getUser(user.id) }
		)
	}

	override suspend fun makeChannel(
		requestMakeChatRoom: RequestMakeChatRoom,
		charRoomImage: MultipartBody.Part?
	): Channel {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val response = bookChatApi.makeChatRoom(
			requestMakeChatRoom = requestMakeChatRoom,
			chatRoomImage = charRoomImage
		)

		val createdChannel = getChannelFromHeader(
			headers = response.headers(),
			requestMakeChatRoom = requestMakeChatRoom
		)
		channelDAO.upsertChannel(createdChannel.toChannelEntity())
		setChannels(mapChannels.value + mapOf(Pair(createdChannel.roomId, createdChannel)))
		return createdChannel
	}

	private fun getChannelFromHeader(
		headers: Headers,
		requestMakeChatRoom: RequestMakeChatRoom
	): Channel {
		return Channel(
			roomId = headers["RoomId"]?.toLong() ?: throw Exception("RoomID not received"),
			roomSid = headers["Location"]?.split("/")?.last()
				?: throw Exception("RoomSID not received"),
			roomImageUri = headers["RoomImageUri"],
			roomName = requestMakeChatRoom.roomName,
			defaultRoomImageType = requestMakeChatRoom.defaultRoomImageType,
			roomMemberCount = 1
		)
	}

	override suspend fun enter(channel: Channel) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		bookChatApi.enterChatRoom(channel.roomId)
		val newChannel = channel.toChannelEntity()
			.copy(lastChatId = Long.MAX_VALUE)
		channelDAO.upsertChannel(newChannel)
		setChannels(mapChannels.value + mapOf(Pair(channel.roomId, channel)))
	}

	override suspend fun leave(channelId: Long) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		bookChatApi.leaveChatRoom(channelId)
		channelDAO.delete(channelId)
		setChannels(mapChannels.value - channelId)
	}

	private suspend fun getChannelInfo(roomId: Long) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		val response = bookChatApi.getChatRoomInfo(roomId)
		saveParticipantsDataInLocalDB(response)
		saveChannelInfoInLocalDB(roomId, response)
	}

	override suspend fun updateMemberCount(channelId: Long, offset: Int) {
		channelDAO.updateMemberCount(channelId, offset)
	}

	override suspend fun updateLastChat(channelId: Long, chatId: Long) {
		val existingLastChatId = channelDAO.getChannel(channelId)?.toChannel()?.lastChat?.chatId
		if (existingLastChatId != null && chatId <= existingLastChatId) return

		channelDAO.updateLastChat(
			roomId = channelId,
			lastChatId = chatId,
		)

		val updatedChannel = getChannelWithInfo(channelId)
		setChannels(mapChannels.value + (channelId to updatedChannel))
	}

	private suspend fun saveParticipantsDataInLocalDB(chatRoomInfo: RespondChatRoomInfo) {
		userRepository.upsertAllUsers(chatRoomInfo.getParticipants())
	}

	private suspend fun saveChannelInfoInLocalDB(roomId: Long, chatRoomInfo: RespondChatRoomInfo) {
		// TODO : 채팅방 제목이 추가되어야함 (채팅방 제목 바뀌면 알 수가 없음)
		channelDAO.updateDetailInfo(
			roomId = roomId,
			hostId = chatRoomInfo.roomHost.id,
			subHostIds = chatRoomInfo.roomSubHostList?.map { it.id },
			guestIds = chatRoomInfo.roomGuestList?.map { it.id },
			bookTitle = chatRoomInfo.bookTitle,
			bookAuthors = chatRoomInfo.bookAuthors,
			bookCoverImageUrl = chatRoomInfo.bookCoverImageUrl,
			roomTags = chatRoomInfo.roomTags,
			roomCapacity = chatRoomInfo.roomCapacity,
		)
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}

}