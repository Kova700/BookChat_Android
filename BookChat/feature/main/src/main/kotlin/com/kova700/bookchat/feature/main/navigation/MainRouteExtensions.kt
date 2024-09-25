package com.kova700.bookchat.feature.main.navigation

import com.kova700.bookchat.core.navigation.MainRoute
import com.kova700.bookchat.feature.main.R

internal fun MainRoute.getResId(): Int = when (this) {
	MainRoute.Home -> R.id.homeFragment
	MainRoute.Bookshelf -> R.id.bookShelfFragment
	MainRoute.Search -> R.id.searchFragment
	MainRoute.ChannelList -> R.id.channelListFragment
	MainRoute.MyPage -> R.id.myPageFragment
}

internal fun Int.resIdToMainRoute(): MainRoute = when (this) {
	R.id.homeFragment -> MainRoute.Home
	R.id.bookShelfFragment -> MainRoute.Bookshelf
	R.id.searchFragment -> MainRoute.Search
	R.id.channelListFragment -> MainRoute.ChannelList
	R.id.myPageFragment -> MainRoute.MyPage
	else -> throw IllegalArgumentException("Invalid resId")
}