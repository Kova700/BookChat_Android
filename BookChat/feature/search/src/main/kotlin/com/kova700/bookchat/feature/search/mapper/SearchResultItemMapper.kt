package com.kova700.bookchat.feature.search.mapper

import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.data.search.channel.external.model.ChannelSearchResult
import com.kova700.bookchat.feature.search.SearchUiState.SearchResultUiState
import com.kova700.bookchat.feature.search.SearchUiState.SearchResultUiState.INIT_ERROR
import com.kova700.bookchat.feature.search.SearchUiState.SearchResultUiState.INIT_LOADING
import com.kova700.bookchat.feature.search.SearchUiState.SearchResultUiState.SUCCESS
import com.kova700.bookchat.feature.search.model.SearchPurpose
import com.kova700.bookchat.feature.search.model.SearchResultItem
import com.kova700.bookchat.util.book.BookImgSizeManager

fun groupItems(
	books: List<Book>? = null,
	channels: List<ChannelSearchResult>? = null,
	searchPurpose: SearchPurpose,
	bookImgSizeManager: BookImgSizeManager,
	bookSearchResultUiState: SearchResultUiState,
	channelSearchResultUiState: SearchResultUiState,
): List<SearchResultItem> {
	val groupedItems = mutableListOf<SearchResultItem>()

	if (bookSearchResultUiState == SUCCESS
		&& channelSearchResultUiState == SUCCESS
		&& books.isNullOrEmpty()
		&& channels.isNullOrEmpty()
	) {
		groupedItems.add(SearchResultItem.BothEmpty)
		return groupedItems
	}

	fun addBookItems() {
		if (bookSearchResultUiState != INIT_LOADING) groupedItems.add(SearchResultItem.BookHeader)
		when {
			bookSearchResultUiState == INIT_ERROR -> groupedItems.add(SearchResultItem.BookRetry)
			bookSearchResultUiState == INIT_LOADING -> groupedItems.add(SearchResultItem.BookLoading)
			books.isNullOrEmpty() -> groupedItems.add(SearchResultItem.BookEmpty)
			else -> {
				val defaultItemSize = bookImgSizeManager.flexBoxBookSpanSize * 2
				val exposureItemCount = minOf(books.size, defaultItemSize)
				groupedItems.addAll(books.take(exposureItemCount).map { it.toBookItem() })
				val dummyItemCount = bookImgSizeManager.getFlexBoxDummyItemCount(exposureItemCount)
				(0 until dummyItemCount).forEach { i -> groupedItems.add(SearchResultItem.BookDummy(i)) }
			}
		}
	}

	fun addChannelItems() {
		if (channelSearchResultUiState != INIT_LOADING) groupedItems.add(SearchResultItem.ChannelHeader)
		when {
			channelSearchResultUiState == INIT_ERROR -> groupedItems.add(SearchResultItem.ChannelRetry)
			channelSearchResultUiState == INIT_LOADING -> groupedItems.add(SearchResultItem.ChannelLoading)
			channels.isNullOrEmpty() -> groupedItems.add(SearchResultItem.ChannelEmpty)
			else -> {
				val exposureItemCount = minOf(channels.size, 3)
				groupedItems.addAll(channels.take(exposureItemCount).map { it.toChannelItem() })
			}
		}
	}

	when (searchPurpose) {
		SearchPurpose.SEARCH_BOTH -> {
			addBookItems()
			addChannelItems()
		}

		SearchPurpose.MAKE_CHANNEL -> addBookItems()
		SearchPurpose.SEARCH_CHANNEL -> addChannelItems()
	}

	return groupedItems
}


fun Book.toBookItem(): SearchResultItem.BookItem {
	return SearchResultItem.BookItem(
		isbn = isbn,
		title = title,
		authors = authors,
		publisher = publisher,
		publishAt = publishAt,
		bookCoverImageUrl = bookCoverImageUrl
	)
}

fun SearchResultItem.BookItem.toBook(): Book {
	return Book(
		isbn = isbn,
		title = title,
		authors = authors,
		publisher = publisher,
		publishAt = publishAt,
		bookCoverImageUrl = bookCoverImageUrl
	)
}

fun List<Book>.toBookSearchResultItem(flexBoxDummyItemCount: Int): List<SearchResultItem> {
	val groupedItems = mutableListOf<SearchResultItem>()
	groupedItems.addAll(this.map { it.toBookItem() })
	(0 until flexBoxDummyItemCount)
		.forEach { i -> groupedItems.add(SearchResultItem.BookDummy(i)) }
	return groupedItems
}


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