package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val userRepository: UserRepository
    ) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<MyPageEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun requestWithdraw() = viewModelScope.launch {
        runCatching { userRepository.withdraw() }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"회원이 탈퇴되었습니다(임시)", Toast.LENGTH_SHORT).show()
                startEvent(MyPageEvent.MoveToLoginPage)
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"회원이 탈퇴 실패 (임시)", Toast.LENGTH_SHORT).show()
            }
    }

    fun clickUserEditBtn(){
        startEvent(MyPageEvent.MoveToUserEditPage)
    }
    fun clickWishBtn(){
        startEvent(MyPageEvent.MoveToWish)
    }
    fun clickNoticeBtn(){
        startEvent(MyPageEvent.MoveToNotice)
    }
    fun clickAccountSetBtn(){
        startEvent(MyPageEvent.MoveToAccountSetting)
    }
    fun clickAppSetBtn(){
        startEvent(MyPageEvent.MoveToAppSetting)
    }

    private fun startEvent (event : MyPageEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class MyPageEvent{
        object MoveToLoginPage :MyPageEvent()
        object MoveToUserEditPage :MyPageEvent()
        object MoveToWish :MyPageEvent()
        object MoveToNotice :MyPageEvent()
        object MoveToAccountSetting :MyPageEvent()
        object MoveToAppSetting :MyPageEvent()
    }
}