package com.example.bookchat.ui.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.data.UserChatRoomListItem
import com.example.bookchat.data.local.entity.ChatRoomEntity
import com.example.bookchat.data.request.RequestMakeChatRoom
import com.example.bookchat.data.repository.ChatRoomManagementRepository
import com.example.bookchat.data.repository.UserChatRoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MakeChatRoomViewModel @Inject constructor(
    private val userChatRoomRepository: UserChatRoomRepository,
    private val chatRoomManagementRepository: ChatRoomManagementRepository
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<MakeChatRoomUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val chatRoomTitle = MutableStateFlow<String>("")
    val chatRoomTag = MutableStateFlow<String>("")
    val selectedBook = MutableStateFlow<Book?>(null)
    val chatRoomProfileImage = MutableStateFlow(byteArrayOf())
    val defaultProfileImageType = MutableStateFlow(Random().nextInt(7) + 1)

    fun requestMakeChatRoom() = viewModelScope.launch {
        if (!isPossibleMakeChatRoom()) return@launch
        runCatching { makeChatRoom() }
            .onSuccess { enterChatRoom(it) }
            .onFailure { makeToast(R.string.make_chat_room_fail) }
    }

    private fun enterChatRoom(chatRoomItem: UserChatRoomListItem) = viewModelScope.launch {
        runCatching { chatRoomManagementRepository.enterChatRoom(chatRoomItem.roomId) }
            .onSuccess { enterChatRoomSuccessCallBack(chatRoomItem.toChatRoomEntity()) }
            .onFailure { makeToast(R.string.enter_chat_room_fail) }
    }

    private fun enterChatRoomSuccessCallBack(chatRoomEntity: ChatRoomEntity) {
        saveChatRoomInLocalDB(chatRoomEntity.copy(lastChatId = Long.MAX_VALUE))
        startEvent(MakeChatRoomUiEvent.MoveToChatPage(chatRoomEntity))
    }

    private fun saveChatRoomInLocalDB(chatRoomEntity: ChatRoomEntity) = viewModelScope.launch {
        App.instance.database.chatRoomDAO().insertOrUpdateChatRoom(chatRoomEntity)
    }

    private suspend fun makeChatRoom() = userChatRoomRepository.makeChatRoom(
        getRequestMakeChatRoom(), getMultiPartBody(chatRoomProfileImage.value)
    )

    fun clickDeleteTextBtn() {
        chatRoomTitle.value = ""
    }

    fun clickXBtn() {
        startEvent(MakeChatRoomUiEvent.MoveToBack)
    }

    fun clickSelectBookBtn() {
        startEvent(MakeChatRoomUiEvent.MoveSelectBook)
    }

    fun clickDeleteSelectBookBtn() {
        selectedBook.value = null
    }

    fun clickImgEditBtn() {
        startEvent(MakeChatRoomUiEvent.OpenGallery)
    }

    fun isPossibleMakeChatRoom(
        chatRoomTitle: String,
        chatRoomTag: String,
        selectedBook: Book?,
    ) = chatRoomTitle.trim().isNotBlank() &&
            chatRoomTag.trim().isNotBlank() && (selectedBook != null)

    private fun isPossibleMakeChatRoom() =
        chatRoomTitle.value.trim().isNotBlank() &&
                chatRoomTag.value.trim().isNotBlank() && (selectedBook.value != null)

    private fun getRequestMakeChatRoom(): RequestMakeChatRoom {
        return RequestMakeChatRoom(
            roomName = chatRoomTitle.value.trim(),
            roomSize = DEFAULT_ROOM_SIZE,
            defaultRoomImageType = defaultProfileImageType.value,
            hashTags = getHashTags(),
            bookRequest = selectedBook.value!!
        )
    }

    private fun getHashTags(): List<String> =
        chatRoomTag.value.split(" ").filter { it.isNotBlank() }
            .map { it.split("#") }.flatten().filter { it.isNotBlank() }

    private fun getMultiPartBody(bitmapByteArray: ByteArray): MultipartBody.Part? {
        if (bitmapByteArray.isEmpty()) return null
        val imageMultipartBody = byteArrayToRequestBody(bitmapByteArray)
        return MultipartBody.Part.createFormData(
            IMAGE_MULTIPART_NAME,
            IMAGE_FILE_NAME + IMAGE_FILE_EXTENSION_WEBP,
            imageMultipartBody
        )
    }

    private fun byteArrayToRequestBody(byteArray: ByteArray): RequestBody {
        return RequestBody.create(CONTENT_TYPE_IMAGE_WEBP.toMediaTypeOrNull(), byteArray)
    }

    private fun makeToast(stringId: Int) {
        Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
    }

    private fun startEvent(event: MakeChatRoomUiEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class MakeChatRoomUiEvent {
        object MoveToBack : MakeChatRoomUiEvent()
        object MoveSelectBook : MakeChatRoomUiEvent()
        data class MoveToChatPage(val chatRoomEntity: ChatRoomEntity) : MakeChatRoomUiEvent()
        object OpenGallery : MakeChatRoomUiEvent()
    }

    companion object {
        private const val DEFAULT_ROOM_SIZE = 100
        private const val CONTENT_TYPE_IMAGE_WEBP = "image/webp"
        private const val IMAGE_FILE_NAME = "profile_img"
        private const val IMAGE_FILE_EXTENSION_WEBP = ".webp"
        private const val IMAGE_MULTIPART_NAME = "chatRoomImage"
    }
}