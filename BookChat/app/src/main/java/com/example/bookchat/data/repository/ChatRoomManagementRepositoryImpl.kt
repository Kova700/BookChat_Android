package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.api.BookChatApi
import com.example.bookchat.data.database.dao.ChatRoomDAO
import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.RespondChatRoomInfo
import com.example.bookchat.domain.repository.ChatRoomManagementRepository
import com.example.bookchat.domain.repository.UserRepository
import javax.inject.Inject

class ChatRoomManagementRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val chatRoomDAO: ChatRoomDAO,
	private val userRepository: UserRepository,
) : ChatRoomManagementRepository {

	override suspend fun enterChatRoom(roomId: Long) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		bookChatApi.enterChatRoom(roomId)
	}

	override suspend fun leaveChatRoom(roomId: Long) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		bookChatApi.leaveChatRoom(roomId)
	}

	override suspend fun getChatRoomInfo(roomId: Long) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		val response = bookChatApi.getChatRoomInfo(roomId)
		saveUserDataInLocalDB(response)
		saveChatRoomDataInLocalDB(roomId, response)
	}

	override suspend fun updateMemberCount(roomId: Long, offset: Int) {
		chatRoomDAO.updateMemberCount(roomId, offset)
	}

	override suspend fun updateLastChat(chat: ChatEntity) {
		chatRoomDAO.updateLastChatInfo(
			roomId = chat.chatRoomId,
			lastChatId = chat.chatId,
			lastActiveTime = chat.dispatchTime,
			lastChatContent = chat.message
		)
	}

	private suspend fun saveUserDataInLocalDB(chatRoomInfo: RespondChatRoomInfo) {
		val chatRoomUserList = mutableListOf(chatRoomInfo.roomHost)
		chatRoomInfo.roomSubHostList?.let { chatRoomUserList.addAll(it) }
		chatRoomInfo.roomGuestList?.let { chatRoomUserList.addAll(it) }
		userRepository.insertOrUpdateAllUser(chatRoomUserList.map { it.toUserEntity() })
	}

	private suspend fun saveChatRoomDataInLocalDB(roomId: Long, chatRoomInfo: RespondChatRoomInfo) {
		// TODO : 채팅방 제목이 추가되어야함 (채팅방 제목 바뀌면 알 수가 없음)
		chatRoomDAO.updateDetailInfo(
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