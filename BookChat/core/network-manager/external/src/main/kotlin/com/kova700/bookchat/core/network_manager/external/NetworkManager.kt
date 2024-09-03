package com.kova700.bookchat.core.network_manager.external

import com.kova700.bookchat.core.network_manager.external.model.NetworkState
import kotlinx.coroutines.flow.StateFlow

interface NetworkManager {
	fun getStateFlow(): StateFlow<NetworkState>
}