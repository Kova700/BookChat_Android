package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.bookchat.App
import com.example.bookchat.data.User
import com.example.bookchat.data.local.entity.ChatRoomEntity
import com.example.bookchat.paging.ReadingBookTapPagingSource
import com.example.bookchat.paging.remotemediator.ChatRoomRemoteMediator.Companion.REMOTE_USER_CHAT_ROOM_LOAD_SIZE
import com.example.bookchat.repository.UserChatRoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userChatRoomRepository: UserChatRoomRepository
) : ViewModel() {

    val user = MutableStateFlow<User>(App.instance.getCachedUser())
    val database = App.instance.database

    init {
        getRemoteUserChatRoomList()
    }

    val readingBookResult by lazy {
        Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ReadingBookTapPagingSource() }
        ).flow
            .map { pagingData ->
                pagingData.map { pair ->
                    pair.first.getBookShelfDataItem()
                }
            }.cachedIn(viewModelScope)
    }

    val chatRoomFlow = database.chatRoomDAO().getActivatedChatRoomList(MAIN_CHAT_ROOM_LIST_LOAD_SIZE)

    private fun getRemoteUserChatRoomList() = viewModelScope.launch {
        val chatRoomList =
            userChatRoomRepository.getUserChatRoomList(REMOTE_USER_CHAT_ROOM_LOAD_SIZE)
        saveChatRoomInLocalDB(chatRoomList.map { it.toChatRoomEntity() })
    }

    private suspend fun saveChatRoomInLocalDB(chatRoomList: List<ChatRoomEntity>) {
        database.chatRoomDAO().insertOrUpdateAllChatRoom(chatRoomList)
    }

    companion object {
        private const val MAIN_CHAT_ROOM_LIST_LOAD_SIZE = 3
    }
}