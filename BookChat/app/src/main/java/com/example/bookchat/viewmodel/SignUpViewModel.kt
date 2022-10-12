package com.example.bookchat.viewmodel

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.repository.DupCheckRepository
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.NameCheckStatus
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.regex.Pattern
import kotlin.coroutines.resume

class SignUpViewModel(private var dupCheckRepository : DupCheckRepository) :ViewModel() {

    lateinit var goSelectTasteActivity : suspend () -> Unit
    private var _isNotNameShort = MutableLiveData<Boolean?>(false)
    private var _isNotNameDuplicate = MutableLiveData<Boolean?>(false)
    private var _nameCheckStatus = MutableLiveData<NameCheckStatus>(NameCheckStatus.Default)

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
            if (_isNotNameShort.value == true){
                if (_isNotNameDuplicate.value == true){
                    goSelectTasteActivity()
                    return@launch
                }
                duplicateCheck()
                return@launch
            }
            lengthCheck("")
        }
    }

    //서버한테 중복 검사 요청
    private suspend fun duplicateCheck() {
        _isNotNameDuplicate.value = suspendCancellableCoroutine<Boolean> { continuation ->
            dupCheckRepository.duplicateCheck { dupCheckResult ->
                when(dupCheckResult){
                    true -> {
                        _nameCheckStatus.value = NameCheckStatus.IsPerfect
                        continuation.resume(true)
                    }
                    false ->{
                        _nameCheckStatus.value = NameCheckStatus.IsDuplicate
                        continuation.resume(false)
                    }
                }
                Log.d(TAG, "SignUpViewModel: duplicateCheck() - 검사 완료 - _nameCheckStatus.value : ${_nameCheckStatus.value}")
                Log.d(TAG, "SignUpViewModel: duplicateCheck() - 검사 완료 - _isNotNameDuplicate.value : ${_isNotNameDuplicate.value}")
            }
        }
    }

}