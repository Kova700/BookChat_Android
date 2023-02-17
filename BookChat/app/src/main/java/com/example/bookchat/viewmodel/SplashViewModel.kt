package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.repository.UserRepository
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
            runCatching { DataStoreManager.getBookchatToken() }
                .onSuccess { requestUserInfo() }
                .onFailure { startEvent(SplashEvent.MoveToLogin) }
        }
    }

    private fun requestUserInfo() = viewModelScope.launch {
        runCatching { userRepository.getUserProfile() }
            .onSuccess { startEvent(SplashEvent.MoveToMain) }
            .onFailure { startEvent(SplashEvent.MoveToLogin) }
    }

    private fun startEvent(event: SplashEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class SplashEvent{
        object MoveToMain : SplashEvent()
        object MoveToLogin : SplashEvent()
    }
}