package com.kova700.bookchat.feature.search.mapper

import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.data.search.channel.external.model.ChannelSearchResult
import com.kova700.bookchat.feature.search.model.SearchPurpose
import com.kova700.bookchat.feature.search.model.SearchResultItem
import com.kova700.bookchat.util.book.BookImgSizeManager

fun groupItems(
	books: List<Book>? = null,
	channels: List<ChannelSearchResult>? = null,
	searchPurpose: SearchPurpose,
	bookImgSizeManager: BookImgSizeManager,
): List<SearchResultItem> {
	val groupedItems = mutableListOf<SearchResultItem>()
	if (books.isNullOrEmpty() && channels.isNullOrEmpty()) return groupedItems

	fun addBookItems() {
		groupedItems.add(SearchResultItem.BookHeader)
		if (books.isNullOrEmpty()) {
			groupedItems.add(SearchResultItem.BookEmpty)
		} else {
			val defaultSize = bookImgSizeManager.flexBoxBookSpanSize * 2
			val exposureItemCount = if (books.size > defaultSize) defaultSize else books.size
			groupedItems.addAll(books.take(exposureItemCount).map { it.toBookItem() })

			val dummyItemCount = bookImgSizeManager.getFlexBoxDummyItemCount(exposureItemCount)
			(0 until dummyItemCount).forEach { i -> groupedItems.add(SearchResultItem.BookDummy(i)) }
		}
	}

	fun addChannelItems() {
		groupedItems.add(SearchResultItem.ChannelHeader)
		if (channels.isNullOrEmpty()) {
			groupedItems.add(SearchResultItem.ChannelEmpty)
		} else {
			val exposureItemCount = if (channels.size > 3) 3 else channels.size
			groupedItems.addAll(channels.take(exposureItemCount).map { it.toChannelItem() })
		}
	}

	when (searchPurpose) {
		SearchPurpose.SEARCH_BOTH -> {
			addBookItems()
			addChannelItems()
		}

		SearchPurpose.MAKE_CHANNEL -> {
			addBookItems()
		}

		SearchPurpose.SEARCH_CHANNEL -> {
			addChannelItems()
		}
	}

	return groupedItems
}