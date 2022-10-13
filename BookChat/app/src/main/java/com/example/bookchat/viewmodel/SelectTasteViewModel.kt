package com.example.bookchat.viewmodel

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.UserSignUpDto
import com.example.bookchat.repository.UserRepository
import com.example.bookchat.response.NetworkIsNotConnectedException
import com.example.bookchat.response.ResponseBodyEmptyException
import com.example.bookchat.response.TokenExpiredException
import com.example.bookchat.response.UnauthorizedOrBlockedUserException
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.ReadingTaste
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class SelectTasteViewModel(private val userRepository : UserRepository) :ViewModel() {
    lateinit var goMainActivityCallBack :() -> Unit

    private val selectedTastes = ArrayList<ReadingTaste>()
    private val _isTastesEmpty = MutableLiveData<Boolean>(true)
    private var recursiveChecker = false //임시 (구조 개선 필요)

    var _signUpDto = MutableLiveData<UserSignUpDto>()

    val isTastesEmpty : LiveData<Boolean>
        get() = _isTastesEmpty

    val signUpDto : LiveData<UserSignUpDto>
        get() = _signUpDto

    fun signUp() = viewModelScope.launch {
        Log.d(TAG, "SelectTasteViewModel: signUp() - called : userDto : ${_signUpDto.value}")
//        _signUpDto.value?.readingTastes = selectedTastes
        runCatching{ userRepository.signUp(_signUpDto.value!!) } //!! 표시 임시
            .onSuccess { signIn() }
            .onFailure { Log.d(TAG, "SelectTasteViewModel: signUp() - onFailure $it") }
    }

    private fun signIn() = viewModelScope.launch {
        Log.d(TAG, "SelectTasteViewModel: signIn() - called")
        runCatching { userRepository.signIn() }
            .onSuccess { requestUserInfo() }
            .onFailure {
                Log.d(TAG, "SelectTasteViewModel: signIn() - onFailure ")
                when(it){
                    is UnauthorizedOrBlockedUserException -> Toast.makeText(App.instance.applicationContext,"차단된 사용자 입니다.\n24시간 후에 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    else -> Log.d(TAG, "LoginViewModel: bookchatLogin() - elseException : $it")
                }
            }
    }

    private fun requestUserInfo() = viewModelScope.launch {
        Log.d(TAG, "LoginViewModel: requestUserInfo() - called")
        runCatching{ userRepository.getUserProfile() }
            .onSuccess { goMainActivityCallBack() }
            .onFailure { failHandler(it) }
    }

    private fun requestTokenRenewal() = viewModelScope.launch {
        Log.d(TAG, "LoginViewModel: requestTokenRenewal() - called")
        runCatching{ userRepository.requestTokenRenewal() }
            .onSuccess { if (recursiveChecker == false) requestUserInfo(); recursiveChecker = true }
    }

    //Status Code별 Exception handle
    private fun failHandler(exception: Throwable) {
        when(exception){
            is TokenExpiredException -> requestTokenRenewal()
            is UnauthorizedOrBlockedUserException -> {
                Log.d(TAG, "LoginViewModel: failHandler() - unauthorizedOrBlockedUserException")
            }
            is ResponseBodyEmptyException -> {
                Log.d(TAG, "LoginViewModel: failHandler() - ResponseBodyEmptyException")
            }
            is NetworkIsNotConnectedException -> {
                Log.d(TAG, "LoginViewModel: failHandler() - NetworkIsNotConnectedException")
            }
            else -> {
                Log.d(TAG, "LoginViewModel: failHandler() - Exception : $exception")}
        }
    }


    fun clickTaste(view: View) {
        val chip = view as Chip
        val taste = chip.text.toString()
        if (selectedTastes.contains(tasteConverter[taste])) selectedTastes.remove(tasteConverter[taste])
        else selectedTastes.add(tasteConverter[taste]!!)
        emptyCheck()
        Log.d(TAG, "SelectTasteViewModel: clickTaste() - selectedTastes : $selectedTastes")
    }

    private fun emptyCheck() {
        if (selectedTastes.isEmpty()){
            _isTastesEmpty.value = true
            return
        }
        _isTastesEmpty.value = false
    }

    val tasteConverter = hashMapOf<String,ReadingTaste>(
        "경제" to ReadingTaste.ECONOMY , "철학" to ReadingTaste.PHILOSOPHY , "역사" to ReadingTaste.HISTORY ,
        "여행" to ReadingTaste.TRAVEL , "건강" to ReadingTaste.HEALTH , "취미" to ReadingTaste.HOBBY ,
        "인문" to ReadingTaste.HUMANITIES , "소설" to ReadingTaste.NOVEL, "예술" to ReadingTaste.ART ,
        "디자인" to ReadingTaste.DESIGN , "개발" to ReadingTaste.DEVELOPMENT , "과학" to ReadingTaste.SCIENCE ,
        "잡지" to ReadingTaste.MAGAZINE , "종교" to ReadingTaste.RELIGION , "인물" to ReadingTaste.CHARACTER
    )

}