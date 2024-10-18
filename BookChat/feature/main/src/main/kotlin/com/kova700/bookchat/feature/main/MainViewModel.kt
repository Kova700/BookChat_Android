package com.kova700.bookchat.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.network_manager.external.NetworkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val networkManager: NetworkManager
) : ViewModel() {

	private val _uiState = MutableStateFlow<MainUiState>(MainUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	init {
		observeNetworkState()
	}

	private fun observeNetworkState() = viewModelScope.launch {
		networkManager.observeNetworkState().collect {
			updateState { copy(networkState = it) }
		}
	}

	private inline fun updateState(block: MainUiState.() -> MainUiState) {
		_uiState.update { _uiState.value.block() }
	}
}