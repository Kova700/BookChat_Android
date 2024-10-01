package com.kova700.bookchat.feature.search.searchdetail.mapper

import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.data.search.channel.external.model.ChannelSearchResult
import com.kova700.bookchat.feature.search.mapper.toBookItem
import com.kova700.bookchat.feature.search.mapper.toChannelItem
import com.kova700.bookchat.feature.search.model.SearchResultItem
import com.kova700.bookchat.feature.search.searchdetail.SearchDetailUiState
import com.kova700.bookchat.util.book.BookImgSizeManager

//둘 중 하나라도 비어있다면 BothEmpty 아이템을 추가하고 반환
// (이미 만들어놨기 때문에 여기서 해도 되고, 밖에서 따로 정의해도 되고)
// 어쩄든, InitRetry는 밖에 따로 만들어야함

fun List<Book>.toBookSearchResultDetailItem(
	bookImgSizeManager: BookImgSizeManager,
	uiState: SearchDetailUiState.UiState
): List<SearchResultItem> {
	val items = mutableListOf<SearchResultItem>()
	if (this.isEmpty()) return items
	items.addAll(this.map { it.toBookItem() })
	val dummyItemCount = bookImgSizeManager.getFlexBoxDummyItemCount(this.size)
	(0 until dummyItemCount).forEach { i -> items.add(SearchResultItem.BookDummy(i)) }
	if (uiState == SearchDetailUiState.UiState.PAGING_ERROR) items.add(SearchResultItem.PagingRetry)
	return items
}

fun List<ChannelSearchResult>.toChannelSearchResultDetailItem(
	uiState: SearchDetailUiState.UiState
): List<SearchResultItem> {
	val items = mutableListOf<SearchResultItem>()
	items.addAll(this.map { it.toChannelItem() })
	if (uiState == SearchDetailUiState.UiState.PAGING_ERROR) items.add(SearchResultItem.PagingRetry)
	return items
}