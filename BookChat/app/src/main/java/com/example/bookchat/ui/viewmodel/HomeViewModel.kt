package com.example.bookchat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.bookchat.data.paging.ReadingBookTapPagingSource
import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.repository.BookRepository
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO : 도서, 채팅 Room에서 가져오는 로직으로 수정
@HiltViewModel
class HomeViewModel @Inject constructor(
	private val bookRepository: BookRepository,
	private val clientRepository: ClientRepository,
	private val channelRepository: ChannelRepository
) : ViewModel() {

	val cachedClient = MutableStateFlow<User>(User.Default)

	init {
		getClientInfo()
		getRemoteUserChatRoomList()
	}

	//PagingSource로 가져오는게 아닌,
	// API로 1회 호출해서 가져오게 수정
	val readingBookResult by lazy {
		Pager(
			config = PagingConfig(
				pageSize = 10,
				enablePlaceholders = false
			),
			pagingSourceFactory = {
				ReadingBookTapPagingSource(
					bookRepository = bookRepository
				)
			}
		).flow
			.map { pagingData ->
				pagingData.map { pair ->
					pair.first.getBookShelfDataItem()
				}
			}.cachedIn(viewModelScope)
	}

	val chatRoomFlow = channelRepository.getChannelsFlow()

	private fun getRemoteUserChatRoomList() = viewModelScope.launch {
		channelRepository.getChannels()
	}

	private fun getClientInfo() = viewModelScope.launch {
		runCatching { clientRepository.getClientProfile() }
			.onSuccess { user -> cachedClient.update { user } }
	}

	companion object {
		private const val MAIN_CHAT_ROOM_LIST_LOAD_SIZE = 3
	}
}