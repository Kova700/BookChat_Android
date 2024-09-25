package com.kova700.bookchat.core.data.channel.external.model

import java.io.IOException

class ChannelIsFullException(message: String?) : IOException(message ?: "Channel is full")
class ChannelIsExplodedException(message: String?) : IOException(message ?: "Channel is exploded")
class UserIsBannedException(message: String?) : IOException(message ?: "User has been Banned")