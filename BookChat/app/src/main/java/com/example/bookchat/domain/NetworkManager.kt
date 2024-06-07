package com.example.bookchat.domain

import com.example.bookchat.domain.model.NetworkState
import kotlinx.coroutines.flow.StateFlow

interface NetworkManager {
	fun getStateFlow(): StateFlow<NetworkState>
}