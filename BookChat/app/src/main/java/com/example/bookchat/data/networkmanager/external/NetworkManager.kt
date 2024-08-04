package com.example.bookchat.data.networkmanager.external

import com.example.bookchat.data.networkmanager.external.model.NetworkState
import kotlinx.coroutines.flow.StateFlow

interface NetworkManager {
	fun getStateFlow(): StateFlow<NetworkState>
}