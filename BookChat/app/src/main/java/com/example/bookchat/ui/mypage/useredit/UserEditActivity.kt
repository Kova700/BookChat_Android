package com.example.bookchat.ui.mypage.useredit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityUserEditBinding
import com.example.bookchat.ui.imagecrop.ImageCropActivity
import com.example.bookchat.ui.imagecrop.ImageCropActivity.Companion.EXTRA_CROPPED_PROFILE_BYTE_ARRAY
import com.example.bookchat.ui.mypage.useredit.dialog.UserProfileEditDialog
import com.example.bookchat.utils.image.loadChangedUserProfile
import com.example.bookchat.utils.makeToast
import com.example.bookchat.utils.permissions.galleryPermissions
import com.example.bookchat.utils.permissions.getPermissionsLauncher
import com.example.bookchat.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@AndroidEntryPoint
class UserEditActivity : AppCompatActivity() {

	private lateinit var binding: ActivityUserEditBinding
	private val userEditViewModel: UserEditViewModel by viewModels()

	private val imm by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

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
		binding = DataBindingUtil.setContentView(this, R.layout.activity_user_edit)
		binding.lifecycleOwner = this
		binding.viewmodel = userEditViewModel
		setFocus()
		initViewState()
		observeUiState()
		observeUiEvent()
	}

	private fun observeUiState() = lifecycleScope.launch {
		userEditViewModel.uiState.collect { state ->
			setViewState(state)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		userEditViewModel.eventFlow.collect { event -> handleUiEvent(event) }
	}

	private fun setViewState(state: UserEditUiState) {
		setLoadingViewState(state)
		setNickNameEditTextState(state)
		setSubmitBtnState(state)
		setProfileImageViewState(state)
	}

	private fun setLoadingViewState(state: UserEditUiState) {
		binding.progressBar.visibility =
			if (state.uiState == UserEditUiState.UiState.LOADING) View.VISIBLE else View.GONE
	}

	private fun setProfileImageViewState(state: UserEditUiState) {
		binding.userProfileIv.loadChangedUserProfile(
			imageUrl = state.client.profileImageUrl,
			userDefaultProfileType = state.client.defaultProfileImageType,
			byteArray = state.clientNewImage
		)
	}

	private fun setNickNameEditTextState(state: UserEditUiState) {
		with(binding.nickNameEt) {
			if (text.toString() != state.newNickname) {
				setText(state.newNickname)
				setSelection(state.newNickname.length)
			}
		}
	}

	private fun setSubmitBtnState(state: UserEditUiState) {
		with(binding.nicknameSubmitBtn) {
			visibility = if (userEditViewModel.uiState.value.isExistsChange) View.VISIBLE else View.GONE
			setText(
				if (state.isNeedDuplicatesNicknameCheck) R.string.duplicate_check
				else R.string.submit_changed_profile
			)
		}
	}

	private val specialCharFilter = InputFilter { source, _, _, _, _, _ ->
		val pattern = Pattern.compile(NAME_CHECK_REGULAR_EXPRESSION)
		if (pattern.matcher(source).matches().not()) {
			userEditViewModel.onEnteredSpecialChar()
			return@InputFilter ""
		}
		source
	}

	private fun initViewState() {
		initNickNameEditText()
		binding.cameraBtn.setOnClickListener { userEditViewModel.onClickCameraBtn() }
	}

	private val maxLengthFilter = InputFilter.LengthFilter(MAX_NICKNAME_LENGTH)

	private fun initNickNameEditText() {
		with(binding.nickNameEt) {
			filters = arrayOf(specialCharFilter, maxLengthFilter)
			addTextChangedListener { text: Editable? ->
				userEditViewModel.onChangeNickname(text.toString())
			}
		}
	}

	private fun setFocus() {
		binding.nickNameEt.requestFocus()
		openKeyboard(binding.nickNameEt)
	}

	private fun openKeyboard(view: View) {
		Handler(Looper.getMainLooper()).postDelayed({
			imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
		}, KEYBOARD_DELAY_TIME)
	}

	private fun closeKeyboard(windowToken: IBinder) {
		imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
	}

	private fun startUserProfileEdit() {
		closeKeyboard(binding.nickNameEt.windowToken)
		permissionsLauncher.launch(galleryPermissions)
	}

	private fun moveToImageCrop() {
		val intent = Intent(this, ImageCropActivity::class.java)
		cropActivityResultLauncher.launch(intent)
	}

	private fun showUserProfileEditDialog() {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_USER_PROFILE_EDIT)
		if (existingFragment != null) return

		val dialog = UserProfileEditDialog(
			onSelectDefaultImage = { userEditViewModel.onSelectDefaultProfileImage() },
			onSelectGallery = { userEditViewModel.onSelectGallery() }
		)
		dialog.show(supportFragmentManager, DIALOG_TAG_USER_PROFILE_EDIT)
	}

	private val cropActivityResultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == RESULT_OK) {
				val intent = result.data
				val bitmapByteArray =
					intent?.getByteArrayExtra(EXTRA_CROPPED_PROFILE_BYTE_ARRAY)
				bitmapByteArray?.let { userEditViewModel.onChangeUserProfile(it) }
			}
		}

	private fun handleUiEvent(event: UserEditUiEvent) {
		when (event) {
			UserEditUiEvent.MoveToBack -> finish()
			UserEditUiEvent.MoveToGallery -> startUserProfileEdit()
			is UserEditUiEvent.ErrorEvent -> binding.root.showSnackBar(event.stringId)
			is UserEditUiEvent.UnknownErrorEvent -> binding.root.showSnackBar(event.message)
			UserEditUiEvent.ShowUserProfileEditDialog -> showUserProfileEditDialog()
		}
	}

	companion object {
		private const val KEYBOARD_DELAY_TIME = 200L
		private const val NAME_CHECK_REGULAR_EXPRESSION =
			"^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55\\uFF1A]+$"
		private const val MAX_NICKNAME_LENGTH = 20
		private const val SCHEME_PACKAGE = "package"
		private const val DIALOG_TAG_USER_PROFILE_EDIT = "DIALOG_TAG_USER_PROFILE_EDIT"

	}

}