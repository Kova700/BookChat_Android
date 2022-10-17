package com.example.bookchat.viewmodel

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.UserSignUpDto
import com.example.bookchat.repository.UserRepository
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.NameCheckStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.regex.Pattern

class SignUpViewModel(private var userRepository : UserRepository) :ViewModel() {

    lateinit var goSelectTasteActivity : suspend () -> Unit
    private var isNotNameShort = false
    private var isNotNameDuplicate = false
    var _signUpDto = MutableStateFlow<UserSignUpDto>(UserSignUpDto( defaultProfileImageType = Random().nextInt(5) + 1) )

    private var _nameCheckStatus = MutableStateFlow<NameCheckStatus>(NameCheckStatus.Default)
    val nameCheckStatus = _nameCheckStatus.asStateFlow()

    var _userProfilByteArray = MutableStateFlow<ByteArray>(byteArrayOf())
    val userProfilByteArray = _userProfilByteArray.asStateFlow()

    val editTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            lengthCheck(s.toString())
            isNotNameDuplicate = false
            Log.d(TAG, "SignUpViewModel: afterTextChanged() - _signUpDto : ${_signUpDto.value}")
        }
    }

    fun lengthCheck(inputedText :String){
        if (inputedText.length < 2){
            _nameCheckStatus.value = NameCheckStatus.IsShort
            isNotNameShort = false
            return
        }
        _nameCheckStatus.value = NameCheckStatus.Default
        isNotNameShort = true
    }

    val specialCharFilter = arrayOf(InputFilter{ source, _, _, _, _, _ ->
        val regex = "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55\\uFF1A]+$"
        val pattern = Pattern.compile(regex)
        if (pattern.matcher(source).matches()){
            return@InputFilter source //통과되는 문자일 때 (입력값 그대로 출력)
        }
        _nameCheckStatus.value = NameCheckStatus.IsSpecialCharInText
        "" //통과되지 않는 문자일 때 대체 문자
    })

    fun clickStartBtn() {
        viewModelScope.launch {
            if (isNotNameShort){
                if (isNotNameDuplicate){
                    goSelectTasteActivity()
                    return@launch
                }
                requestNameDuplicateCheck(_signUpDto.value.nickname)
                return@launch
            }
            lengthCheck("")
        }
    }

    private suspend fun requestNameDuplicateCheck(nickName :String) {
        Log.d(TAG, "SignUpViewModel: requestNameDuplicateCheck() - called")
        runCatching { userRepository.requestNameDuplicateCheck(nickName) }
            .onSuccess {
                _nameCheckStatus.value = NameCheckStatus.IsPerfect
                isNotNameDuplicate = true
            }
            .onFailure {
                Log.d(TAG, "SignUpViewModel: requestNameDuplicateCheck() Exception : $it")
                _nameCheckStatus.value = NameCheckStatus.IsDuplicate
                isNotNameDuplicate = false
            }
    }

}