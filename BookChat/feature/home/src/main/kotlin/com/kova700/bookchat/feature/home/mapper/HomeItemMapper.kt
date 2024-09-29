package com.kova700.bookchat.feature.home.mapper

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.feature.home.HomeUiState.UiState
import com.kova700.bookchat.feature.home.model.HomeItem
import com.kova700.bookchat.util.book.BookImgSizeManager

fun groupItems(
	clientNickname: String,
	books: List<BookShelfItem>? = null,
	channels: List<Channel>? = null,
	bookImgSizeManager: BookImgSizeManager,
	bookUiState: UiState,
	channelUiState: UiState,
): List<HomeItem> {
	val groupedItems = mutableListOf<HomeItem>()
	groupedItems.add(HomeItem.Header(clientNickname = clientNickname))

	if (bookUiState != UiState.INIT_LOADING) groupedItems.add(HomeItem.BookHeader)
	when {
		bookUiState == UiState.INIT_ERROR -> groupedItems.add(HomeItem.BookRetry)
		bookUiState == UiState.INIT_LOADING -> groupedItems.add(HomeItem.BookLoading)
		books.isNullOrEmpty() -> groupedItems.add(HomeItem.BookEmpty)
		else -> {
			val defaultSize = bookImgSizeManager.flexBoxBookSpanSize
			val exposureItemCount = minOf(books.size, defaultSize)
			groupedItems.addAll(books.take(exposureItemCount).toHomeBookItems())
			val dummyItemCount = bookImgSizeManager.getFlexBoxDummyItemCount(exposureItemCount)
			(0 until dummyItemCount).forEach { i -> groupedItems.add(HomeItem.BookDummy(i)) }
		}
	}

	if (channelUiState != UiState.INIT_LOADING) groupedItems.add(HomeItem.ChannelHeader)
	when {
		channelUiState == UiState.INIT_ERROR -> groupedItems.add(HomeItem.ChannelRetry)
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