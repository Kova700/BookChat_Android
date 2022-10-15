package com.example.bookchat.viewmodel

import android.graphics.Bitmap
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.*
import com.example.bookchat.data.UserSignUpDto
import com.example.bookchat.repository.UserRepository
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.NameCheckStatus
import kotlinx.coroutines.launch
import java.util.*
import java.util.regex.Pattern

class SignUpViewModel(private var userRepository : UserRepository) :ViewModel() {

    lateinit var goSelectTasteActivity : suspend () -> Unit
    private var _isNotNameShort = MutableLiveData<Boolean?>(false)
    private var _isNotNameDuplicate = MutableLiveData<Boolean?>(false)
    private var _nameCheckStatus = MutableLiveData<NameCheckStatus>(NameCheckStatus.Default)
    val randomInteger = Random().nextInt(5) + 1
    var _signUpDto = MutableLiveData<UserSignUpDto>(UserSignUpDto( defaultProfileImageType = randomInteger) )
    var _userProfilBitmap = MutableLiveData<Bitmap>()

    val isNotNameShort : LiveData<Boolean?>
        get() = _isNotNameShort
    val isNotNameDuplicate : LiveData<Boolean?>
        get() = _isNotNameDuplicate
    val nameCheckStatus : LiveData<NameCheckStatus>
        get() = _nameCheckStatus

    val editTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            //글자가 수정될 때마다 검사 기록 초기화
            lengthCheck(s.toString())
            _isNotNameDuplicate.value = false
            Log.d(TAG, "SignUpViewModel: afterTextChanged() - _signUpDto : ${_signUpDto.value}")
        }
    }

    fun lengthCheck(inputedText :String){
        if (inputedText.length < 2){
            _nameCheckStatus.value = NameCheckStatus.IsShort
            _isNotNameShort.value = false
            return
        }
        _nameCheckStatus.value = NameCheckStatus.Default
        _isNotNameShort.value = true
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
            goSelectTasteActivity()

//            if (_isNotNameShort.value == true){
//                if (_isNotNameDuplicate.value == true){
//                    goSelectTasteActivity()
//                    return@launch
//                }
//                _signUpDto.value?.nickname?.let { requestNameDuplicateCheck(it) }
//                return@launch
//            }
//            lengthCheck("")
        }
    }

    private suspend fun requestNameDuplicateCheck(nickName :String) {
        Log.d(TAG, "SignUpViewModel: requestNameDuplicateCheck() - called")
        runCatching { userRepository.requestNameDuplicateCheck(nickName) }
            .onSuccess {
                _nameCheckStatus.value = NameCheckStatus.IsPerfect
                _isNotNameDuplicate.value = true
            }
            .onFailure {
                Log.d(TAG, "SignUpViewModel: requestNameDuplicateCheck() Exception : $it")
                _nameCheckStatus.value = NameCheckStatus.IsDuplicate
                _isNotNameDuplicate.value = false
            }
    }

}