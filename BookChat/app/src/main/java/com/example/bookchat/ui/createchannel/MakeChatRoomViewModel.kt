package com.example.bookchat.ui.createchannel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.mapper.toBookRequest
import com.example.bookchat.data.request.RequestMakeChatRoom
import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.repository.ChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.Random
import javax.inject.Inject

@HiltViewModel
class MakeChatRoomViewModel @Inject constructor(
	private val channelRepository: ChannelRepository,
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<MakeChatRoomUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	val chatRoomTitle = MutableStateFlow<String>("")
	val chatRoomTag = MutableStateFlow<String>("")
	val selectedBook = MutableStateFlow<Book?>(null)
	val chatRoomProfileImage = MutableStateFlow(byteArrayOf())
	val defaultProfileImageType = MutableStateFlow(Random().nextInt(7) + 1)

	//TODO : 로딩 상태 변경으로 연속 요청 방지해야함 (채팅방 중복 생성됨)
	fun requestMakeChatRoom() = viewModelScope.launch {
		if (!isPossibleMakeChannel()) return@launch
		runCatching { makeChannel() }
			.onSuccess { enterChannel(it) }
			.onFailure { makeToast(R.string.make_chat_room_fail) }
	}

	private fun enterChannel(channel: Channel) = viewModelScope.launch {
		runCatching { channelRepository.enter(channel) }
			.onSuccess { startEvent(MakeChatRoomUiEvent.MoveToChatPage(channel.roomId)) }
			.onFailure { makeToast(R.string.enter_chat_room_fail) }
	}

	private suspend fun makeChannel() = channelRepository.makeChannel(
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

	fun isPossibleMakeChannel(
		chatRoomTitle: String,
		chatRoomTag: String,
		selectedBook: Book?,
	) = chatRoomTitle.trim().isNotBlank() &&
					chatRoomTag.trim().isNotBlank() && (selectedBook != null)

	private fun isPossibleMakeChannel() =
		chatRoomTitle.value.trim().isNotBlank() &&
						chatRoomTag.value.trim().isNotBlank() && (selectedBook.value != null)

	private fun getRequestMakeChatRoom(): RequestMakeChatRoom {
		return RequestMakeChatRoom(
			roomName = chatRoomTitle.value.trim(),
			roomSize = DEFAULT_ROOM_SIZE,
			defaultRoomImageType = defaultProfileImageType.value,
			hashTags = getHashTags(),
			bookRequest = selectedBook.value!!.toBookRequest()
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
		data class MoveToChatPage(val channelId: Long) : MakeChatRoomUiEvent()
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