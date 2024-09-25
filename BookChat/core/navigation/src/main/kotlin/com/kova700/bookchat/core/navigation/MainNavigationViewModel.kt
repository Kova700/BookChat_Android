package com.kova700.bookchat.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainNavigationViewModel @Inject constructor() : ViewModel() {
	private val _navigateEvent = MutableSharedFlow<MainRoute>()
	val navigateEvent = _navigateEvent.asSharedFlow()

	fun navigateTo(route: MainRoute) = viewModelScope.launch {
		_navigateEvent.emit(route)
	}

}

sealed interface MainRoute {
	data object Home : MainRoute
	data object Bookshelf : MainRoute
	data object Search : MainRoute
	data object ChannelList : MainRoute
	data object MyPage : MainRoute
}