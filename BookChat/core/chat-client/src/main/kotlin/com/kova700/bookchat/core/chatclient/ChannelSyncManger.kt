package com.kova700.bookchat.core.chatclient

import kotlinx.coroutines.Job

interface ChannelSyncManger {
	fun sync(channelIds: Collection<Long>) : Job
}