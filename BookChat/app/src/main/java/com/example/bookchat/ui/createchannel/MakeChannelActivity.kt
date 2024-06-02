package com.example.bookchat.ui.createchannel

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityMakeChannelBinding
import com.example.bookchat.ui.channel.chatting.ChannelActivity
import com.example.bookchat.ui.channelList.ChannelListFragment.Companion.EXTRA_CHANNEL_ID
import com.example.bookchat.ui.imagecrop.ImageCropActivity
import com.example.bookchat.ui.search.searchdetail.SearchDetailActivity.Companion.EXTRA_SELECTED_BOOK_ISBN
import com.example.bookchat.utils.MakeChannelImgSizeManager
import com.example.bookchat.utils.PermissionManager
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO : 채팅방 이미지 랜덤으로 안돌아가고 고정되어있음
@AndroidEntryPoint
class MakeChannelActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMakeChannelBinding

	private val makeChannelViewModel: MakeChannelViewModel by viewModels()

	@Inject
	lateinit var makeChannelImgSizeManager: MakeChannelImgSizeManager

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_make_channel)
		binding.viewmodel = makeChannelViewModel
		binding.lifecycleOwner = this
		observeUiState()
		observeUiEvent()
		initView()
	}

	private val permissionsLauncher =
		PermissionManager.getPermissionsLauncher(this) { moveToImageCrop() }

	private fun observeUiState() = lifecycleScope.launch {
		makeChannelViewModel.uiState.collect { state ->
			setViewState(state)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		makeChannelViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun startImageEdit() {
		permissionsLauncher.launch(PermissionManager.getGalleryPermissions())
	}

	private fun initView() {
		initChannelHashTagBar()
		initChannelTitleInputBar()
		makeChannelImgSizeManager.setMakeChannelImgSize(binding.channelImg)
	}

	private fun initChannelHashTagBar() {
		binding.hashTagEt.addTextChangedListener { text: Editable? ->
			makeChannelViewModel.onChangeHashTag(text.toString())
		}
	}

	private fun initChannelTitleInputBar() {
		binding.channelTitleEt.addTextChangedListener { text: Editable? ->
			makeChannelViewModel.onChangeChannelTitle(text.toString())
		}
	}

	private fun setViewState(uiState: MakeChannelUiState) {
		setSubmitTextState(uiState)
		setChannelTitleEditTextState(uiState)
		setChannelTagEditTextState(uiState)
	}

	private fun setChannelTitleEditTextState(uiState: MakeChannelUiState) {
		with(binding.channelTitleEt) {
			if (text.toString() == uiState.channelTitle) return
			setText(uiState.channelTitle)
			setSelection(uiState.channelTitle.length)
		}
	}

	private fun setChannelTagEditTextState(uiState: MakeChannelUiState) {
		with(binding.hashTagEt) {
			if (text.toString() == uiState.channelTag) return
			setText(uiState.channelTag)
			setSelection(uiState.channelTag.length)
		}
	}

	private fun setSubmitTextState(uiState: MakeChannelUiState) {
		with(binding.makeChannelFinishBtn) {
			if (uiState.isPossibleMakeChannel) {
				setTextColor(Color.parseColor("#000000"))
				isClickable = true
				return
			}

			setTextColor(Color.parseColor("#B5B7BB"))
			isClickable = false
		}
	}

	private val cropActivityResultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == RESULT_OK) {
				val intent = result.data
				val bitmapByteArray =
					intent?.getByteArrayExtra(ImageCropActivity.EXTRA_CROPPED_PROFILE_BYTE_ARRAY)
				bitmapByteArray?.let { makeChannelViewModel.onChangeChannelImg(it) }
			}
		}

	private val selectBookResultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == RESULT_OK) {
				val intent = result.data
				val selectedBookISBN = intent?.getStringExtra(EXTRA_SELECTED_BOOK_ISBN)
				selectedBookISBN?.let { makeChannelViewModel.onChangeSelectedBook(it) }
			}
		}

	private fun moveToImageCrop() {
		val intent = Intent(this, ImageCropActivity::class.java)
		cropActivityResultLauncher.launch(intent)
	}

	private fun moveToBookSelect() {
		val intent = Intent(this, MakeChannelSelectBookActivity::class.java)
		selectBookResultLauncher.launch(intent)
	}

	private fun moveToChannel(channelId: Long) {
		val intent = Intent(this, ChannelActivity::class.java)
		intent.putExtra(EXTRA_CHANNEL_ID, channelId)
		startActivity(intent)
		finish()
	}

	private fun handleEvent(event: MakeChannelEvent) = when (event) {
		is MakeChannelEvent.MoveToBack -> finish()
		is MakeChannelEvent.MoveToBookSelect -> moveToBookSelect()
		is MakeChannelEvent.OpenGallery -> startImageEdit()
		is MakeChannelEvent.MoveToChannel -> moveToChannel(event.channelId)
		is MakeChannelEvent.MakeToast -> makeToast(event.stringId)
	}
}