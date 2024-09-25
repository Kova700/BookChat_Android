package com.kova700.bookchat.feature.search.mapper

import com.kova700.bookchat.core.data.search.channel.external.model.ChannelSearchResult
import com.kova700.bookchat.feature.search.model.SearchResultItem

fun ChannelSearchResult.toChannelItem(): SearchResultItem.ChannelItem {
	return SearchResultItem.ChannelItem(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType,
		roomImageUri = roomImageUri,
		lastChat = lastChat,
		host = host,
		tagsString = tagsString,
		roomCapacity = roomCapacity,
	)
}

fun List<ChannelSearchResult>.toChannelSearchResultItem(): List<SearchResultItem> {
	val groupedItems = mutableListOf<SearchResultItem>()
	groupedItems.addAll(this.map { it.toChannelItem() })
	return groupedItems
}