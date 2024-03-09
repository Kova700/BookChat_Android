package com.example.bookchat.ui.createchannel

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityMakeChatRoomBinding
import com.example.bookchat.domain.model.Book
import com.example.bookchat.ui.imagecrop.ImageCropActivity
import com.example.bookchat.ui.channel.ChannelActivity
import com.example.bookchat.ui.channelList.ChannelListFragment.Companion.EXTRA_CHAT_ROOM_ID
import com.example.bookchat.ui.createchannel.MakeChatRoomViewModel.MakeChatRoomUiEvent
import com.example.bookchat.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MakeChatRoomActivity : AppCompatActivity() {

	private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
	private lateinit var binding: ActivityMakeChatRoomBinding
	private val makeChatRoomViewModel: MakeChatRoomViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_make_chat_room)
		permissionsLauncher = PermissionManager.getPermissionsLauncher(this) { openCropActivity() }
		with(binding) {
			viewmodel = makeChatRoomViewModel
			lifecycleOwner = this@MakeChatRoomActivity
		}
		observeUiEvent()
	}

	private fun startImgEdit() {
		permissionsLauncher.launch(PermissionManager.getGalleryPermissions())
	}

	private fun openCropActivity() {
		val intent = Intent(this, ImageCropActivity::class.java)
		cropActivityResultLauncher.launch(intent)
	}

	private val cropActivityResultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == RESULT_OK) {
				val intent = result.data
				val bitmapByteArray =
					intent?.getByteArrayExtra(ImageCropActivity.EXTRA_CROPPED_PROFILE_BYTE_ARRAY)
						?: byteArrayOf()
				makeChatRoomViewModel.chatRoomProfileImage.value = bitmapByteArray
			}
		}

	private fun moveToSelectBook() {
		val intent = Intent(this, MakeChatRoomSelectBookActivity::class.java)
		selectBookResultLauncher.launch(intent)
	}

	private val selectBookResultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == RESULT_OK) {
				val intent = result.data
//				val selectBook =
//					intent?.getSerializableExtra(MakeChatRoomSelectBookActivity.EXTRA_SELECTED_BOOK) as? Book
//				selectBook?.let { makeChatRoomViewModel.selectedBook.value = selectBook }
			}
		}

	private fun observeUiEvent() = lifecycleScope.launch {
		makeChatRoomViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun startChatRoomActivity(channelId: Long) {
		val intent = Intent(this, ChannelActivity::class.java)
		intent.putExtra(EXTRA_CHAT_ROOM_ID, channelId)
		startActivity(intent)
		finish()
	}

	private fun handleEvent(event: MakeChatRoomUiEvent) = when (event) {
		is MakeChatRoomUiEvent.MoveToBack -> finish()
		is MakeChatRoomUiEvent.MoveSelectBook -> moveToSelectBook()
		is MakeChatRoomUiEvent.OpenGallery -> startImgEdit()
		is MakeChatRoomUiEvent.MoveToChatPage -> startChatRoomActivity(event.channelId)
	}
}