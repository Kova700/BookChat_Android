package com.example.bookchat.ui.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSettingViewModel @Inject constructor(
    private val userRepository: UserRepository
) :ViewModel() {

    private val _eventFlow = MutableSharedFlow<AccountSettingUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun clickSignOutBtn(){
        //서버와 협의
        //1번 : 북챗 토큰 삭제, 로컬에서 알림 받지 않음 설정 (FCM알림은 넘어오지만 표시하지 않음)
        //2번 : 서버에 FCM토큰, 북챗 토큰삭제 (== 서비스(광고) 알림을 날리지 못함) (API 필요)
    }

    fun clickWithdrawBtn(){
        requestWithdraw()
    }

    private fun requestWithdraw() = viewModelScope.launch {
        runCatching { userRepository.withdraw() }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"회원이 탈퇴되었습니다(임시)", Toast.LENGTH_SHORT).show()
                startEvent(AccountSettingUiEvent.MoveToLoginPage)
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"회원이 탈퇴 실패 (임시)", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startEvent (event : AccountSettingUiEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class AccountSettingUiEvent{
        object MoveToLoginPage : AccountSettingUiEvent()
    }
}