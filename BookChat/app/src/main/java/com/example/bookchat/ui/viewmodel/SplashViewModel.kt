package com.example.bookchat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.domain.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
	private val clientRepository: ClientRepository
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<SplashEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	init {
		requestUserInfo()
	}

	private fun requestUserInfo() = viewModelScope.launch {
		runCatching { clientRepository.getClientProfile() }
			.onSuccess { startEvent(SplashEvent.MoveToMain) }
			.onFailure { startEvent(SplashEvent.MoveToLogin) }
	}

	private fun startEvent(event: SplashEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	sealed class SplashEvent {
		object MoveToMain : SplashEvent()
		object MoveToLogin : SplashEvent()
	}
}