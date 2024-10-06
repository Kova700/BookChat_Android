package com.kova700.bookchat.feature.createchannel

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.core.navigation.ChannelNavigator
import com.kova700.bookchat.core.navigation.ImageCropNavigator
import com.kova700.bookchat.core.navigation.ImageCropNavigator.Companion.EXTRA_CROPPED_IMAGE_CACHE_URI
import com.kova700.bookchat.core.navigation.ImageCropNavigator.ImageCropPurpose
import com.kova700.bookchat.feature.createchannel.databinding.ActivityMakeChannelBinding
import com.kova700.bookchat.feature.createchannel.dialog.MakeChannelImageSelectDialog
import com.kova700.bookchat.feature.search.createchannel.MakeChannelSelectBookActivity
import com.kova700.bookchat.feature.search.searchdetail.SearchDetailActivity.Companion.EXTRA_SELECTED_BOOK_ISBN
import com.kova700.bookchat.util.channel.MakeChannelImgSizeManager
import com.kova700.bookchat.util.image.bitmap.getImageBitmap
import com.kova700.bookchat.util.image.image.deleteImageCache
import com.kova700.bookchat.util.image.image.loadChangedChannelProfile
import com.kova700.bookchat.util.image.image.loadUrl
import com.kova700.bookchat.util.permissions.galleryPermissions
import com.kova700.bookchat.util.permissions.getPermissionsLauncher
import com.kova700.bookchat.util.snackbar.showSnackBar
import com.kova700.bookchat.util.toast.makeToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MakeChannelActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMakeChannelBinding

	private val makeChannelViewModel: MakeChannelViewModel by viewModels()

	@Inject
	lateinit var makeChannelImgSizeManager: MakeChannelImgSizeManager

	@Inject
	lateinit var imageCropNavigator: ImageCropNavigator

	@Inject
	lateinit var channelNavigator: ChannelNavigator

	private val imm by lazy {
		getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
	}

	private val permissionsLauncher = getPermissionsLauncher(
		onSuccess = { moveToImageCrop() },
		onDenied = {
			makeToast(R.string.gallery_permission_denied)
		},
		onExplained = {
			makeToast(R.string.gallery_permission_explained)
			val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
			val uri = Uri.fromParts(SCHEME_PACKAGE, packageName, null)
			intent.data = uri
			startActivity(intent)
		}
	)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMakeChannelBinding.inflate(layoutInflater)
		setContentView(binding.root)
		observeUiState()
		observeUiEvent()
		initViewState()
	}

	private fun observeUiState() = lifecycleScope.launch {
		makeChannelViewModel.uiState.collect { state ->
			setViewState(state)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		makeChannelViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun startImageEdit() {
		permissionsLauncher.launch(galleryPermissions)
	}

	private fun initViewState() {
		initChannelHashTagBar()
		initChannelTitleInputBar()
		makeChannelImgSizeManager.setMakeChannelImgSize(binding.channelImg)
		with(binding) {
			xBtn.setOnClickListener { makeChannelViewModel.onClickXBtn() }
			makeChannelFinishBtn.setOnClickListener {
				makeChannelViewModel.onClickFinishBtn()
				closeKeyboard()
			}
			textClearBtn.setOnClickListener { makeChannelViewModel.onClickTextClearBtn() }
			selectBootBtn.setOnClickListener { makeChannelViewModel.onClickSelectBookBtn() }
			deleteSelectedBookBtn.setOnClickListener { makeChannelViewModel.onClickDeleteSelectedBookBtn() }
			cameraBtn.setOnClickListener { makeChannelViewModel.onClickCameraBtn() }
		}
	}

	private fun closeKeyboard() {
		imm.hideSoftInputFromWindow(
			currentFocus?.windowToken,
			InputMethodManager.HIDE_NOT_ALWAYS
		)
		binding.hashTagEt.clearFocus()
		binding.channelTitleEt.clearFocus()
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
		setChannelImage(uiState)
		setSubmitTextState(uiState)
		setChannelTitleEditTextState(uiState)
		setChannelTagEditTextState(uiState)
		setSelectBookImage(uiState)
		with(binding) {
			selectedBookTitleTv.isSelected = true
			selectedBookAuthorsTv.isSelected = true
			selectedBookAuthorsTv.text = uiState.selectedBook?.authorsString
			selectedBookTitleTv.text = uiState.selectedBook?.title
			textClearBtn.visibility = if (uiState.channelTitle.isNotEmpty()) View.VISIBLE else View.GONE
			channelTitleCountTv.text =
				getString(R.string.make_chat_room_title_length, uiState.channelTitle.length)
			channelTagCountTv.text =
				getString(R.string.make_chat_room_tags_length, uiState.channelTag.length)
			selectedBookCv.visibility =
				if (uiState.selectedBook != null) View.VISIBLE else View.GONE
			selectBootBtn.visibility =
				if (uiState.selectedBook == null) View.VISIBLE else View.GONE
			deleteSelectedBookBtn.visibility =
				if (uiState.selectedBook != null) View.VISIBLE else View.GONE
		}
	}

	private fun setSelectBookImage(uiState: MakeChannelUiState) {
		binding.selectedBookImgIv.loadUrl(uiState.selectedBook?.bookCoverImageUrl)
	}

	private fun setChannelImage(uiState: MakeChannelUiState) {
		binding.channelImg.loadChangedChannelProfile(
			imageUrl = null,
			channelDefaultImageType = uiState.defaultProfileImageType,
			bitmap = uiState.channelProfileImage,
		)
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
				val uri = result.data?.getStringExtra(EXTRA_CROPPED_IMAGE_CACHE_URI)
					?: return@registerForActivityResult
				getCroppedImageBitmap(uri)
			}
		}

	private fun getCroppedImageBitmap(uri: String) = lifecycleScope.launch {
		val croppedImageBitmap = uri.getImageBitmap(this@MakeChannelActivity) ?: return@launch
		makeChannelViewModel.onChangeChannelImg(croppedImageBitmap)
		deleteImageCache(uri)
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
		imageCropNavigator.navigate(
			currentActivity = this,
			imageCropPurpose = ImageCropPurpose.CHANNEL_PROFILE,
			resultLauncher = cropActivityResultLauncher,
		)
	}

	private fun moveToBookSelect() {
		val intent = Intent(this, MakeChannelSelectBookActivity::class.java)
		selectBookResultLauncher.launch(intent)
	}

	private fun moveToChannel(channelId: Long) {
		channelNavigator.navigate(
			currentActivity = this,
			channelId = channelId,
			shouldFinish = true
		)
	}

	private fun showChannelImageSelectDialog() {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_CHANNEL_IMAGE_SELECT)
		if (existingFragment != null) return

		val dialog = MakeChannelImageSelectDialog(
			onSelectChangeDefaultImage = { makeChannelViewModel.onClickChangeDefaultImage() },
			onSelectGallery = { makeChannelViewModel.onClickGallery() }
		)
		dialog.show(supportFragmentManager, DIALOG_TAG_CHANNEL_IMAGE_SELECT)
	}

	private fun handleEvent(event: MakeChannelEvent) = when (event) {
		is MakeChannelEvent.MoveToBack -> finish()
		is MakeChannelEvent.MoveToBookSelect -> moveToBookSelect()
		is MakeChannelEvent.OpenGallery -> startImageEdit()
		is MakeChannelEvent.MoveToChannel -> moveToChannel(event.channelId)
		is MakeChannelEvent.ShowSnackBar -> binding.root.showSnackBar(event.stringId)
		MakeChannelEvent.ShowChannelImageSelectDialog -> showChannelImageSelectDialog()
	}

	companion object {
		private const val SCHEME_PACKAGE = "package"
		private const val DIALOG_TAG_CHANNEL_IMAGE_SELECT = "DIALOG_TAG_CHANNEL_IMAGE_SELECT"
	}
}