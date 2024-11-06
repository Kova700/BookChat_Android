package com.kova700.bookchat.core.network_manager.internal

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.kova700.bookchat.core.network_manager.external.NetworkManager
import com.kova700.bookchat.core.network_manager.external.model.NetworkState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class NetworkManagerImpl @Inject constructor(
	@ApplicationContext applicationContext: Context,
) : NetworkManager {

	private val connectivityManager: ConnectivityManager =
		applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

	private val networkRequest = NetworkRequest.Builder()
		.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
		.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
		.build()

	private val _networkState = MutableStateFlow<NetworkState>(NetworkState.DISCONNECTED)

	private val networkCallback = object : ConnectivityManager.NetworkCallback() {
		override fun onAvailable(network: Network) {
			_networkState.update { NetworkState.CONNECTED }
		}

		override fun onUnavailable() {
			_networkState.update { NetworkState.DISCONNECTED }
		}

		override fun onLost(network: Network) {
			_networkState.update { NetworkState.DISCONNECTED }
		}

		override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
			updateState()
		}
	}

	init {
		connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
	}

	override fun observeNetworkState(): StateFlow<NetworkState> {
		return _networkState.asStateFlow().also { updateState() }
	}

	override fun isNetworkAvailable(): Boolean {
		return _networkState.value == NetworkState.CONNECTED
	}

	private fun updateState() {
		val networkCapabilities =
			connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

		val currentState = when {
			networkCapabilities == null -> NetworkState.DISCONNECTED
			networkCapabilities.isConnected() -> NetworkState.CONNECTED
			else -> NetworkState.DISCONNECTED
		}
		_networkState.update { currentState }
	}

	private fun NetworkCapabilities.isConnected(): Boolean {
		return isWIFIConnected() || isMobileDataConnected()
	}

	private fun NetworkCapabilities.isWIFIConnected(): Boolean {
		return hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
	}

	private fun NetworkCapabilities.isMobileDataConnected(): Boolean {
		return hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
	}
}