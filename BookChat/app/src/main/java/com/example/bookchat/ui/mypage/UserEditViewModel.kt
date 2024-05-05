package com.example.bookchat.ui.mypage

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.network.model.response.NickNameDuplicateException
import com.example.bookchat.domain.model.NameCheckStatus
import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class UserEditViewModel @Inject constructor(
	private val clientRepository: ClientRepository
) : ViewModel() {
	val cachedClient = MutableStateFlow<User>(User.Default)

	private val _eventFlow = MutableSharedFlow<UserEditUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private var isNotShortFlag = false
	private var isNotDuplicateFlag = false
	val nameCheckStatus = MutableStateFlow<NameCheckStatus>(NameCheckStatus.Default)
	val newProfileImage = MutableStateFlow(byteArrayOf())
	val newNickname = MutableStateFlow("")

	init {
		getUserInfo()
	}

	val editTextWatcher = object : TextWatcher {
		override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
		override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
		override fun afterTextChanged(s: Editable) {
			renewLengthStatus(s.toString())
			isNotDuplicateFlag = false
		}
	}
	val specialCharFilter = arrayOf(InputFilter { source, _, _, _, _, _ ->
		val regex = "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55\\uFF1A]+$"
		val pattern = Pattern.compile(regex)
		if (pattern.matcher(source).matches()) return@InputFilter source
		nameCheckStatus.value = NameCheckStatus.IsSpecialCharInText
		return@InputFilter ""
	})

	fun clickDupCheckBtn() {
		if (isNotDuplicateFlag || !haveNewNickName()) return
		if (isNotShortFlag) {
			requestNameDuplicateCheck(newNickname.value.trim())
			return
		}
		renewLengthStatus("")
	}

	fun renewLengthStatus(inputedText: String) {
		if (inputedText.isShort()) {
			nameCheckStatus.value = NameCheckStatus.IsShort
			isNotShortFlag = false
			return
		}
		nameCheckStatus.value = NameCheckStatus.Default
		isNotShortFlag = true
	}

	private fun haveNewNickName() =
		newNickname.value.isNotBlank() &&
						(newNickname.value.trim() != cachedClient.value.nickname.trim())

	private fun haveNewProfile() =
		newProfileImage.value.isNotEmpty()

	fun saveChange() {
		when {
			haveNewNickName() && !haveNewProfile() -> {
				if (nameCheckStatus.value != NameCheckStatus.IsPerfect) {
					makeToast(R.string.my_page_profile_need_name_dup_check)
					return
				}
				//기존 User객체에 새로운 NickName담아서 변경 API호출
				requestReviseUserProfile()
			}

			!haveNewNickName() && haveNewProfile() -> {
				//기존 User객체에 이미지만 담아서 변경 API호출
				requestReviseUserProfile()
			}

			haveNewNickName() && haveNewProfile() -> {
				if (nameCheckStatus.value != NameCheckStatus.IsPerfect) {
					makeToast(R.string.my_page_profile_need_name_dup_check)
					return
				}
				//기존 User객체에 닉네임 , 이미지 담아서 변경 API호출
				requestReviseUserProfile()
			}

			else -> {
				startEvent(UserEditUiEvent.Finish)
			}
		}
	}

	/* 사용자 정보 수정 API 생기면 API요청 부분 변경 */
	/*이미지 ByteArray로 전달해야함*/
	private fun requestReviseUserProfile() = viewModelScope.launch {
		runCatching { }
			.onSuccess { makeToast(R.string.my_page_profile_edit_success) }
			.onFailure { makeToast(R.string.my_page_profile_edit_fail) }
			.also { startEvent(UserEditUiEvent.Finish) }
	}

	private fun requestNameDuplicateCheck(nickName: String) =
		viewModelScope.launch {
			runCatching { clientRepository.checkForDuplicateUserName(nickName) }
				.onSuccess {
					nameCheckStatus.value = NameCheckStatus.IsPerfect
					isNotDuplicateFlag = true
				}
				.onFailure { failHandler(it) }
		}

	private fun getUserInfo() = viewModelScope.launch {
		runCatching { clientRepository.getClientProfile() }
			.onSuccess { user -> cachedClient.update { user } }
	}

	fun clickBackBtn() {
		saveChange()
	}

	private fun startEvent(event: UserEditUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private fun String.isShort() = this.length < 2

	sealed class UserEditUiEvent {
		object Finish : UserEditUiEvent()
		object UnknownError : UserEditUiEvent()
	}

	private fun failHandler(exception: Throwable) {
		when (exception) {
			is NickNameDuplicateException -> {
				nameCheckStatus.value = NameCheckStatus.IsDuplicate
				isNotDuplicateFlag = false
			}

			else -> startEvent(UserEditUiEvent.UnknownError)
		}
	}
}