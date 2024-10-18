package com.kova700.bookchat.feature.main

import com.kova700.bookchat.core.network_manager.external.model.NetworkState

data class MainUiState(
	val networkState: NetworkState,
) {

	companion object {
		val DEFAULT = MainUiState(
			networkState = NetworkState.DISCONNECTED,
		)
	}
}
