package com.example.bookchat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.domain.repository.UserRepository
import com.example.bookchat.utils.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
	private val userRepository: UserRepository
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<SplashEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	init {
		viewModelScope.launch {
			runCatching { DataStoreManager.getBookChatToken() }
				.onSuccess { requestUserInfo() }
				.onFailure { startEvent(SplashEvent.MoveToLogin) }
		}
	}

	private fun requestUserInfo() = viewModelScope.launch {
		runCatching { userRepository.getUserProfile() }
			.onSuccess { getUserInfoSuccessCallback() }
			.onFailure { startEvent(SplashEvent.MoveToLogin) }
	}

	private fun getUserInfoSuccessCallback() {
		if (isFcmTokenRefreshed()) {
			//지금 로컬DB에 저장된 FCM토큰을 서버로 보내는 API를 호출해서 서버 DB업데이트
			return
		}
		//GoogleSDK로부터 FCM토큰을 가져와서 로컬DB에 저장된 FCM토큰과 같은 토큰인지 확인한다.
		//  같다면
		//      토큰이 변경되지 않았음으로, 다시 일반적인 로직으로 돌아간다. (토큰이 유효하면 MainActivity등..)
		//  다르다면
		//      새로 받은 토큰을 로컬 DB에 저장하고, 새로 저장한 FCM토큰을 서버로 보내 FCM토큰을 업데이트한다.
		//      토큰 갱신 Flag는 다시 False로 변경
		startEvent(SplashEvent.MoveToMain)
	}

	//여기서 FirebaseMessaging.getInstance().token으로 진짜 내가 가지고 있는 토큰이 유효한 토큰인지 확인
//    private fun a(){
//        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
//                return@OnCompleteListener
//            }
//
//            // Get new FCM registration token
//            val token = task.result
//
//            // Log and toast
//            val msg = getString(R.string.msg_token_fmt, token)
//            Log.d(TAG, msg)
//            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//        })
//    }

	//onNewToken에서 확인하는거랑
	//GoogleSDK에서 확인하는거랑 사실상 로직이 같음 (함수명 분리해야함)

	private fun isFcmTokenRefreshed(): Boolean {
		//로컬 DB에서 토큰 갱신 Flag가 True인지 False인지 가져와서 반환
		return false
	}

	private fun startEvent(event: SplashEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	sealed class SplashEvent {
		object MoveToMain : SplashEvent()
		object MoveToLogin : SplashEvent()
	}
}