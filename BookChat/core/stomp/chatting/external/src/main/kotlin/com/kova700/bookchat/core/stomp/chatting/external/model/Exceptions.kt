package com.kova700.bookchat.core.stomp.chatting.external.model

import java.io.IOException

class SocketConnectionFailureException : IOException()
class DuplicateSocketConnectionException : IOException()
class ChannelSubscriptionFailureException : IOException()
class DuplicateSubscriptionRequestException : IOException()
class NetworkUnavailableException : IOException()
