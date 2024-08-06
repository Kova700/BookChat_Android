package com.example.bookchat.ui.home.mapper

import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.ui.home.HomeUiState.UiState
import com.example.bookchat.ui.home.model.HomeItem
import com.example.bookchat.utils.BookImgSizeManager

fun groupItems(
	clientNickname: String,
	bookshelfItems: List<BookShelfItem>? = null,
	channels: List<Channel>? = null,
	bookImgSizeManager: BookImgSizeManager,
	bookUiState: UiState,
	channelUiState: UiState,
): List<HomeItem> {
	val groupedItems = mutableListOf<HomeItem>()
	groupedItems.add(HomeItem.Header(clientNickname = clientNickname))

	if (bookUiState != UiState.INIT_LOADING) groupedItems.add(HomeItem.BookHeader)
	when {
		bookUiState == UiState.ERROR -> groupedItems.add(HomeItem.BookRetry)
		bookUiState == UiState.INIT_LOADING -> groupedItems.add(HomeItem.BookLoading)
		bookshelfItems.isNullOrEmpty() -> groupedItems.add(HomeItem.BookEmpty)
		else -> {
			val defaultSize = bookImgSizeManager.flexBoxBookSpanSize
			val exposureItemCount = minOf(bookshelfItems.size, defaultSize)
			groupedItems.addAll(bookshelfItems.take(exposureItemCount).toHomeBookItems())
			val dummyItemCount = bookImgSizeManager.getFlexBoxDummyItemCount(exposureItemCount)
			(0 until dummyItemCount).forEach { i -> groupedItems.add(HomeItem.BookDummy(i)) }
		}
	}

	if (channelUiState != UiState.INIT_LOADING) groupedItems.add(HomeItem.ChannelHeader)
	when {
		channelUiState == UiState.ERROR -> groupedItems.add(HomeItem.ChannelRetry)
		channelUiState == UiState.INIT_LOADING -> groupedItems.add(HomeItem.ChannelLoading)
		channels.isNullOrEmpty() -> groupedItems.add(HomeItem.ChannelEmpty)
		else -> groupedItems.addAll(channels.take(3).toHomeChannelItems())
	}

	return groupedItems
}

private fun List<BookShelfItem>.toHomeBookItems(): List<HomeItem> {
	return map { it.toHomeItem() }
}

private fun BookShelfItem.toHomeItem(): HomeItem.BookItem {
	return HomeItem.BookItem(
		bookShelfId = bookShelfId,
		book = book,
		state = state,
		lastUpdatedAt = lastUpdatedAt,
	)
}

private fun List<Channel>.toHomeChannelItems(): List<HomeItem> {
	return map { it.toHomeItem() }
}

private fun Channel.toHomeItem(): HomeItem.ChannelItem {
	return HomeItem.ChannelItem(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType,
		notificationFlag = notificationFlag,
		topPinNum = topPinNum,
		isBanned = isBanned,
		isExploded = isExploded,
		roomImageUri = roomImageUri,
		lastReadChatId = lastReadChatId,
		lastChat = lastChat,
		host = host,
	)
}