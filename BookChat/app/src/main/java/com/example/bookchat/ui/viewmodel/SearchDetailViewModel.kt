package com.example.bookchat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.paging.SearchResultBookPagingSource
import com.example.bookchat.data.paging.SearchResultChatRoomPagingSource
import com.example.bookchat.domain.repository.BookSearchRepository
import com.example.bookchat.domain.repository.WholeChatRoomRepository
import com.example.bookchat.ui.viewmodel.SearchViewModel.NecessaryDataFlagInDetail
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.ChatSearchFilter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class SearchDetailViewModel @AssistedInject constructor(
	private val bookSearchRepository: BookSearchRepository,
	private val wholeChatRoomRepository: WholeChatRoomRepository,
	@Assisted private val searchKeyword: String,
	@Assisted val necessaryDataFlag: NecessaryDataFlagInDetail,
	@Assisted val chatSearchFilter: ChatSearchFilter
) : ViewModel() {

	val pagingBookData by lazy {
		Pager(
			config = PagingConfig(
				pageSize = SEARCH_BOOKS_ITEM_LOAD_SIZE,
				enablePlaceholders = false
			),
			pagingSourceFactory = {
				SearchResultBookPagingSource(
					searchKeyword = searchKeyword,
					bookSearchRepository = bookSearchRepository,
				)
			}
		).flow
			.cachedIn(viewModelScope)
	}

	val pagingChatRoomData by lazy {
		Pager(
			config = PagingConfig(
				pageSize = SEARCH_CHAT_ROOMS_LOAD_SIZE,
				enablePlaceholders = false
			),
			pagingSourceFactory = {
				SearchResultChatRoomPagingSource(
					searchKeyword = searchKeyword,
					chatSearchFilter = chatSearchFilter,
					wholeChatRoomRepository = wholeChatRoomRepository
				)
			}
		).flow
			.cachedIn(viewModelScope)
	}

	fun getHeaderTitle(): String {
		return when (necessaryDataFlag) {
			is NecessaryDataFlagInDetail.Book -> {
				App.instance.applicationContext.resources.getString(R.string.book)
			}

			is NecessaryDataFlagInDetail.ChatRoom -> {
				App.instance.applicationContext.resources.getString(R.string.chat_room)
			}
		}
	}

	fun isNecessaryDataFlagBook(flag: NecessaryDataFlagInDetail) =
		flag is NecessaryDataFlagInDetail.Book

	fun isNecessaryDataFlagChatRoom(flag: NecessaryDataFlagInDetail) =
		flag is NecessaryDataFlagInDetail.ChatRoom

	@dagger.assisted.AssistedFactory
	interface AssistedFactory {
		fun create(
			searchKeyword: String,
			necessaryDataFlag: NecessaryDataFlagInDetail,
			chatSearchFilter: ChatSearchFilter
		): SearchDetailViewModel
	}

	companion object {
		private val SEARCH_BOOKS_ITEM_LOAD_SIZE = BookImgSizeManager.flexBoxBookSpanSize * 2
		private const val SEARCH_CHAT_ROOMS_LOAD_SIZE = 7

		fun provideFactory(
			assistedFactory: AssistedFactory,
			searchKeyword: String,
			necessaryDataFlag: NecessaryDataFlagInDetail,
			chatSearchFilter: ChatSearchFilter
		): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				return assistedFactory.create(
					searchKeyword,
					necessaryDataFlag,
					chatSearchFilter
				) as T
			}
		}
	}

}