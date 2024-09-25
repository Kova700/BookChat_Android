package com.kova700.bookchat.feature.mypage.useredit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
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
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.core.navigation.ImageCropNavigator
import com.kova700.bookchat.core.navigation.ImageCropNavigator.Companion.EXTRA_CROPPED_IMAGE_CACHE_URI
import com.kova700.bookchat.core.navigation.ImageCropNavigator.ImageCropPurpose
import com.kova700.bookchat.feature.mypage.databinding.ActivityUserEditBinding
import com.kova700.bookchat.feature.mypage.useredit.dialog.ProfileEditDialog
import com.kova700.bookchat.util.image.bitmap.getImageBitmap
import com.kova700.bookchat.util.image.image.deleteImageCache
import com.kova700.bookchat.util.image.image.loadChangedUserProfile
import com.kova700.bookchat.util.permissions.galleryPermissions
import com.kova700.bookchat.util.permissions.getPermissionsLauncher
import com.kova700.bookchat.util.snackbar.showSnackBar
import com.kova700.bookchat.util.toast.makeToast
import com.kova700.bookchat.util.user.namecheck.MAX_NICKNAME_LENGTH
import com.kova700.bookchat.util.user.namecheck.NAME_CHECK_REGULAR_EXPRESSION
import com.kova700.bookchat.util.user.namecheck.getNameCheckResultBackgroundResId
import com.kova700.bookchat.util.user.namecheck.getNameCheckResultHexInt
import com.kova700.bookchat.util.user.namecheck.getNameCheckResultText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class UserEditActivity : AppCompatActivity() {

	private lateinit var binding: ActivityUserEditBinding
	private val userEditViewModel: UserEditViewModel by viewModels()

	private val imm by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

	@Inject
	lateinit var imageCropNavigator: ImageCropNavigator

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
		binding = ActivityUserEditBinding.inflate(layoutInflater)
		setContentView(binding.root)
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
		setNameCheckResultTextViewState(state)
		setNameCheckResultLayoutState(state)
		with(binding) {
			textClearBtn.visibility = if (state.newNickname.isEmpty()) View.GONE else View.VISIBLE
			textLengthTv.text = getString(R.string.user_nickname_length, state.newNickname.length)
		}
	}

	private fun setNameCheckResultLayoutState(state: UserEditUiState) {
		binding.nickNameLayout.setBackgroundResource(
			state.nicknameCheckState.getNameCheckResultBackgroundResId()
		)
	}

	private fun setNameCheckResultTextViewState(state: UserEditUiState) {
		with(binding.checkResultTv) {
			setTextColor(state.nicknameCheckState.getNameCheckResultHexInt(context))
			text = state.nicknameCheckState.getNameCheckResultText(context)
		}
	}

	private fun setLoadingViewState(state: UserEditUiState) {
		binding.progressBar.visibility =
			if (state.uiState == UserEditUiState.UiState.LOADING) View.VISIBLE else View.GONE
	}

	private fun setProfileImageViewState(state: UserEditUiState) {
		binding.userProfileIv.loadChangedUserProfile(
			imageUrl = state.client.profileImageUrl,
			userDefaultProfileType = state.client.defaultProfileImageType,
			bitmap = state.clientNewImage
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
		with(binding) {
			cameraBtn.setOnClickListener { userEditViewModel.onClickCameraBtn() }
			nicknameSubmitBtn.setOnClickListener { userEditViewModel.onClickSubmitBtn() }
			textClearBtn.setOnClickListener { userEditViewModel.onClickClearNickNameBtn() }
			backBtn.setOnClickListener { userEditViewModel.onClickBackBtn() }
		}
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

	private fun startUserProfileEdit() {
		closeKeyboard()
		permissionsLauncher.launch(galleryPermissions)
	}

	private fun moveToImageCrop() {
		imageCropNavigator.navigate(
			currentActivity = this,
			imageCropPurpose = ImageCropPurpose.USER_PROFILE,
			resultLauncher = cropActivityResultLauncher,
		)
	}

	private fun showProfileEditDialog() {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_PROFILE_EDIT)
		if (existingFragment != null) return

		val dialog = ProfileEditDialog(
			onSelectDefaultImage = { userEditViewModel.onSelectDefaultProfileImage() },
			onSelectGallery = { userEditViewModel.onSelectGallery() }
		)
		dialog.show(supportFragmentManager, DIALOG_TAG_PROFILE_EDIT)
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
		val croppedImageBitmap = uri.getImageBitmap(this@UserEditActivity) ?: return@launch
		userEditViewModel.onChangeUserProfile(croppedImageBitmap)
		deleteImageCache(uri)
	}

	private fun closeKeyboard() {
		imm.hideSoftInputFromWindow(binding.nickNameEt.windowToken, 0)
	}

	private fun handleUiEvent(event: UserEditUiEvent) {
		when (event) {
			UserEditUiEvent.MoveToBack -> finish()
			UserEditUiEvent.MoveToGallery -> startUserProfileEdit()
			UserEditUiEvent.ShowProfileEditDialog -> showProfileEditDialog()
			UserEditUiEvent.CloseKeyboard -> closeKeyboard()
			is UserEditUiEvent.ShowSnackBar -> binding.root.showSnackBar(event.stringId)
		}
	}

	companion object {
		private const val KEYBOARD_DELAY_TIME = 200L
		private const val SCHEME_PACKAGE = "package"
		private const val DIALOG_TAG_PROFILE_EDIT = "DIALOG_TAG_USER_PROFILE_EDIT"

	}

}