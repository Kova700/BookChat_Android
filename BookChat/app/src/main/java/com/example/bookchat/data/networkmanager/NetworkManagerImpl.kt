package com.example.bookchat.data.networkmanager

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.bookchat.domain.NetworkManager
import com.example.bookchat.domain.model.NetworkState
import com.example.bookchat.utils.Constants.TAG
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
	}

	init {
		connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
	}

	override fun getStateFlow(): StateFlow<NetworkState> {
		updateState()
		return _networkState.asStateFlow()
	}

	private fun updateState() {
		val networkCapabilities =
			connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
		if (networkCapabilities == null){
			Log.d(TAG, "NetworkManagerImpl: updateState() - networkCapabilities == null")
			return
		}

		val currentState = when {
			networkCapabilities.isWIFIConnected()
							|| networkCapabilities.isMobileDataConnected() -> NetworkState.CONNECTED

			else -> NetworkState.DISCONNECTED
		}

		Log.d(
			TAG, "NetworkManagerImpl: updateState() - isWIFIConnected() : ${networkCapabilities.isWIFIConnected()} \n" +
							"isDataConnected() : ${networkCapabilities.isMobileDataConnected()}"
		)
		_networkState.update { currentState }
	}

	private fun NetworkCapabilities.isWIFIConnected(): Boolean {
		return hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
	}

	private fun NetworkCapabilities.isMobileDataConnected(): Boolean {
		return hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
	}
}