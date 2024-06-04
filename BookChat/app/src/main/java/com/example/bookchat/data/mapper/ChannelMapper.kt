package com.example.bookchat.data.mapper

import com.example.bookchat.data.database.model.ChannelEntity
import com.example.bookchat.data.network.model.response.ChannelResponse
import com.example.bookchat.data.network.model.response.ChannelSearchResponse
import com.example.bookchat.data.network.model.response.ChannelSingleSearchResponse
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.ChannelMemberAuthority
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.User

fun ChannelResponse.toChannelEntity(): ChannelEntity {
	return ChannelEntity(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType.toDomain(),
		roomImageUri = roomImageUri,
		lastChatId = lastChatId,
		isBanned = isBanned,
		isExploded = isExploded
	)
}

fun ChannelResponse.toChannel(clientId: Long): Channel {
	return Channel(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType.toDomain(),
		roomImageUri = roomImageUri,
		lastChat = getLastChat(clientId),
		isBanned = isBanned,
		isExploded = isExploded
	)
}

fun Channel.toChannelEntity(): ChannelEntity {
	return ChannelEntity(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType,
		isNotificationOn = notificationFlag,
		topPinNum = topPinNum,
		isBanned = isBanned,
		isExploded = isExploded,
		roomImageUri = roomImageUri,
		lastChatId = lastChat?.chatId,
		hostId = host?.id,
		participantIds = participantIds,
		participantAuthorities = participantAuthorities,
		roomTags = roomTags,
		roomCapacity = roomCapacity,
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUrl
	)
}

suspend fun ChannelEntity.toChannel(
	getChat: suspend (Long) -> Chat,
	getUser: suspend (Long) -> User
): Channel {
	return Channel(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType,
		notificationFlag = isNotificationOn,
		topPinNum = topPinNum,
		isBanned = isBanned,
		isExploded = isExploded,
		roomImageUri = roomImageUri,
		lastChat = lastChatId?.let { getChat(it) },
		lastReadChatId = lastReadChatId,
		host = hostId?.let { getUser(it) },
		participants = participantIds?.map { getUser(it) },
		participantAuthorities = participantAuthorities,
		roomTags = roomTags,
		roomCapacity = roomCapacity,
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUrl,
	)
}

//TODO : Channel말고 isEntered 필드 반영하는 새로운 도메인 레이어 data class만들어야할듯?
suspend fun ChannelSearchResponse.toChannel(
	clientId: Long,
	getUser: suspend (Long) -> User
): Channel {
	return Channel(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomImageUri = roomImageUri,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType.toDomain(),
		roomTags = tags.split(","),
		roomCapacity = roomSize,
		host = host,
		participants = listOf(host),
		participantAuthorities = mapOf(host.id to ChannelMemberAuthority.HOST),
		lastChat = this.getLastChat(
			clientId = clientId,
			getUser = getUser
		),
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUri,
		isBanned = isBanned
		//isEntered
	)
}

//TODO : lastChatId 추가되면 상당히 편할 듯
fun ChannelSingleSearchResponse.toChannel(): Channel {
	return Channel(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType.toDomain(),
		roomImageUri = roomImageUri,
	)
}

fun List<ChannelResponse>.toChannelEntity() =
	this.map { it.toChannelEntity() }