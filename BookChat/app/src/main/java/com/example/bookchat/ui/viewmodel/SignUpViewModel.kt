package com.example.bookchat.ui.viewmodel

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.response.NickNameDuplicateException
import com.example.bookchat.data.repository.UserRepository
import com.example.bookchat.utils.NameCheckStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private var userRepository: UserRepository
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<SignUpEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var isNotNameShortFlag = false
    private var isNotNameDuplicateFlag = false
    val inputtedNickname = MutableStateFlow("")
    val userProfileByteArray = MutableStateFlow<ByteArray?>(null)

    private val _nameCheckStatus = MutableStateFlow<NameCheckStatus>(NameCheckStatus.Default)
    val nameCheckStatus = _nameCheckStatus.asStateFlow()

    val editTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            renewNameCheckStatus(s.toString())
            isNotNameDuplicateFlag = false
        }
    }

    fun renewNameCheckStatus(inputtedText: String) {
        if (inputtedText.length < 2) {
            _nameCheckStatus.value = NameCheckStatus.IsShort
            isNotNameShortFlag = false
            return
        }
        _nameCheckStatus.value = NameCheckStatus.Default
        isNotNameShortFlag = true
    }

    val specialCharFilter = arrayOf(InputFilter { source, _, _, _, _, _ ->
        val regex =
            "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55\\uFF1A]+$"
        val pattern = Pattern.compile(regex)
        if (pattern.matcher(source).matches()) return@InputFilter source
        _nameCheckStatus.value = NameCheckStatus.IsSpecialCharInText
        return@InputFilter ""
    }, InputFilter.LengthFilter(20))

    fun clickStartBtn() = viewModelScope.launch {
        if (isNotNameShortFlag) {
            isNotNameDuplicate(isNotNameDuplicateFlag)
            return@launch
        }
        renewNameCheckStatus("")
    }

    private suspend fun isNotNameDuplicate(isNotNameDuplicateFlag: Boolean) {
        if (isNotNameDuplicateFlag) {
            startEvent(
                SignUpEvent.MoveToSelectTaste(inputtedNickname.value, userProfileByteArray.value)
            )
            return
        }
        requestNameDuplicateCheck(inputtedNickname.value.trim())
    }

    private suspend fun requestNameDuplicateCheck(nickName: String) {
        runCatching { userRepository.requestNameDuplicateCheck(nickName) }
            .onSuccess { nameDuplicateCheckSuccessCallBack() }
            .onFailure { failHandler(it) }
    }

    private fun nameDuplicateCheckSuccessCallBack() {
        _nameCheckStatus.value = NameCheckStatus.IsPerfect
        isNotNameDuplicateFlag = true
    }

    private fun nameDuplicateCallBack(){
        _nameCheckStatus.value = NameCheckStatus.IsDuplicate
        isNotNameDuplicateFlag = false
    }

    fun openGallery() {
        startEvent(SignUpEvent.PermissionCheck)
    }

    fun clickBackBtn() {
        startEvent(SignUpEvent.MoveToBack)
    }

    fun clickClearNickNameBtn() {
        inputtedNickname.value = ""
    }

    private fun startEvent(event: SignUpEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class SignUpEvent {
        data class MoveToSelectTaste(
            val userNickname: String,
            val userProfilByteArray: ByteArray?
        ) : SignUpEvent()

        object PermissionCheck : SignUpEvent()
        object MoveToBack : SignUpEvent()
        object UnknownError : SignUpEvent()
    }

    private fun failHandler(exception: Throwable) {
        when (exception) {
            is NickNameDuplicateException -> nameDuplicateCallBack()
            else -> startEvent(SignUpEvent.UnknownError)
        }
    }
}