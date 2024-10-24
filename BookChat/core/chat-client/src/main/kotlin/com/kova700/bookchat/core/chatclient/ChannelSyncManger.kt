package com.kova700.bookchat.core.chatclient

interface ChannelSyncManger {
	fun sync(channelIds: Collection<Long>)
}