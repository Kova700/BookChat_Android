package com.example.bookchat.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.bookchat.App
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
//
//class NetworkConnectionManager @Inject constructor() {
//
//	fun checkNetworkState(): Boolean {
//		val connectivityManager =
//			App.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//		val network = connectivityManager.activeNetwork ?: return false
//		val actNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
//
//		return when {
//			actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
//			actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
//			else -> false
//		}
//	}
//}
//
//
//class NetworkChecker @Inject constructor(
//	@ApplicationContext applicationContext: Context,
//	@ApplicationScope applicationScope: CoroutineScope
//) {
//	private val _networkState = MutableStateFlow<NetworkState>(NetworkState.None)
//	val networkState: StateFlow<NetworkState> = _networkState
//
//	private var previousNetworkState: NetworkState = NetworkState.None
//
//	// connectivityManager: 앱에 시스템의 연결 상태를 알림
//	private val connectivityManager: ConnectivityManager =
//		applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//	private val validTransportTypes = listOf(
//		NetworkCapabilities.TRANSPORT_WIFI,
//		NetworkCapabilities.TRANSPORT_CELLULAR
//	)
//
//	// https://developer.android.com/training/basics/network-ops/reading-network-state
//	private val networkCallback = object : ConnectivityManager.NetworkCallback() {
//		override fun onAvailable(network: Network) {
//			super.onAvailable(network)
//			if (previousNetworkState == NetworkState.NotConnected) {
//				applicationScope.launch {
//					delay(RECONNECTION_UI_VISIBLE_TIME)
//					_networkState.value = NetworkState.Connected
//				}
//				_networkState.value = NetworkState.Reconnected.also {
//					previousNetworkState = it
//				}
//			} else {
//				_networkState.value = NetworkState.Connected.also {
//					previousNetworkState = it
//				}
//			}
//		}
//
//		// WIFI에서 모바일 데이터로 변경 시 모바일 데이터로 전환하는 잠깐의 시간 동안
//		// 네트워크에 연결된 상태가 아니므로 onLost가 호출되고 잠깐 뒤에 네트워크 연결 여부를 확인
//		override fun onLost(network: Network) {
//			super.onLost(network)
//			applicationScope.launch {
//				delay(RECONNECTION_DELAY)
//				if (isNetworkAvailable().not()) {
//					_networkState.value = NetworkState.NotConnected
//					previousNetworkState = NetworkState.NotConnected
//				}
//			}
//		}
//	}
//
//	init {
//		initiateNetworkState()
//		registerNetworkCallback(connectivityManager)
//	}
//
//	private fun initiateNetworkState() {
//		_networkState.value = if (isNetworkAvailable()) {
//			NetworkState.Connected.also { previousNetworkState = it }
//		} else {
//			NetworkState.NotConnected.also { previousNetworkState = it }
//		}
//	}
//
//	// addTransportType(int transportType) : Adds the given transport requirement to this builder.
//	private fun registerNetworkCallback(manager: ConnectivityManager) {
//		val networkRequestBuilder = NetworkRequest.Builder().apply {
//			validTransportTypes.forEach { addTransportType(it) }
//		}
//		manager.registerNetworkCallback(networkRequestBuilder.build(), networkCallback)
//	}
//
//	private fun isNetworkAvailable(): Boolean {
//		val capabilities =
//			connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
//
//		if (capabilities != null) {
//			return validTransportTypes.any { capabilities.hasTransport(it) }
//		}
//
//		return false
//	}
//}
//
//sealed class NetworkState {
//	object None : NetworkState()
//	object Connected : NetworkState()
//	object Reconnected : NetworkState()
//	object NotConnected : NetworkState()
//}