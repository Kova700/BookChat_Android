package com.kova700.bookchat.feature.signup.signup

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
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.core.navigation.ImageCropNavigator
import com.kova700.bookchat.core.navigation.ImageCropNavigator.Companion.EXTRA_CROPPED_IMAGE_CACHE_URI
import com.kova700.bookchat.core.navigation.ImageCropNavigator.ImageCropPurpose
import com.kova700.bookchat.feature.signup.databinding.ActivitySignUpBinding
import com.kova700.bookchat.feature.signup.selecttaste.SelectTasteActivity
import com.kova700.bookchat.feature.signup.selecttaste.SelectTasteActivity.Companion.EXTRA_SIGNUP_USER_NICKNAME
import com.kova700.bookchat.feature.signup.selecttaste.SelectTasteActivity.Companion.EXTRA_USER_PROFILE_URI
import com.kova700.bookchat.util.image.image.loadUserProfile
import com.kova700.bookchat.util.permissions.galleryPermissions
import com.kova700.bookchat.util.permissions.getPermissionsLauncher
import com.kova700.bookchat.util.snackbar.showSnackBar
import com.kova700.bookchat.util.toast.makeToast
import com.kova700.bookchat.util.user.namecheck.MAX_NICKNAME_LENGTH
import com.kova700.bookchat.util.user.namecheck.NAME_CHECK_REGULAR_EXPRESSION
import com.kova700.bookchat.util.user.namecheck.NicknameCheckState
import com.kova700.bookchat.util.user.namecheck.getNameCheckResultBackgroundResId
import com.kova700.bookchat.util.user.namecheck.getNameCheckResultHexInt
import com.kova700.bookchat.util.user.namecheck.getNameCheckResultText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

	private lateinit var binding: ActivitySignUpBinding
	private val signUpViewModel: SignUpViewModel by viewModels()
	private val imm by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

	@Inject
	lateinit var imageCropNavigator: ImageCropNavigator

	private val permissionsLauncher = this.getPermissionsLauncher(
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
		binding = ActivitySignUpBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setFocus()
		observeUiState()
		observeUiEvent()
		initViewState()
	}

	private fun observeUiState() = lifecycleScope.launch {
		signUpViewModel.uiState.collect { state ->
			setViewState(state)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		signUpViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		initNickNameEditText()
		with(binding) {
			backBtn.setOnClickListener { signUpViewModel.onClickBackBtn() }
			startBookchatBtn.setOnClickListener { signUpViewModel.onClickStartBtn() }
			textClearBtn.setOnClickListener { signUpViewModel.onClickClearNickNameBtn() }
			cameraBtn.setOnClickListener { signUpViewModel.onClickCameraBtn() }
		}
	}

	private fun setViewState(state: SignUpState) {
		setNickNameEditTextState(state)
		setSubmitBtnState(state)
		setUserProfileImage(state)
		setNameCheckResultTextViewState(state)
		setNameCheckResultLayoutState(state)
		with(binding) {
			textLengthTv.text = getString(R.string.sign_up_user_nickname_length, state.nickname.length)
			textClearBtn.visibility = if (state.nickname.isBlank()) View.INVISIBLE else View.VISIBLE
			progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
		}
	}

	private fun setNameCheckResultLayoutState(state: SignUpState) {
		binding.nickNameLayout.setBackgroundResource(
			state.nicknameCheckState.getNameCheckResultBackgroundResId()
		)
	}

	private fun setNameCheckResultTextViewState(state: SignUpState) {
		with(binding.checkResultTv) {
			setTextColor(state.nicknameCheckState.getNameCheckResultHexInt(context))
			text = state.nicknameCheckState.getNameCheckResultText(context)
		}
	}

	private fun setUserProfileImage(state: SignUpState) {
		binding.userProfileIv.loadUserProfile(imageUrl = state.clientNewImageUri)
	}

	private fun setNickNameEditTextState(state: SignUpState) {
		with(binding.nicknameEt) {
			if (text.toString() != state.nickname) {
				setText(state.nickname)
				setSelection(state.nickname.length)
			}
		}
	}

	private fun setSubmitBtnState(state: SignUpState) {
		with(binding.startBookchatBtn) {
			setText(
				if (state.nicknameCheckState != NicknameCheckState.IsPerfect)
					R.string.sign_up_check_nickname_duplicate_btn
				else R.string.sign_up_submit_btn
			)
		}
	}

	private val specialCharFilter = InputFilter { source, _, _, _, _, _ ->
		val pattern = Pattern.compile(NAME_CHECK_REGULAR_EXPRESSION)
		if (pattern.matcher(source).matches().not()) {
			signUpViewModel.onEnteredSpecialChar()
			return@InputFilter ""
		}
		source
	}
	private val maxLengthFilter = InputFilter.LengthFilter(MAX_NICKNAME_LENGTH)

	private fun initNickNameEditText() {
		with(binding.nicknameEt) {
			filters = arrayOf(specialCharFilter, maxLengthFilter)
			addTextChangedListener { text: Editable? ->
				signUpViewModel.onChangeNickname(text.toString())
			}
		}
	}

	private fun setFocus() {
		binding.nicknameEt.requestFocus()
		openKeyboard(binding.nicknameEt)
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
		closeKeyboard(binding.nicknameEt.windowToken)
		permissionsLauncher.launch(galleryPermissions)
	}

	private fun moveToImageCrop() {
		imageCropNavigator.navigate(
			currentActivity = this,
			imageCropPurpose = ImageCropPurpose.USER_PROFILE,
			resultLauncher = cropActivityResultLauncher,
		)
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
		signUpViewModel.onChangeUserProfile(uri)
	}

	private fun moveToSelectTaste(event: SignUpEvent.MoveToSelectTaste) {
		val intent = Intent(this, SelectTasteActivity::class.java)
			.putExtra(EXTRA_SIGNUP_USER_NICKNAME, event.userNickname)
			.putExtra(EXTRA_USER_PROFILE_URI, event.userProfileUri)
		startActivity(intent)
	}

	private fun handleEvent(event: SignUpEvent) = when (event) {
		is SignUpEvent.PermissionCheck -> startUserProfileEdit()
		is SignUpEvent.MoveToBack -> finish()
		is SignUpEvent.MoveToSelectTaste -> moveToSelectTaste(event)
		is SignUpEvent.ErrorEvent -> binding.signUpLayout.showSnackBar(event.stringId)
		is SignUpEvent.UnknownErrorEvent -> binding.signUpLayout.showSnackBar(event.message)
	}

	companion object {
		const val KEYBOARD_DELAY_TIME = 200L
		private const val SCHEME_PACKAGE = "package"
	}
}