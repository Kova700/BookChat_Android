package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.api.BookChatApi
import com.example.bookchat.data.database.dao.ChannelDAO
import com.example.bookchat.data.database.dao.ChatDAO
import com.example.bookchat.data.mapper.toChannel
import com.example.bookchat.data.mapper.toChannelEntity
import com.example.bookchat.data.mapper.toChatEntity
import com.example.bookchat.data.request.RequestMakeChatRoom
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.RespondChatRoomInfo
import com.example.bookchat.data.response.getLastChat
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import okhttp3.Headers
import okhttp3.MultipartBody
import javax.inject.Inject

//TODO : 채팅방 정보 조회 API 실패 시 재시도 로직 필요함 (채팅 전송 재시도 로직같은)
class ChannelRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val channelDAO: ChannelDAO,
	private val chatDAO: ChatDAO,
	private val userRepository: UserRepository,
) : ChannelRepository {

	private val mapChannels = MutableStateFlow<Map<Long, Channel>>(emptyMap())//(cid, channel)
	private val channels = mapChannels.map { it.values.toList() }
	private val _currentChannel = MutableStateFlow<Channel?>(null)
	private var currentPage: Long? = null
	private var isEndPage = false

	override fun getChannelsFlow(): Flow<List<Channel>> {
		return channels
	}

	override fun getChannelFlow(channelId: Long): StateFlow<Channel?> {
		return _currentChannel.asStateFlow()
	}

	override suspend fun getChannels(loadSize: Int) {
		if (isEndPage) return
		if (isNetworkConnected().not()) throw NetworkIsNotConnectedException()

		val response = bookChatApi.getChannels(
			postCursorId = currentPage,
			size = loadSize
		)
		isEndPage = response.cursorMeta.last
		currentPage = response.cursorMeta.nextCursorId
		channelDAO.upsertAllChannels(response.channels.toChannelEntity())
		chatDAO.insertAllChat(response.channels.mapNotNull { it.getLastChat() }.toChatEntity())
		val newMapChannels = response.channels.associate { it.roomId to it.toChannel() }
		mapChannels.emit(mapChannels.value + newMapChannels)
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
		mapChannels.emit(mapChannels.value + mapOf(Pair(createdChannel.roomId, createdChannel)))
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
		mapChannels.emit(mapChannels.value + mapOf(Pair(channel.roomId, channel)))
	}

	override suspend fun leave(channelId: Long) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		bookChatApi.leaveChatRoom(channelId)
		channelDAO.delete(channelId)
		mapChannels.emit(mapChannels.value.minus(channelId))
	}

	override suspend fun getChannelInfo(roomId: Long) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		val response = bookChatApi.getChatRoomInfo(roomId)
		saveUserDataInLocalDB(response)
		saveChannelInfoInLocalDB(roomId, response)
	}

	override suspend fun updateMemberCount(channelId: Long, offset: Int) {
		channelDAO.updateMemberCount(channelId, offset)
	}

	override suspend fun updateLastChat(channelId: Long, chatId: Long) {
		channelDAO.updateLastChat(
			roomId = channelId,
			lastChatId = chatId,
		)
	}

	private suspend fun saveUserDataInLocalDB(chatRoomInfo: RespondChatRoomInfo) {
		val chatRoomUserList = mutableListOf(chatRoomInfo.roomHost)
		chatRoomInfo.roomSubHostList?.let { chatRoomUserList.addAll(it) }
		chatRoomInfo.roomGuestList?.let { chatRoomUserList.addAll(it) }
		userRepository.upsertAllUsers(chatRoomUserList.map { it.toUser() })
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